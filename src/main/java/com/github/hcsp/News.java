package com.github.hcsp;

import java.time.Instant;

public class News {
    private Integer id;
    private String url;
    private String title;
    private String content;
    private Instant createdAt;
    private Instant modifiedAt;

    News(String title, String url, String content) {
        this.title = title;
        this.url = url;
        this.content = content;
    }

    public News() {
    }

    public News(News old) {
        this.id = old.id;
        this.content = old.content;
        this.title = old.title;
        this.createdAt = old.createdAt;
        this.modifiedAt = old.modifiedAt;
    }

    Instant getCreatedAt() {
        return createdAt;
    }

    void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getModifiedAt() {
        return modifiedAt;
    }

    void setModifiedAt(Instant modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
