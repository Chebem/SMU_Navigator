package com.example.smunavigator2.Domain;

import java.io.Serializable;

public class NoticeModel implements Serializable {
    public String id;
    public String title_ko;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle_ko() {
        return title_ko;
    }

    public void setTitle_ko(String title_ko) {
        this.title_ko = title_ko;
    }

    public String getTitle_en() {
        return title_en;
    }

    public void setTitle_en(String title_en) {
        this.title_en = title_en;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getText_ko() {
        return text_ko;
    }

    public void setText_ko(String text_ko) {
        this.text_ko = text_ko;
    }

    public String getText_en() {
        return text_en;
    }

    public void setText_en(String text_en) {
        this.text_en = text_en;
    }

    public String getHtml_ko() {
        return html_ko;
    }

    public void setHtml_ko(String html_ko) {
        this.html_ko = html_ko;
    }

    public String getHtml_en() {
        return html_en;
    }

    public void setHtml_en(String html_en) {
        this.html_en = html_en;
    }

    public String title_en;
    public String department;
    public String date;
    public String url;
    public String text_ko;
    public String text_en;
    public String html_ko;
    public String html_en;

    public NoticeModel() {
        // Required for Firebase
    }

    public NoticeModel(String id, String title_ko, String title_en, String department, String date, String url,
                       String text_ko, String text_en, String html_ko, String html_en) {
        this.id = id;
        this.title_ko = title_ko;
        this.title_en = title_en;
        this.department = department;
        this.date = date;
        this.url = url;
        this.text_ko = text_ko;
        this.text_en = text_en;
        this.html_ko = html_ko;
        this.html_en = html_en;
    }
}
