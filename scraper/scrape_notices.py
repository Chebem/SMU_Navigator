"""Scrape Semyung University notices with Crawl4AI and sync them to Firebase RTDB `Notices/{board_num}`.

Env vars (each key: JSON string in *_JSON, or a file path in *_FILE for local runs):
  FIREBASE_SA_JSON / FIREBASE_SA_FILE     Firebase Admin service account (project smu-navigator). Required unless DRY_RUN=1.
  TRANSLATE_SA_JSON / TRANSLATE_SA_FILE   Cloud Translation service account (project smu-navigator-460213).
  FIREBASE_DB_URL    RTDB URL (default: smu-navigator asia-southeast1).
  PAGES              list pages to scan (default 1; use 3 on first run).
  DRY_RUN=1          print results instead of writing to Firebase.
  TRANSLATE=0        skip KO->EN translation (English fields fall back to Korean).
  NOTIFY=0           skip FCM push for new notices.
  LIMIT              max new notices to fetch this run (default: no limit).
"""

import asyncio
import json
import os
import re
import sys
import time
from urllib.parse import parse_qs, urljoin, urlparse

from bs4 import BeautifulSoup
from crawl4ai import AsyncWebCrawler, BrowserConfig, CacheMode, CrawlerRunConfig
from crawl4ai.extraction_strategy import JsonCssExtractionStrategy

SITE = "https://www.semyung.ac.kr"
BASE = f"{SITE}/prog/vwBoard/bbs01/kor/sub08_02_01"
MNO = "sub08_02_01"
DB_URL = os.getenv("FIREBASE_DB_URL", "https://smu-navigator-default-rtdb.asia-southeast1.firebasedatabase.app/")
PAGES = int(os.getenv("PAGES", "1"))
DRY_RUN = os.getenv("DRY_RUN") == "1"
TRANSLATE = os.getenv("TRANSLATE", "1") != "0"
NOTIFY = os.getenv("NOTIFY", "1") != "0"
LIMIT = int(os.getenv("LIMIT", "0")) or None
MAX_PUSH = 5  # more new notices than this in one run (e.g. first run) -> no push spam
REQUEST_DELAY_S = 1.0
USER_AGENT = "SMUNavigatorBot/1.0 (+https://github.com/Chebem/SMU_Navigator)"

LIST_SCHEMA = {
    "name": "notices",
    "baseSelector": "table.basic_table tbody tr",
    "fields": [
        {"name": "pinned", "selector": "td.problem_number .notice_bul", "type": "text"},
        {"name": "title", "selector": "td.left .list_subject .link a", "type": "text"},
        {"name": "href", "selector": "td.left .list_subject .link a", "type": "attribute", "attribute": "href"},
        {"name": "department", "selector": "td.problem_name", "type": "text"},
        {"name": "date", "selector": "td.date", "type": "text"},
        {"name": "views", "selector": "td.problem_count", "type": "text"},
        {"name": "has_file", "selector": "td.problem_file .pb_file_bg", "type": "attribute", "attribute": "title"},
    ],
}


def clean(text):
    return re.sub(r"\s+", " ", text or "").strip()


def board_num(href):
    # hrefs look like view.do;jsessionid=...?board_num=190541&mno=...
    return (parse_qs(urlparse(href or "").query).get("board_num") or [None])[0]


def view_url(num):
    return f"{BASE}/view.do?board_num={num}&mno={MNO}"


def parse_list(extracted_json):
    rows = {}
    for r in json.loads(extracted_json or "[]"):
        num = board_num(r.get("href"))
        if not num:
            continue
        rows[num] = {  # dict dedupes pinned rows repeated on every page
            "id": num,
            "title_ko": clean(r.get("title")),
            "department": clean(r.get("department")),
            "date": clean(r.get("date")),
            "views": int(re.sub(r"\D", "", r.get("views") or "") or 0),
            "pinned": bool(clean(r.get("pinned"))),
            "url": view_url(num),
        }
    return rows


def parse_detail(html):
    soup = BeautifulSoup(html, "html.parser")
    body = soup.select_one(".bbs_detail_cont .bbs-view-content")
    if body is None:
        return None

    # Sanitize: upstream HTML is untrusted and is shown in the app's WebView
    for tag in body.select("script, style, iframe, object, embed, form, input, button, link, meta, base"):
        tag.decompose()
    for tag in body.find_all(True):
        for attr in list(tag.attrs):
            # event handlers (onclick, onerror, ...) and noisy editor attributes
            if attr.lower().startswith(("on", "data-")) or attr.lower() in ("srcdoc", "formaction"):
                del tag[attr]
    for tag in body.select("[src]"):
        tag["src"] = urljoin(SITE, tag["src"])
    for tag in body.select("[href]"):
        tag["href"] = urljoin(SITE, tag["href"])
    for tag in body.select("[src], [href]"):  # only allow web links (no javascript:, data:, file:)
        for attr in ("src", "href"):
            if tag.get(attr) and not tag[attr].lower().startswith(("http://", "https://")):
                del tag[attr]

    attachments = []
    for form in soup.select('#download form[action*="filedown.do"]'):
        path = form.select_one('input[name="filePath"]')
        name = form.select_one('input[name="fileName"]')
        button = form.select_one("button")
        if path and name:
            attachments.append({
                "name": clean(button.get_text() if button else name["value"]),
                "filePath": path["value"],
                "fileName": name["value"],
            })

    date = soup.select_one(".bbs_detail_tit .info .date")
    return {
        "title_ko": clean(soup.select_one(".bbs_detail_tit h2").get_text()) if soup.select_one(".bbs_detail_tit h2") else None,
        "date": clean(date.get_text()).replace("등록일:", "").strip() if date else None,
        "html_ko": str(body).strip(),
        "attachments": attachments,
    }


