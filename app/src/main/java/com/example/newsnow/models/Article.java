package com.example.newsnow.models;

import com.google.gson.annotations.SerializedName;

public class Article {
    @SerializedName("source")
    private Source source;
    
    @SerializedName("author")
    private String author;
    
    @SerializedName("title")
    private String title;
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("url")
    private String url;
    
    @SerializedName("urlToImage")
    private String urlToImage;
    
    @SerializedName("publishedAt")
    private String publishedAt;

    public Source getSource() { return source; }
    public String getAuthor() { return author; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getUrl() { return url; }
    public String getUrlToImage() { return urlToImage; }
    public String getPublishedAt() { return publishedAt; }

    public static class Source {
        @SerializedName("id")
        private String id;
        @SerializedName("name")
        private String name;

        public String getId() { return id; }
        public String getName() { return name; }
    }
}
