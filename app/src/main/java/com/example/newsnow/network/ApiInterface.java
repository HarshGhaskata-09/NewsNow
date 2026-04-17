package com.example.newsnow.network;

import com.example.newsnow.models.NewsResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;

public interface ApiInterface {

    // endpoint: https://saurav.tech/NewsAPI/top-headlines/category/{category}/{country}.json
    @Headers("User-Agent: NewsNow")
    @GET("top-headlines/category/{category}/{country}.json")
    Call<NewsResponse> getLatestNews(
            @Path("category") String category,
            @Path("country") String country
    );
}