class Translator:
    def __init__(self, credentials_info):
        self.client = None
        if not TRANSLATE:
            return
        try:
            from google.cloud import translate_v2
            from google.oauth2 import service_account

            creds = service_account.Credentials.from_service_account_info(credentials_info) if credentials_info else None
            self.client = translate_v2.Client(credentials=creds)
        except Exception as e:  # missing creds / API disabled -> fall back to Korean
            print(f"[warn] translation disabled: {e}")

    def to_en(self, text, html=False):
        if not text or self.client is None:
            return text
        try:
            return self.client.translate(text, source_language="ko", target_language="en",
                                         format_="html" if html else "text")["translatedText"]
        except Exception as e:
            print(f"[warn] translate failed, keeping Korean: {e}")
            return text


def init_firebase(credentials_info):
    import firebase_admin
    from firebase_admin import credentials

    firebase_admin.initialize_app(credentials.Certificate(credentials_info), {"databaseURL": DB_URL})


def existing_ids():
    from firebase_admin import db

    return set((db.reference("Notices").get(shallow=True) or {}).keys())


def send_push(notice):
    from firebase_admin import messaging

    messaging.send(messaging.Message(
        topic="notices",
        notification=messaging.Notification(title="New notice / 새 공지", body=notice["title_ko"]),
        data={"noticeId": notice["id"]},
    ))


def load_key(name):
    if os.getenv(f"{name}_JSON"):
        return json.loads(os.environ[f"{name}_JSON"])
    if os.getenv(f"{name}_FILE"):
        with open(os.environ[f"{name}_FILE"], encoding="utf-8") as f:
            return json.load(f)
    return None


async def main():
    firebase_key = load_key("FIREBASE_SA")
    if not DRY_RUN:
        if not firebase_key:
            sys.exit("FIREBASE_SA_JSON or FIREBASE_SA_FILE is required (or set DRY_RUN=1)")
        init_firebase(firebase_key)
    translator = Translator(load_key("TRANSLATE_SA"))

    browser = BrowserConfig(headless=True, user_agent=USER_AGENT, verbose=False)
    list_cfg = CrawlerRunConfig(extraction_strategy=JsonCssExtractionStrategy(LIST_SCHEMA),
                                cache_mode=CacheMode.BYPASS, verbose=False)
    # target_elements narrows only the Markdown; res.html stays the full page (title, date, attachments)
    detail_cfg = CrawlerRunConfig(target_elements=[".bbs_detail_cont .bbs-view-content"],
                                  cache_mode=CacheMode.BYPASS, verbose=False)

    async with AsyncWebCrawler(config=browser) as crawler:
        rows = {}
        for page in range(1, PAGES + 1):
            res = await crawler.arun(f"{BASE}/list.do?pageIndex={page}", config=list_cfg)
            if not res.success:
                sys.exit(f"list page {page} failed: {res.error_message}")
            rows.update(parse_list(res.extracted_content))
            await asyncio.sleep(REQUEST_DELAY_S)

        if not rows:
            sys.exit("0 notices parsed - the board layout may have changed")  # fails the workflow -> email alert

        known = set() if DRY_RUN else existing_ids()
        new_ids = [n for n in rows if n not in known][:LIMIT]
        print(f"{len(rows)} notices on {PAGES} page(s), {len(new_ids)} new")

        from firebase_admin import db  # noqa: E402 (only used when not DRY_RUN)

        # Existing notices: refresh cheap fields only.
        if not DRY_RUN:
            for n in set(rows) - set(new_ids):
                db.reference(f"Notices/{n}").update({"views": rows[n]["views"], "pinned": rows[n]["pinned"]})

        created = []
        for n in new_ids:
            notice = rows[n]
            res = await crawler.arun(notice["url"], config=detail_cfg)
            await asyncio.sleep(REQUEST_DELAY_S)
            detail = parse_detail(res.html) if res.success else None
            if detail is None:
                print(f"[warn] skip {n}: detail page not parsed")
                continue

            notice.update({k: v for k, v in detail.items() if v})
            notice["text_ko"] = str(res.markdown.raw_markdown if res.markdown else "").strip()
            notice["title_en"] = translator.to_en(notice["title_ko"])
            notice["text_en"] = translator.to_en(notice["text_ko"])
            notice["html_en"] = translator.to_en(notice["html_ko"], html=True)
            notice["scrapedAt"] = int(time.time() * 1000)

            if DRY_RUN:
                preview = {k: (v[:120] + "…" if isinstance(v, str) and len(v) > 120 else v) for k, v in notice.items()}
                print(json.dumps(preview, ensure_ascii=False, indent=2))
            else:
                db.reference(f"Notices/{n}").set(notice)
            created.append(notice)

        if NOTIFY and not DRY_RUN and 0 < len(created) <= MAX_PUSH:
            for notice in created:
                send_push(notice)
        print(f"done: {len(created)} written{' (dry run)' if DRY_RUN else ''}")


if __name__ == "__main__":
    asyncio.run(main())
