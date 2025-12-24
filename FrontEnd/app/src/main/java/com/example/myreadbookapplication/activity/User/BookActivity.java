package com.example.myreadbookapplication.activity.User;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myreadbookapplication.R;
import com.example.myreadbookapplication.adapter.AllBooksAdapter;
import com.example.myreadbookapplication.model.Book;
import com.example.myreadbookapplication.model.ApiResponse;
import com.example.myreadbookapplication.model.BooksResponse;
import com.example.myreadbookapplication.network.ApiService;
import com.example.myreadbookapplication.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.myreadbookapplication.utils.PaginationManager;

public class BookActivity extends AppCompatActivity {

    private static final String TAG = "BookActivity";

    private TextView tvTitle;
    private RecyclerView rvBooks;
    private AllBooksAdapter bookAdapter;  // Adapter mới cho all books
    private ProgressBar progressBar;
    private ApiService apiService;
    private ImageView backAllBookIcon;
    private java.util.Map<Integer, String> categoryIdToName = new java.util.HashMap<>();
    private java.util.List<Book> allBooks = new java.util.ArrayList<>();
    private int currentPage = 1;
    private int totalPages = Integer.MAX_VALUE;
    private int pageSize = PaginationManager.DEFAULT_ITEMS_PER_PAGE;
    private boolean isLoading = false;
    private GridLayoutManager gridLayoutManager;
    private boolean isLastPage = false; // Để biết hết data chưa
    
