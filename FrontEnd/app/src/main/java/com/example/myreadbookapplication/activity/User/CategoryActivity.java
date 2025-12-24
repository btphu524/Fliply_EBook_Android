package com.example.myreadbookapplication.activity.User;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myreadbookapplication.R;
import com.example.myreadbookapplication.adapter.CategoryAdapter;
import com.example.myreadbookapplication.adapter.CategoryBookAdapter;
import com.example.myreadbookapplication.utils.GridSpacingItemDecoration;
import com.example.myreadbookapplication.model.Book;
import com.example.myreadbookapplication.model.Category;
import com.example.myreadbookapplication.model.ApiResponse;
import com.example.myreadbookapplication.model.BooksResponse;
import com.example.myreadbookapplication.model.CategoriesResponse;
import com.example.myreadbookapplication.network.ApiService;
import com.example.myreadbookapplication.network.RetrofitClient;
import com.example.myreadbookapplication.utils.PaginationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryActivity extends AppCompatActivity {

    private static final String TAG = "CategoryActivity";  // Tag cho log

    private RecyclerView rvCategoriesContent;
    private CategoryAdapter categoryAdapter;
    private CategoryBookAdapter categoryBookAdapter;
    private ImageView backIconCategory;
    private TextView tvCategoryTitle;
    private ProgressBar progressBar;
    private ApiService apiService;
    private FrameLayout paginationContainer;
    private PaginationManager paginationManager;

    // Pagination state for books
    private int currentPage = 1;
    private int totalPages = 1;
    private int totalItems = 0;
    private int itemsPerPage = PaginationManager.DEFAULT_ITEMS_PER_PAGE;
    private int currentCategoryId = -1;
    private String currentCategoryName = "";
    private final List<Book> categoryBooks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        // Ánh xạ views
        backIconCategory = findViewById(R.id.back_icon_category);
        rvCategoriesContent = findViewById(R.id.rv_category_books);
        tvCategoryTitle = findViewById(R.id.tv_category_title);
        progressBar = findViewById(R.id.progressBar_category);
        apiService = RetrofitClient.getApiService();
        paginationContainer = findViewById(R.id.pagination_container);
        int edgeSpacing = dpToPx(12);
        int innerSpacing = dpToPx(12);
        int verticalSpacing = dpToPx(16);
        rvCategoriesContent.setClipToPadding(false);
        rvCategoriesContent.setPadding(0, verticalSpacing / 2, 0, verticalSpacing);
        if (rvCategoriesContent.getItemDecorationCount() == 0) {
            rvCategoriesContent.addItemDecoration(new GridSpacingItemDecoration(2, innerSpacing, edgeSpacing, verticalSpacing));
        }

        initPagination();

        // Nhận extra từ Intent
        String selectedCategoryIdStr = getIntent().getStringExtra("selected_category_id");
        String selectedCategoryName = getIntent().getStringExtra("selected_category_name");
        Log.d(TAG, "Received extra - ID: " + selectedCategoryIdStr + ", Name: " + selectedCategoryName);  // Debug extra

        // Set title ngay lập tức nếu có name (trước API call)
        if (selectedCategoryName != null && !selectedCategoryName.isEmpty()) {
            tvCategoryTitle.setText(selectedCategoryName);
            tvCategoryTitle.setVisibility(View.VISIBLE);
            Log.d(TAG, "Set header title: " + selectedCategoryName);
        }

        boolean isFullList = (selectedCategoryIdStr == null || selectedCategoryIdStr.isEmpty());
        Log.d(TAG, "isFullList: " + isFullList);

        backIconCategory.setOnClickListener(v -> finish());

        if (isFullList) {
            loadFullCategories();
        } else {
            loadBooksForCategory(selectedCategoryIdStr, selectedCategoryName);
        }
    }

    private void initPagination() {
        paginationManager = new PaginationManager(this, paginationContainer);
        paginationManager.setVisible(false);
        paginationManager.setOnPageChangeListener(page -> {
            currentPage = page;
            fetchBooksForCategoryPage();
        });
        paginationManager.setOnPageJumpListener(page -> {
            currentPage = page;
            fetchBooksForCategoryPage();
        });
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void loadFullCategories() {
        progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "Loading full categories...");
        tvCategoryTitle.setVisibility(View.VISIBLE);
        paginationManager.setVisible(false);
        paginationContainer.setVisibility(View.GONE);

        Call<ApiResponse<CategoriesResponse>> call = apiService.getCategories("active");
        call.enqueue(new Callback<ApiResponse<CategoriesResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<CategoriesResponse>> call, Response<ApiResponse<CategoriesResponse>> response) {
                progressBar.setVisibility(View.GONE);
                Log.d(TAG, "Full categories response code: " + response.code());
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    CategoriesResponse catResp = response.body().getData();
                    List<Category> allCategories = (catResp != null) ? catResp.getCategories() : null;
                    if (allCategories != null) {
                        allCategories = allCategories.stream()
                                .filter(cat -> cat != null && "active".equals(cat.getStatus()))
                                .collect(Collectors.toList());
                        Log.d(TAG, "Filtered full categories size: " + allCategories.size());
                    }
                    if (allCategories != null && !allCategories.isEmpty()) {
                        categoryAdapter = new CategoryAdapter(allCategories, CategoryActivity.this, new CategoryAdapter.OnCategoryClickListener() {
                            @Override
                            public void onCategoryClick(Category category) {
                                Log.d(TAG, "Clicked category in full list: " + category.getName());
                                Intent intent = new Intent(CategoryActivity.this, CategoryActivity.class);
                                intent.putExtra("selected_category_id", String.valueOf(category.getId()));
                                intent.putExtra("selected_category_name", category.getName());
                                startActivity(intent);
                            }
                        });
                        rvCategoriesContent.setLayoutManager(new GridLayoutManager(CategoryActivity.this, 2));
                        rvCategoriesContent.setAdapter(categoryAdapter);
                        rvCategoriesContent.invalidate();  // Force refresh UI
                        Log.d(TAG, "Full categories adapter set");
                    } else {
                        Log.w(TAG, "No full categories data");
                        Toast.makeText(CategoryActivity.this, "No categories found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Full categories API fail: " + response.code());
                    Toast.makeText(CategoryActivity.this, "Load categories failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<CategoriesResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Full categories failure: " + t.getMessage());
                Toast.makeText(CategoryActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadBooksForCategory(String categoryIdStr, String categoryName) {
        currentCategoryId = -1;
        currentCategoryName = categoryName != null ? categoryName : "";

        if (categoryIdStr != null && !categoryIdStr.isEmpty()) {
            try {
                currentCategoryId = Integer.parseInt(categoryIdStr);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Invalid category ID: " + categoryIdStr);
            }
        }

        if (currentCategoryId == -1) {
            Toast.makeText(this, "Category không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        currentPage = 1;
        totalPages = 1;
        totalItems = 0;
        itemsPerPage = PaginationManager.DEFAULT_ITEMS_PER_PAGE;
        categoryBooks.clear();
        paginationManager.setVisible(false);

        if (tvCategoryTitle.getVisibility() != View.VISIBLE) {
            tvCategoryTitle.setVisibility(View.VISIBLE);
        }
        tvCategoryTitle.setText(currentCategoryName);

        fetchBooksForCategoryPage();
    }

    private void fetchBooksForCategoryPage() {
        if (currentCategoryId == -1) {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "Loading books for category ID: " + currentCategoryId + " page " + currentPage);
        Call<ApiResponse<BooksResponse>> call = apiService.getBooks(String.valueOf(currentCategoryId), "active", PaginationManager.DEFAULT_ITEMS_PER_PAGE, currentPage);
        call.enqueue(new Callback<ApiResponse<BooksResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<BooksResponse>> call, Response<ApiResponse<BooksResponse>> response) {
                progressBar.setVisibility(View.GONE);
                Log.d(TAG, "Books response code: " + response.code() + " for category " + currentCategoryName);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    BooksResponse bookResp = response.body().getData();
                    List<Book> booksList = (bookResp != null) ? bookResp.getBooks() : null;
                    Log.d(TAG, "Books data size: " + (booksList != null ? booksList.size() : 0));

                    int backendTotalPages = 1;
                    int backendTotalItems = booksList != null ? booksList.size() : 0;

                    if (bookResp != null && bookResp.getPagination() != null) {
                        try {
                            backendTotalPages = bookResp.getPagination().getTotalPages() > 0 ? bookResp.getPagination().getTotalPages() : 1;
                            backendTotalItems = bookResp.getPagination().getTotal();
                            itemsPerPage = PaginationManager.DEFAULT_ITEMS_PER_PAGE;
                            totalPages = backendTotalPages;
                            totalItems = backendTotalItems;
                            paginationManager.setPaginationData(currentPage, totalPages, totalItems, PaginationManager.DEFAULT_ITEMS_PER_PAGE);
                            paginationManager.setVisible(totalPages > 1);
                            paginationContainer.setVisibility(totalPages > 1 ? View.VISIBLE : View.GONE);
                        } catch (Exception e) {
                            Log.w(TAG, "Error parsing pagination: " + e.getMessage());
                            paginationManager.setVisible(false);
                            paginationContainer.setVisibility(View.GONE);
                        }
                    } else {
                        itemsPerPage = PaginationManager.DEFAULT_ITEMS_PER_PAGE;
                        totalItems = backendTotalItems;
                        totalPages = (int) Math.ceil((double) totalItems / PaginationManager.DEFAULT_ITEMS_PER_PAGE);
                        paginationManager.setPaginationData(currentPage, totalPages, totalItems, PaginationManager.DEFAULT_ITEMS_PER_PAGE);
                        paginationManager.setVisible(totalPages > 1);
                        paginationContainer.setVisibility(totalPages > 1 ? View.VISIBLE : View.GONE);
                    }

                    if (booksList != null && !booksList.isEmpty()) {
                        // Filter active nếu cần (từ BE đã filter, nhưng an toàn)
                        booksList = booksList.stream()
                                .filter(book -> book != null && "active".equals(book.getStatus()))
                                .collect(Collectors.toList());

                        int totalAvailable = booksList.size();
                        int perPage = PaginationManager.DEFAULT_ITEMS_PER_PAGE;
                        int startIndex = Math.max(0, (currentPage - 1) * perPage);
                        if (totalAvailable <= perPage) {
                            startIndex = 0;
                        } else if (startIndex > totalAvailable - 1) {
                            startIndex = Math.max(0, totalAvailable - perPage);
                        }
                        int endIndex = Math.min(startIndex + perPage, totalAvailable);
                        if (endIndex <= startIndex) {
                            startIndex = Math.max(0, totalAvailable - perPage);
                            endIndex = Math.min(startIndex + perPage, totalAvailable);
                        }
                        List<Book> pageBooks = new ArrayList<>(booksList.subList(startIndex, endIndex));

                        categoryBooks.clear();
                        categoryBooks.addAll(pageBooks);

                        rvCategoriesContent.setLayoutManager(new GridLayoutManager(CategoryActivity.this, 2));
                        categoryBookAdapter = new CategoryBookAdapter(new ArrayList<>(categoryBooks), CategoryActivity.this, currentCategoryName);
                        rvCategoriesContent.setAdapter(categoryBookAdapter);
                        rvCategoriesContent.scrollToPosition(0);
                        rvCategoriesContent.invalidate();  // Force refresh
                        Log.d(TAG, "Books adapter set: " + categoryBooks.size() + " items (page " + currentPage + ")");
                    } else {
                        Log.w(TAG, "No books data for " + currentCategoryName);
                        categoryBooks.clear();
                        categoryBookAdapter = new CategoryBookAdapter(new ArrayList<>(), CategoryActivity.this, currentCategoryName);
                        rvCategoriesContent.setLayoutManager(new GridLayoutManager(CategoryActivity.this, 2));
                        rvCategoriesContent.setAdapter(categoryBookAdapter);
                        paginationManager.setVisible(false);
                        paginationContainer.setVisibility(View.GONE);
                        Toast.makeText(CategoryActivity.this, "No books in " + currentCategoryName + " yet", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Books API fail for " + currentCategoryName + ": " + response.code());
                    paginationManager.setVisible(false);
                    paginationContainer.setVisibility(View.GONE);
                    Toast.makeText(CategoryActivity.this, "Failed to load books for " + currentCategoryName, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<BooksResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Books failure for " + currentCategoryName + ": " + t.getMessage());
                paginationManager.setVisible(false);
                paginationContainer.setVisibility(View.GONE);
                Toast.makeText(CategoryActivity.this, "Network error loading " + currentCategoryName, Toast.LENGTH_SHORT).show();
            }
        });
    }
}