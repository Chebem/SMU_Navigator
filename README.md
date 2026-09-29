
# SMU Navigator (SMU 길아잡이)📍
https://github.com/user-attachments/assets/73f72fb0-0522-40f8-97c6-7fb28d7e620e

**SMU Navigator** is a **bilingual Android app (Korean/English)** that helps students and visitors find their way around **Semyung University** and explore nearby places in **Jecheon, South Korea**, with campus guides, a city guide, university notices and a small student social feed.

> Status: **Pre-release, preparing for Google Play (Android)**
> Role: **Team Leader** (Capstone Team Project)

---

## ✨ Features

### Campus & City Guide
- **Campus guide:** faculties, dormitories, convenience facilities and other facilities, each with a detail page
- **City guide:** cafés, restaurants, bars, marts, convenience stores and accommodation around Jecheon, with "nearest to you" filters (1 km / 2 km / all)
- **Live distance:** distance from you (or from campus when you're far away) plus an estimated walking time
- **Map:** Google Maps with category markers; **walking directions open in Kakao Map or Naver Map**, since Google Maps has no walking routes in Korea
- **Reviews & ratings:** one review per person per place, with a live average
- **Favorites:** save places from the map or any detail page

### University Notices
- Notices from Semyung University boards, **scraped daily** and translated into English
- **Push notification** for each new notice

### Social
- Social feed with photo posts (multiple images), **likes** and **comments**
- **Find people** by name or department, **follow / unfollow**, followers and following lists
- **Notifications** when someone follows you (push + in-app list with an unread badge)
- **Report and block** for posts, comments, reviews and users

### Account
- Email sign-up / login, **password reset**
- Profile with photo, department and bio (default avatar when there's no photo)
- **Delete account** in Settings, which removes all of the user's data

---

## Tech Stack

| Area | Technology |
|---|---|
| **Android app** | Java (some Kotlin), XML layouts, Material Components, ViewModel / LiveData, Glide |
| **Backend** | Firebase Authentication, Realtime Database, Storage, Cloud Messaging (FCM), Crashlytics, App Check |
| **Server code** | Firebase Cloud Functions (Node.js 22): follow notifications, account data cleanup |
| **Maps** | Google Maps SDK; Kakao Map / Naver Map for walking directions |
| **Notice scraper** | Python, [Crawl4AI](https://docs.crawl4ai.com/), Google Cloud Translation, runs daily on GitHub Actions |
| **Tooling** | Git, GitHub, GitHub Actions, Firebase CLI |

---

## App Demo
- Demo Video (KR):
https://github.com/user-attachments/assets/89862122-6b26-45de-871f-056a6f621564
- Demo Video (ENG):
https://github.com/user-attachments/assets/5850f6e5-2763-4c45-9aba-7edd2840684c

---

## Conference Paper & Poster Presentation
**MITA International Conference 2025**

This app was extended into a **research paper** that was **accepted** and presented in a **poster session** at an international academic conference in **2025**, and published on **Springer**.

🔗 **Paper:** [View Conference Paper](https://link.springer.com/chapter/10.1007/978-981-95-3141-7_19)

The presentation focused on the **design, implementation and real-world deployment** of a bilingual, data-driven mobile navigation system for campus and local environments.

---

## Privacy
See the [privacy policy](docs/privacy/index.md) for what the app collects and how to delete your account and data.

---

## License
© 2025–2026 Chukwuka Chebem Yvette. **All rights reserved.** This code is shared for viewing only; you may not copy, modify, distribute or publish it without written permission.
