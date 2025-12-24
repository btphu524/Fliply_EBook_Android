package com.example.myreadbookapplication.activity.User;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myreadbookapplication.R;
import com.example.myreadbookapplication.adapter.AllBooksAdapter;
import com.example.myreadbookapplication.model.ApiResponse;
import com.example.myreadbookapplication.model.Book;
import com.example.myreadbookapplication.model.BooksResponse;
import com.example.myreadbookapplication.model.Category;
import com.example.myreadbookapplication.network.ApiService;
import com.example.myreadbookapplication.network.RetrofitClient;
import com.example.myreadbookapplication.utils.PaginationManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    private EditText etSearch;
    private ImageView btnSearch;
    private ImageView btnBack;
    private RecyclerView rvSearchResults;
    private ProgressBar progressBar;
    private FrameLayout paginationContainer;
    private LinearLayout layoutEmpty;
    private AllBooksAdapter searchAdapter;
    private ApiService apiService;
    private Map<Integer, String> categoryIdToName;
    private List<Book> searchResults;
    private String currentQuery = "";
    private int currentPage = 1;
    private final int pageSize = 10;
    private PaginationManager paginationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Đặt fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                           WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.activity_search);

        // Khởi tạo views
        initViews();
        
        // Khởi tạo API service
        apiService = RetrofitClient.getApiService();
        
        // Khởi tạo dữ liệu
        searchResults = new ArrayList<>();
        categoryIdToName = new HashMap<>();
        
        // Setup RecyclerView
        setupRecyclerView();
        
        // Load categories để map category ID với tên
        loadCategories();
        
        // Setup listeners
        setupListeners();

        // Setup pagination
        setupPagination();
    }

    private void initViews() {
        etSearch = findViewById(R.id.et_search);
        btnSearch = findViewById(R.id.btn_search);
        btnBack = findViewById(R.id.btn_back);
        rvSearchResults = findViewById(R.id.rv_search_results);
        progressBar = findViewById(R.id.progress_bar);
        layoutEmpty = findViewById(R.id.layout_empty);
        paginationContainer = findViewById(R.id.pagination_container);
    }
    private void setupPagination() {
        if(paginationContainer == null ) return;

        //create pagination and add to container
        paginationManager = new PaginationManager(this, paginationContainer);

        //set clistener: when user click , search new page with current query
        paginationManager.setOnPageChangeListener(newPage -> {
            currentPage = newPage;
            if(!currentQuery.isEmpty()){
                searchBooks(currentQuery, currentPage); //reload api wich new page
            }
        });

        paginationManager.setVisible(false);
    }

    private void setupRecyclerView() {
        // Sử dụng GridLayoutManager với 2 cột như trong ảnh demo
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        rvSearchResults.setLayoutManager(gridLayoutManager);
        
        searchAdapter = new AllBooksAdapter(searchResults, this, categoryIdToName);
        rvSearchResults.setAdapter(searchAdapter);
    }

    private void setupListeners() {
        // Nút back
        btnBack.setOnClickListener(v -> finish());

        // Nút search
        btnSearch.setOnClickListener(v -> performSearch());

        // Text change listener để search real-time
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (!query.equals(currentQuery)) {
                    currentQuery = query;
                    currentPage = 1;
                    if(paginationManager != null) paginationManager.setVisible(false);
                    if (query.length() >= 2) {
                        // Delay search để tránh gọi API quá nhiều
                        rvSearchResults.removeCallbacks(searchRunnable);
                        rvSearchResults.postDelayed(searchRunnable, 500);
                    } else if (query.isEmpty()) {
                        clearResults();
                    }
                }
            }
        });

        // Enter key listener
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            performSearch();
            return true;
        });
    }

    private final Runnable searchRunnable = new Runnable() {
        @Override
        public void run() {
            performSearch();
        }
    };

    private void performSearch() {
        String query = etSearch.getText().toString().trim();
        if (query.isEmpty()) {
            clearResults();
            return;
        }

        if (query.equals(currentQuery) && !searchResults.isEmpty()) {
            return; // Đã search rồi, không cần search lại
        }

        currentQuery = query;
        currentPage = 1;
        searchBooks(query, currentPage);
    }

    private void searchBooks(String query, int page) {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
        if(paginationManager != null) paginationManager.setVisible(false);

        Log.d("SearchActivity", "Searching for: " + query + "on page: " + page);

        Call<ApiResponse<BooksResponse>> call = apiService.searchBooks(query, page, pageSize);
        call.enqueue(new Callback<ApiResponse<BooksResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<BooksResponse>> call, 
                                 @NonNull Response<ApiResponse<BooksResponse>> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    BooksResponse booksResponse = response.body().getData();
                    if (booksResponse != null && booksResponse.getBooks() != null) {
                        searchResults.clear();
                        searchResults.addAll(booksResponse.getBooks());
                        searchAdapter.notifyDataSetChanged();

                        // Thêm log để check pagination data
                        if (booksResponse.getPagination() != null) {
                            Log.d("SearchActivity", "API returned: page=" + booksResponse.getPagination().getPage()
                                    + ", totalPages=" + booksResponse.getPagination().getTotalPages());
                        }

                        updatePagination(booksResponse);
                        
                        if (searchResults.isEmpty()) {
                            showEmptyState();
                        } else {
                            hideEmptyState();
                        }
                        
                        Log.d("SearchActivity", "Found " + searchResults.size() + " books");
                    } else {
                        showEmptyState();
                        updatePagination(null); //hiden pagination if no data
                    }
                } else {
                    Log.e("SearchActivity", "Search API failed: " + response.code());
                    Toast.makeText(SearchActivity.this, "Search failed", Toast.LENGTH_SHORT).show();
                    showEmptyState();
                    if (paginationManager != null) paginationManager.setVisible(false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<BooksResponse>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e("SearchActivity", "Search failure: " + t.getMessage());
                Toast.makeText(SearchActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                showEmptyState();
                if(paginationManager != null) paginationManager.setVisible(false);
            }
        });
    }

    private void updatePagination(BooksResponse booksResponse) {
        if (paginationManager == null || booksResponse == null || booksResponse.getPagination() == null) {
            if (paginationManager != null) {
                paginationManager.setVisible(false);
            }
            if (paginationContainer != null) paginationContainer.setVisibility(View.GONE);
            return;
        }

        // Giả sử Pagination có getTotalPages(), getTotal(), getPage()
        int totalPages = booksResponse.getPagination().getTotalPages();
        int totalItems = booksResponse.getPagination().getTotal();
        currentPage = booksResponse.getPagination().getPage();  // Sync từ API

        // Update state và UI
        paginationManager.setPaginationData(currentPage, totalPages, totalItems, pageSize);

        // Hiển thị nếu >1 trang
        boolean visible = totalPages > 1;
        paginationManager.setVisible(visible);
        if (paginationContainer != null) {
            paginationContainer.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    private void loadCategories() {
        Call<ApiResponse<com.example.myreadbookapplication.model.CategoriesResponse>> call = 
            apiService.getCategories("active");
        call.enqueue(new Callback<ApiResponse<com.example.myreadbookapplication.model.CategoriesResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<com.example.myreadbookapplication.model.CategoriesResponse>> call, 
                                 @NonNull Response<ApiResponse<com.example.myreadbookapplication.model.CategoriesResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    com.example.myreadbookapplication.model.CategoriesResponse categoriesResponse = response.body().getData();
                    if (categoriesResponse != null && categoriesResponse.getCategories() != null) {
                        for (Category category : categoriesResponse.getCategories()) {
                            if (category != null) {
                                categoryIdToName.put(category.getId(), category.getName());
                            }
                        }
                        Log.d("SearchActivity", "Loaded " + categoryIdToName.size() + " categories");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<com.example.myreadbookapplication.model.CategoriesResponse>> call, @NonNull Throwable t) {
                Log.e("SearchActivity", "Categories load failure: " + t.getMessage());
            }
        });
    }

    private void clearResults() {
        searchResults.clear();
        searchAdapter.notifyDataSetChanged();
        currentPage =1;
        if(paginationManager != null) paginationManager.setVisible(false);
        hideEmptyState();
    }

    private void showEmptyState() {
        layoutEmpty.setVisibility(View.VISIBLE);
        rvSearchResults.setVisibility(View.GONE);
        if (paginationManager != null ) paginationManager.setVisible(false);
    }

    private void hideEmptyState() {
        layoutEmpty.setVisibility(View.GONE);
        rvSearchResults.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