    // Pagination
    private PaginationManager paginationManager;
    private android.widget.FrameLayout paginationContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);  // Layout mới

        // Ánh xạ views
        //toolbar = findViewById(R.id.toolbar);
        tvTitle = findViewById(R.id.tv_book_title);
        rvBooks = findViewById(R.id.rv_books);
        progressBar = findViewById(R.id.progressBar_books);
        paginationContainer = findViewById(R.id.pagination_container);
        apiService = RetrofitClient.getApiService();
        backAllBookIcon = findViewById(R.id.back_all_book_icon);

        // No Toolbar in layout, title is handled by TextView

        backAllBookIcon.setOnClickListener(v -> finish());

        // Initialize pagination
        initPagination();

        // Nhận extra từ Home (nếu có)
        boolean isAllBooks = getIntent().getBooleanExtra("all_books", true);  // Default true
        String title = getIntent().getStringExtra("selected_category_name") != null
                ? getIntent().getStringExtra("selected_category_name") : "All Books";
        tvTitle.setText(title);
        tvTitle.setVisibility(View.VISIBLE);
        Log.d(TAG, "BookActivity opened for: " + title + ", AllBooks: " + isAllBooks);

        // Load categories first to map category id -> name, then load books
        loadCategoriesThenBooks();
    }

    private void initPagination() {
        paginationManager = new PaginationManager(this, paginationContainer);
        paginationManager.setOnPageChangeListener(page -> {
            currentPage = page;
            loadAllBooks();
        });
        paginationManager.setOnPageJumpListener(page -> {
            currentPage = page;
            loadAllBooks();
        });
    }

    // Back button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Cập nhật loadAllBooks() để sử dụng pagination
    private void loadAllBooks() {
        if (isLoading) return;
        isLoading = true;
        progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "Loading books page=" + currentPage + ", size=" + pageSize);

        pageSize = PaginationManager.DEFAULT_ITEMS_PER_PAGE;
        Call<ApiResponse<BooksResponse>> call = apiService.getBooks(null, "active", pageSize, currentPage);
        call.enqueue(new Callback<ApiResponse<BooksResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<BooksResponse>> call, Response<ApiResponse<BooksResponse>> response) {
                progressBar.setVisibility(View.GONE);
                isLoading = false;
                Log.d(TAG, "Books response code: " + response.code());
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    BooksResponse bookResp = response.body().getData();
                    List<Book> booksList = (bookResp != null) ? bookResp.getBooks() : null;
                    int totalItems = booksList != null ? booksList.size() : 0;

                    // Cập nhật pagination từ response
                    if (bookResp != null && bookResp.getPagination() != null) {
                        try {
                            pageSize = PaginationManager.DEFAULT_ITEMS_PER_PAGE;
                            totalPages = bookResp.getPagination().getTotalPages();
                            totalItems = bookResp.getPagination().getTotal();
                            
                            // Update pagination UI
                            paginationManager.setPaginationData(currentPage, totalPages, totalItems, pageSize);
                            paginationManager.setVisible(totalPages > 1);
                            paginationContainer.setVisibility(totalPages > 1 ? View.VISIBLE : View.GONE);
                        } catch (Exception ignored) {}
                    } else {
                        totalPages = Math.max(1, (int) Math.ceil((double) totalItems / PaginationManager.DEFAULT_ITEMS_PER_PAGE));
                        paginationManager.setPaginationData(currentPage, totalPages, totalItems, PaginationManager.DEFAULT_ITEMS_PER_PAGE);
                        paginationManager.setVisible(totalPages > 1);
                        paginationContainer.setVisibility(totalPages > 1 ? View.VISIBLE : View.GONE);
                    }

                    Log.d(TAG, "Loaded page=" + currentPage + ", count=" + (booksList != null ? booksList.size() : 0) + "/ totalPages=" + totalPages);
                    if (booksList != null && !booksList.isEmpty()) {
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
                        List<Book> pageBooks = booksList.subList(startIndex, endIndex);

                        allBooks.clear();
                        allBooks.addAll(pageBooks);
                        
                        if (bookAdapter == null) {
                            bookAdapter = new AllBooksAdapter(allBooks, BookActivity.this, categoryIdToName);
                            gridLayoutManager = new GridLayoutManager(BookActivity.this, 2);
                            rvBooks.setLayoutManager(gridLayoutManager);
                            rvBooks.setAdapter(bookAdapter);
                            rvBooks.scrollToPosition(0);
                        } else {
                            bookAdapter.notifyDataSetChanged();
                            rvBooks.scrollToPosition(0);
                        }
                        rvBooks.invalidate();
                    } else {
                        if (allBooks.isEmpty()) {
                            Toast.makeText(BookActivity.this, "No books found", Toast.LENGTH_SHORT).show();
                        }
                        paginationManager.setVisible(false);
                        paginationContainer.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(BookActivity.this, "Load books failed: " + response.message(), Toast.LENGTH_SHORT).show();
                    paginationManager.setVisible(false);
                    paginationContainer.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<BooksResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                isLoading = false;
                Log.e(TAG, "Books failure: " + t.getMessage());
                Toast.makeText(BookActivity.this, "Network error. Tap to retry.", Toast.LENGTH_SHORT).show();
                paginationManager.setVisible(false);
            }
        });
    }

    // Reset pagination khi quay lại activity
    @Override
    protected void onResume() {
        super.onResume();
        if (allBooks.isEmpty()) { // Chỉ reset nếu list rỗng (tránh load lại hết)
            currentPage = 1;
            allBooks.clear();
            if (bookAdapter != null) {
                bookAdapter.notifyDataSetChanged();
            }
            loadCategoriesThenBooks(); // Hoặc chỉ loadAllBooks() nếu categories đã có
        }
    }

    private void loadCategoriesThenBooks() {
        progressBar.setVisibility(View.VISIBLE);
        retrofit2.Call<com.example.myreadbookapplication.model.ApiResponse<com.example.myreadbookapplication.model.CategoriesResponse>> call =
                apiService.getCategories("active");
        call.enqueue(new retrofit2.Callback<com.example.myreadbookapplication.model.ApiResponse<com.example.myreadbookapplication.model.CategoriesResponse>>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.myreadbookapplication.model.ApiResponse<com.example.myreadbookapplication.model.CategoriesResponse>> call,
                                   retrofit2.Response<com.example.myreadbookapplication.model.ApiResponse<com.example.myreadbookapplication.model.CategoriesResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    com.example.myreadbookapplication.model.CategoriesResponse data = response.body().getData();
                    if (data != null && data.getCategories() != null) {
                        for (com.example.myreadbookapplication.model.Category c : data.getCategories()) {
                            if (c != null) categoryIdToName.put(c.getId(), c.getName());
                        }
                    }
                }
                // Regardless of success, continue to load books
                loadAllBooks();
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.myreadbookapplication.model.ApiResponse<com.example.myreadbookapplication.model.CategoriesResponse>> call, Throwable t) {
                // Continue with books even if categories fail
                loadAllBooks();
            }
        });
    }
}