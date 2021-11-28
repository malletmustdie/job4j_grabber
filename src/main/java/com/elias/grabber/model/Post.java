package com.elias.grabber.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Post {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd-MMMM-EEEE-yyyy HH:mm:ss");

    private int id;

    private String title;

    private String link;

    private String description;

    private final LocalDateTime created = LocalDateTime.now();

    public Post() {
    }

    public Post(int id, String title, String link, String description) {
        this.id = id;
        this.title = title;
        this.link = link;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Post post = (Post) o;
        return id == post.id && Objects.equals(title, post.title) && Objects.equals(link, post.link);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, link);
    }

    @Override
    public String toString() {
        return String.format(
                "id: %d, title: %s, link: %s, description: %s, created: %s",
                id, title, link, description, FORMATTER.format(created));
    }

}