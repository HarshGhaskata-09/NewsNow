package com.example.newsnow.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class NewsResponse {
    @SerializedName("status")
    private String status;
    
    @SerializedName("totalResults")
    private int totalResults;
    
    @SerializedName("articles")
    private List<Article> articles;

    public String getStatus() { return status; }
    public int getTotalResults() { return totalResults; }
    public List<Article> getArticles() { return articles; }
}
