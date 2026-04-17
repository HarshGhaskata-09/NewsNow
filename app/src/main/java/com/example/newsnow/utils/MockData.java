package com.example.newsnow.utils;

import com.example.newsnow.models.Article;
import java.util.ArrayList;
import java.util.List;

public class MockData {

    public static List<Article> getMockArticles() {
        List<Article> articles = new ArrayList<>();

        articles.add(createArticle(
                "James Webb Telescope Captures Stunning New Images of Distant Galaxy",
                "NASA's flagship telescope has revealed unprecedented details of a galaxy 12 billion light years away.",
                "https://images.unsplash.com/photo-1614728894747-a83421e2b9c9?q=80&w=1000",
                "NASA News",
                "12:00 PM"
        ));

        articles.add(createArticle(
                "Tech Giants Announce New AI Collaboration to Set Safety Standards",
                "Leading AI companies have agreed on a set of voluntary safety measures to guide development.",
                "https://images.unsplash.com/photo-1677442136019-21780ecad995?q=80&w=1000",
                "Tech Daily",
                "2h ago"
        ));

        articles.add(createArticle(
                "Stock Markets Hit Record Highs Amid Economic Optimism",
                "Global indices rose today as investors reacted positively to falling inflation data.",
                "https://images.unsplash.com/photo-1611974715853-2b8ef9d4d842?q=80&w=1000",
                "Financial Times",
                "4h ago"
        ));

        articles.add(createArticle(
                "Upcoming Sports Season: What to Expect from the Top Teams",
                "An in-depth look at the rosters and strategies of this year's top championship contenders.",
                "https://images.unsplash.com/photo-1504450758481-7338eba7524a?q=80&w=1000",
                "Sports Central",
                "6h ago"
        ));

        return articles;
    }

    private static Article createArticle(String title, String desc, String urlToImage, String sourceName, String publishedAt) {
        // We need to use reflection or a builder if we don't have a public constructor, 
        // but for now, I'll assume we can use a custom mock wrapper or just return objects.
        // Actually, I'll just create a helper that matches the GSON mapping.
        return new MockArticle(title, desc, urlToImage, sourceName, publishedAt);
    }

    // Since our Article class might have private fields and no setters, 
    // we use a subclass or just modify Article to have setters.
    // I will modify Article.java to have a constructor for testing/mocking.
    
    public static class MockArticle extends Article {
        private String mockTitle;
        private String mockDesc;
        private String mockImg;
        private String mockSource;
        private String mockDate;

        public MockArticle(String title, String desc, String img, String source, String date) {
            this.mockTitle = title;
            this.mockDesc = desc;
            this.mockImg = img;
            this.mockSource = source;
            this.mockDate = date;
        }

        @Override public String getTitle() { return mockTitle; }
        @Override public String getDescription() { return mockDesc; }
        @Override public String getUrlToImage() { return mockImg; }
        @Override public String getPublishedAt() { return mockDate; }
        @Override public String getUrl() { return "https://www.google.com"; }
        @Override public Source getSource() {
            return new Source() {
                @Override public String getName() { return mockSource; }
            };
        }
    }
}
