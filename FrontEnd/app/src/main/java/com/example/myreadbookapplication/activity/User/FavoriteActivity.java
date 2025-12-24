package com.example.myreadbookapplication.activity.User;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myreadbookapplication.R;
import com.example.myreadbookapplication.adapter.CategoryBookAdapter;
import com.example.myreadbookapplication.model.ApiResponse;
import com.example.myreadbookapplication.model.Book;
import com.example.myreadbookapplication.model.BooksResponse;
import com.example.myreadbookapplication.model.FavoritesResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myreadbookapplication.network.ApiService;
import com.example.myreadbookapplication.network.RetrofitClient;
import com.example.myreadbookapplication.utils.AuthManager;
import com.example.myreadbookapplication.utils.PaginationManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoriteActivity extends AppCompatActivity {

    private static final String TAG = "FavoriteActivity";
    private ImageView backIconFavoriteBook;
    private RecyclerView rvFavoriteBooks;
    private ProgressBar progressBarFavoriteBooks;
    private CategoryBookAdapter favoriteBookAdapter;
    private ApiService apiService;
    
    // Pagination
    private PaginationManager paginationManager;
    private android.widget.FrameLayout paginationContainer;
    private int currentPage = 1;
    private int totalPages = 1;
    private int totalItems = 0;
    private int itemsPerPage = PaginationManager.DEFAULT_ITEMS_PER_PAGE;
    
    // Frontend pagination data
    private List<Book> allFavorites = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        // Ánh xạ cac thanh phan
        backIconFavoriteBook = findViewById(R.id.back_favorite_book_icon);
        rvFavoriteBooks = findViewById(R.id.rv_favorite_books);
        progressBarFavoriteBooks = findViewById(R.id.progressBar_favorite_books);
        paginationContainer = findViewById(R.id.pagination_container);
        apiService = RetrofitClient.getApiService();

        // Back click
        backIconFavoriteBook.setOnClickListener(v -> finish());

        // Initialize pagination
        initPagination();

        // Load favorites
        setupFavoriteBooks();
    }

    private void initPagination() {
        paginationManager = new PaginationManager(this, paginationContainer);
        paginationManager.setOnPageChangeListener(page -> {
            currentPage = page;
            // Chỉ cần update UI với data đã có
            if (!allFavorites.isEmpty()) {
                setupFrontendPagination(allFavorites);
            }
        });
        paginationManager.setOnPageJumpListener(page -> {
            currentPage = page;
            // Chỉ cần update UI với data đã có
            if (!allFavorites.isEmpty()) {
                setupFrontendPagination(allFavorites);
            }
        });
    }

    private void setupFavoriteBooks() {
        progressBarFavoriteBooks.setVisibility(View.VISIBLE);
        
        AuthManager authManager = AuthManager.getInstance(this);
        String userId = authManager.getUserId();
        String token = authManager.getAccessToken();

        if (userId != null && token != null && !token.isEmpty()) {
            Log.d(TAG, "Fetching favorites from backend for user: " + userId);
            Call<ApiResponse<FavoritesResponse>> call = apiService.getFavorites(userId, "Bearer " + token);
            call.enqueue(new Callback<ApiResponse<com.example.myreadbookapplication.model.FavoritesResponse>>() {
                @Override
                public void onResponse(Call<ApiResponse<FavoritesResponse>> call, Response<ApiResponse<FavoritesResponse>> response) {
                    progressBarFavoriteBooks.setVisibility(View.GONE);
                    
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        FavoritesResponse data = response.body().getData();
                        List<Book> allFavorites = (data != null) ? data.getFavoriteBooks() : null;
                        
                        if (allFavorites != null && !allFavorites.isEmpty()) {
                            // Lưu trữ tất cả favorites để dùng cho pagination
                            FavoriteActivity.this.allFavorites.clear();
                            FavoriteActivity.this.allFavorites.addAll(allFavorites);
                            
                            // Frontend pagination - chia nhỏ danh sách favorites
                            setupFrontendPagination(FavoriteActivity.this.allFavorites);
                        } else {
                            Toast.makeText(FavoriteActivity.this, "No favorite books", Toast.LENGTH_SHORT).show();
                            paginationManager.setVisible(false);
                            paginationContainer.setVisibility(View.GONE);
                        }
                        // Sync local cache of ids
                        if (data != null && data.getFavoriteBookIds() != null) {
                            Gson gson = new Gson();
                            SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
                            prefs.edit().putString("favorite_books", gson.toJson(data.getFavoriteBookIds())).apply();
                        }
                    } else {
                        Toast.makeText(FavoriteActivity.this, "Load favorites failed", Toast.LENGTH_SHORT).show();
                        paginationManager.setVisible(false);
                        paginationContainer.setVisibility(View.GONE);
                        loadFavoritesFromLocal();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<com.example.myreadbookapplication.model.FavoritesResponse>> call, Throwable t) {
                    progressBarFavoriteBooks.setVisibility(View.GONE);
                    Log.e(TAG, "Favorites failure: " + t.getMessage());
                    paginationManager.setVisible(false);
                    paginationContainer.setVisibility(View.GONE);
                    loadFavoritesFromLocal();
                }
            });
        } else {
            Log.d(TAG, "No token/userId, fallback to local cache");
            paginationManager.setVisible(false);
            paginationContainer.setVisibility(View.GONE);
            loadFavoritesFromLocal();
        }
    }

    //Pagination
    private void setupFrontendPagination(List<Book> allFavorites) {
        // Tính toán pagination cho frontend
        totalItems = allFavorites.size();
        totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);
        
        // Update pagination UI
        paginationManager.setPaginationData(currentPage, totalPages, totalItems, itemsPerPage);
        paginationManager.setVisible(totalPages > 1);
        paginationContainer.setVisibility(totalPages > 1 ? View.VISIBLE : View.GONE);
        
        // Lấy items cho trang hiện tại
        int startIndex = (currentPage - 1) * itemsPerPage;
        if (startIndex >= totalItems) {
            currentPage = 1;
            startIndex = 0;
            paginationManager.setPaginationData(currentPage, totalPages, totalItems, itemsPerPage);
        }
        int endIndex = Math.min(startIndex + itemsPerPage, totalItems);
        
        List<Book> pageFavorites = new ArrayList<>(allFavorites.subList(startIndex, endIndex));
        
        // Hiển thị favorites cho trang hiện tại
        favoriteBookAdapter = new CategoryBookAdapter(pageFavorites, FavoriteActivity.this, "Favorites");
        rvFavoriteBooks.setLayoutManager(new GridLayoutManager(FavoriteActivity.this, 2));
        rvFavoriteBooks.setAdapter(favoriteBookAdapter);
    }

    private void loadFavoritesFromLocal() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String favoriteBooksJson = prefs.getString("favorite_books", "[]");
        Gson gson = new Gson();
        Type type = new TypeToken<List<String>>() {}.getType();
        List<String> favoriteBookIds = gson.fromJson(favoriteBooksJson, type);
        if (favoriteBookIds == null || favoriteBookIds.isEmpty()) {
            Toast.makeText(this, "No favorite books yet", Toast.LENGTH_SHORT).show();
            paginationManager.setVisible(false);
            paginationContainer.setVisibility(View.GONE);
            return;
        }
        String idsQuery = String.join(",", favoriteBookIds);
        Call<ApiResponse<BooksResponse>> call = apiService.getBooksByIds(idsQuery, "active", null, 1);
        call.enqueue(new Callback<ApiResponse<BooksResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<BooksResponse>> call, Response<ApiResponse<BooksResponse>> response) {
                progressBarFavoriteBooks.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    BooksResponse bookResp = response.body().getData();
                    List<Book> favoritesList = (bookResp != null) ? bookResp.getBooks() : null;
                    if (favoritesList != null && !favoritesList.isEmpty()) {
                        // Lưu trữ tất cả favorites để dùng cho pagination
                        FavoriteActivity.this.allFavorites.clear();
                        FavoriteActivity.this.allFavorites.addAll(favoritesList);
                        
                        // Frontend pagination
                        setupFrontendPagination(FavoriteActivity.this.allFavorites);
                    } else {
                        Toast.makeText(FavoriteActivity.this, "No favorite books found", Toast.LENGTH_SHORT).show();
                        paginationManager.setVisible(false);
                    }
                } else {
                    Toast.makeText(FavoriteActivity.this, "Load favorites failed", Toast.LENGTH_SHORT).show();
                    paginationManager.setVisible(false);
                    paginationContainer.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<BooksResponse>> call, Throwable t) {
                progressBarFavoriteBooks.setVisibility(View.GONE);
                Toast.makeText(FavoriteActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                paginationManager.setVisible(false);
                paginationContainer.setVisibility(View.GONE);
            }
        });
    }
}