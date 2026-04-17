package com.example.newsnow;

import android.content.Context;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.activity.OnBackPressedCallback;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.newsnow.adapters.NewsAdapter;
import com.example.newsnow.models.Article;
import com.example.newsnow.models.NewsResponse;
import com.example.newsnow.network.ApiClient;
import com.example.newsnow.network.ApiInterface;
import com.example.newsnow.utils.Constants;
import com.example.newsnow.utils.LocaleHelper;
import com.example.newsnow.utils.MockData;
import com.example.newsnow.utils.ThemeHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NewsAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private SearchView searchView;
    private TextView demoBanner;
    private ImageView btnSettings;

    private List<Article> articleList = new ArrayList<>();
    private String currentCategory = "general";
    private Handler autoRefreshHandler = new Handler();
    private Runnable autoRefreshRunnable;

    private static final String PREFS_NAME = "newsnow_prefs";
    private static final String KEY_AUTO_REFRESH = "auto_refresh";
    private int appliedTheme;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply saved theme BEFORE setContentView
        appliedTheme = ThemeHelper.getSavedTheme(this);
        ThemeHelper.applyTheme(appliedTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestNotificationPermission();
        setupExitBottomSheet();

        initUI();
        setupCategoryButtons();
        setupSearch();

        fetchNews(currentCategory);
        startAutoRefresh();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void setupExitBottomSheet() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (searchView != null && !searchView.getQuery().toString().isEmpty()) {
                    searchView.setQuery("", false);
                    searchView.clearFocus();
                } else {
                    showExitDialog();
                }
            }
        });
    }


    private void showExitDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this); 
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_exit, null);
        dialog.setContentView(view);
        
        // Remove background dim if needed, but default is good
        View parent = (View) view.getParent();
        if (parent != null) {
            parent.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        }

        view.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btn_exit).setOnClickListener(v -> {
            dialog.dismiss();
            finishAffinity();
        });

        dialog.show();
    }

    private void initUI() {
        recyclerView = findViewById(R.id.recycler_main);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        progressBar = findViewById(R.id.progress_bar);
        searchView = findViewById(R.id.search_view);
        demoBanner = findViewById(R.id.demo_banner);
        btnSettings = findViewById(R.id.btn_settings);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NewsAdapter(this, articleList);
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(getResources().getColor(R.color.bg_card));
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.accent_vibrant));
        swipeRefreshLayout.setOnRefreshListener(() -> fetchNews(currentCategory));

        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });
    }


    private void setupCategoryButtons() {
        int[] ids = {R.id.btn_general, R.id.btn_business, R.id.btn_entertainment, 
                      R.id.btn_health, R.id.btn_science, R.id.btn_sports, R.id.btn_technology};
        
        for (int id : ids) {
            View v = findViewById(id);
            v.setOnClickListener(view -> {
                for (int otherId : ids) findViewById(otherId).setSelected(false);
                view.setSelected(true);
                
                String category = ((TextView)view).getText().toString().toLowerCase();
                onCategoryClick(category);
            });
        }
        findViewById(R.id.btn_general).setSelected(true);
    }

    private void onCategoryClick(String category) {
        currentCategory = category;
        fetchNews(category);
    }

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                hideKeyboard();
                filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });
    }

    private void filter(String text) {
        List<Article> filteredList = new ArrayList<>();
        for (Article article : articleList) {
            if (article.getTitle().toLowerCase().contains(text.toLowerCase()) || 
                (article.getDescription() != null && article.getDescription().toLowerCase().contains(text.toLowerCase()))) {
                filteredList.add(article);
            }
        }
        adapter.setArticles(filteredList);
    }


    private void fetchNews(String category) {
        progressBar.setVisibility(View.VISIBLE);
        demoBanner.setVisibility(View.GONE);

        ApiInterface apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface.class);
        // Default country "in" or "us"
        Call<NewsResponse> call = apiInterface.getLatestNews(category, "in");

        call.enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                
                if (response.isSuccessful() && response.body() != null && response.body().getArticles() != null && !response.body().getArticles().isEmpty()) {
                    articleList = response.body().getArticles();
                    
                    // Sort articles by date: Newest first
                    Collections.sort(articleList, new Comparator<Article>() {
                        @Override
                        public int compare(Article a1, Article a2) {
                            String d1 = a1.getPublishedAt();
                            String d2 = a2.getPublishedAt();
                            if (d1 == null) return 1;
                            if (d2 == null) return -1;
                            return d2.compareTo(d1); // Descending order
                        }
                    });

                    adapter.setArticles(articleList);
                } else {
                    loadMockData(getString(R.string.notif_permission_msg)); // Using a generic error msg or should add a new one
                }
            }

            @Override
            public void onFailure(Call<NewsResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                loadMockData(getString(R.string.notif_permission_msg));
            }
        });
    }

    private void startAutoRefresh() {
        if (autoRefreshRunnable == null) {
            autoRefreshRunnable = new Runnable() {
                @Override
                public void run() {
                    boolean autoRefreshEnabled = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                            .getBoolean(KEY_AUTO_REFRESH, true);
                    if (autoRefreshEnabled) {
                        swipeRefreshLayout.setRefreshing(true);
                        fetchNews(currentCategory);
                    }
                    autoRefreshHandler.removeCallbacks(this);
                    autoRefreshHandler.postDelayed(this, 15 * 1000); // 15 seconds
                }
            };
        }
    }

    @Override
    public void onBackPressed() {
        if (searchView != null && !searchView.getQuery().toString().isEmpty()) {
            searchView.setQuery("", false);
            searchView.clearFocus();
            return;
        }
        showExitDialog();
    }

    @Override
    protected void onPause() {
        super.onPause();
        autoRefreshHandler.removeCallbacks(autoRefreshRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (autoRefreshRunnable != null) {
            autoRefreshHandler.removeCallbacks(autoRefreshRunnable);
            autoRefreshHandler.postDelayed(autoRefreshRunnable, 15 * 1000);
        }
        // Recreate if theme was changed in Settings
        int currentTheme = ThemeHelper.getSavedTheme(this);
        if (currentTheme != appliedTheme) {
            recreate();
        }
    }

    private void loadMockData(String message) {
        demoBanner.setVisibility(View.VISIBLE);
        demoBanner.setText(message);
        articleList = MockData.getMockArticles(); // You would update MockData to handle categories as per the other plan if desired
        adapter.setArticles(articleList);
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}