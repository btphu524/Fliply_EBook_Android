package com.example.myreadbookapplication.fragment.admin;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myreadbookapplication.R;
import com.example.myreadbookapplication.activity.Admin.AdminAddBookActivity;
import com.example.myreadbookapplication.activity.Admin.AdminEditBookActivity;
import com.example.myreadbookapplication.adapter.AdminBookAdapter;
import com.example.myreadbookapplication.model.ApiResponse;
import com.example.myreadbookapplication.model.Book;
import com.example.myreadbookapplication.model.BooksResponse;
import com.example.myreadbookapplication.network.ApiService;
import com.example.myreadbookapplication.network.RetrofitClient;
import com.example.myreadbookapplication.utils.AuthManager;
import com.example.myreadbookapplication.utils.PaginationManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookFragment extends Fragment {

    private static final String TAG = "BookFragment";
    private static final int REQUEST_ADD_BOOK = 1;
    private static final int REQUEST_EDIT_BOOK = 2;

    private RecyclerView rvBook;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private FloatingActionButton fabAddBook;
    private EditText etSearch;

    private ApiService apiService;
    private AuthManager authManager;
    private static final int ADMIN_ITEMS_PER_PAGE = PaginationManager.DEFAULT_ITEMS_PER_PAGE;
    private List<Book> bookList = new ArrayList<>();
    private AdminBookAdapter bookAdapter;
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private Map<Integer, String> categoryMap = new HashMap<>();
    private boolean categoriesLoaded = false;
    private boolean isDataLoaded = false;
    private PaginationManager paginationManager;
    private FrameLayout paginationContainer;
    private int currentPage = 1;
    private int totalPages = 1;
    private int itemsPerPage = ADMIN_ITEMS_PER_PAGE;
    private int totalItems = 0;
    private boolean isSearching = false;
    private String currentQuery = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiService = RetrofitClient.getApiService();
        authManager = AuthManager.getInstance(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book, container, false);
        
        initViews(view);
        setupRecyclerView();
        setupClickListeners();
        setupSearchListener();
        setupPagination();
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!isDataLoaded) {
            loadCategories();
        }
    }

    private void initViews(View view) {
        rvBook = view.findViewById(R.id.rv_book);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        progressBar = view.findViewById(R.id.progress_bar);
        fabAddBook = view.findViewById(R.id.fab_add_book);
        etSearch = view.findViewById(R.id.et_search);
        paginationContainer = view.findViewById(R.id.pagination_container);
    }

    private void setupRecyclerView() {
        bookAdapter = new AdminBookAdapter(requireContext(), bookList);
        bookAdapter.setOnBookActionListener(new AdminBookAdapter.OnBookActionListener() {
            @Override
            public void onEditClick(Book book) {
                openEditBook(book);
            }

            @Override
            public void onDeleteClick(Book book) {
                showDeleteConfirmDialog(book);
            }
        });
        rvBook.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvBook.setAdapter(bookAdapter);
    }

    private void setupClickListeners() {
        if (fabAddBook != null) {
            fabAddBook.setOnClickListener(v -> openAddBook());
        }
    }

    private void openAddBook() {
        Intent intent = new Intent(requireContext(), AdminAddBookActivity.class);
        startActivityForResult(intent, REQUEST_ADD_BOOK);
    }

    private void openEditBook(Book book) {
        Intent intent = new Intent(requireContext(), AdminEditBookActivity.class);
        intent.putExtra(AdminEditBookActivity.EXTRA_BOOK, book);
        startActivityForResult(intent, REQUEST_EDIT_BOOK);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_ADD_BOOK && resultCode == AdminAddBookActivity.RESULT_BOOK_ADDED) {
            Toast.makeText(requireContext(), "Book added!", Toast.LENGTH_SHORT).show();
            loadBooks();
        } else if (requestCode == REQUEST_EDIT_BOOK && resultCode == AdminEditBookActivity.RESULT_BOOK_UPDATED) {
            Toast.makeText(requireContext(), "Book updated!", Toast.LENGTH_SHORT).show();
            loadBooks();
        }
    }

    private void showDeleteConfirmDialog(Book book) {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_confirm_delete);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView btnCancel = dialog.findViewById(R.id.btn_cancel);
        TextView btnYes = dialog.findViewById(R.id.btn_yes);

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }

        if (btnYes != null) {
            btnYes.setOnClickListener(v -> {
                deleteBook(book);
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    private void deleteBook(Book book) {
        String accessToken = authManager.getAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            Toast.makeText(requireContext(), "Please login as admin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        int bookId = book.getIdAsInt();
        apiService.hardDeleteBook(bookId, "Bearer " + accessToken).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(requireContext(), "Book deleted successfully!", Toast.LENGTH_SHORT).show();

                    bookList.remove(book);
                    bookAdapter.updateBookList(bookList);
                    if (bookList.isEmpty()) {
                        fetchBooks();
                    }
                } else {
                    Toast.makeText(requireContext(), "Delete failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearchListener() {
        if (etSearch == null) return;

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            performSearch();
            return true;
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                searchRunnable = () -> performSearch();
                searchHandler.postDelayed(searchRunnable, 500);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupPagination() {
        if (paginationContainer == null) return;
        paginationManager = new PaginationManager(requireContext(), paginationContainer);
        paginationManager.setVisible(false);
        paginationContainer.setVisibility(View.GONE);
        paginationManager.setMaxVisiblePages(5);
        paginationManager.setOnPageChangeListener(page -> {
            if (page == currentPage) return;
            currentPage = page;
            fetchBooks();
        });
        paginationManager.setOnPageJumpListener(page -> {
            if (page == currentPage) return;
            currentPage = page;
            fetchBooks();
        });
    }

    private void performSearch() {
        if (etSearch == null) return;

        String query = etSearch.getText().toString().trim();
        
        currentPage = 1;
        if (query.isEmpty()) {
            isSearching = false;
            currentQuery = "";
        } else {
            isSearching = true;
            currentQuery = query;
        }
        fetchBooks();
    }

    private void updateListView() {
        if (bookList.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
        }
    }

    /**
     * Public method để reload data từ Activity
     */
    public void reloadData() {
        Log.d(TAG, "Reloading books data...");
        // Clear data và load lại
        currentPage = 1;
        totalPages = 1;
        totalItems = 0;
        currentQuery = "";
        isSearching = false;
        if (paginationManager != null) {
            paginationManager.setVisible(false);
        }
        loadCategories();
    }

    private void loadCategories() {
        apiService.getCategoriesRawBody(null).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseString = response.body().string();
                        Gson gson = new Gson();
                        JsonObject jsonResponse = JsonParser.parseString(responseString).getAsJsonObject();
                        
                        if (!jsonResponse.has("success") || !jsonResponse.get("success").getAsBoolean()) {
                            Log.e(TAG, "Categories API returned success=false");
                            loadBooks();
                            return;
                        }
                        
                        JsonElement dataElement = jsonResponse.get("data");
                        if (dataElement == null || !dataElement.isJsonObject()) {
                            Log.w(TAG, "Data element is null or not an object");
                            loadBooks();
                            return;
                        }
                        
                        JsonObject dataObj = dataElement.getAsJsonObject();
                        JsonElement categoriesElement = dataObj.get("categories");
                        
                        if (categoriesElement != null && categoriesElement.isJsonObject()) {
                            JsonObject categoriesObj = categoriesElement.getAsJsonObject();
                            categoryMap.clear();
                            
                            for (String key : categoriesObj.keySet()) {
                                try {
                                    JsonObject catJson = categoriesObj.get(key).getAsJsonObject();
                                    int categoryId;
                                    
                                    if (catJson.has("_id")) {
                                        try {
                                            JsonElement idElement = catJson.get("_id");
                                            if (idElement.isJsonPrimitive()) {
                                                if (idElement.getAsJsonPrimitive().isNumber()) {
                                                    categoryId = idElement.getAsInt();
                                                } else {
                                                    categoryId = Integer.parseInt(idElement.getAsString());
                                                }
                                            } else {
                                                categoryId = Integer.parseInt(key);
                                            }
                                        } catch (Exception e) {
                                            Log.w(TAG, "Failed to parse _id for key " + key + ", using key as ID: " + e.getMessage());
                                            categoryId = Integer.parseInt(key);
                                        }
                                    } else {
                                        categoryId = Integer.parseInt(key);
                                    }
                                    
                                    String categoryName = null;
                                    if (catJson.has("name")) {
                                        JsonElement nameElement = catJson.get("name");
                                        if (!nameElement.isJsonNull()) {
                                            categoryName = nameElement.getAsString();
                                        }
                                    }
                                    
                                    if (categoryName != null && !categoryName.isEmpty()) {
                                        categoryMap.put(categoryId, categoryName);
                                    }
                                } catch (Exception e) {
                                    Log.w(TAG, "Failed to parse category " + key + ": " + e.getMessage());
                                }
                            }
                            
                            Log.d(TAG, "Loaded " + categoryMap.size() + " categories for mapping");
                            categoriesLoaded = true;
                        } else {
                            Log.w(TAG, "Categories element is not an object or is null");
                            categoriesLoaded = true;
                        }
                        
                        fetchBooks();
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading response body: " + e.getMessage());
                        fetchBooks();
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing categories: " + e.getMessage());
                        fetchBooks();
                    }
                } else {
                    Log.e(TAG, "Failed to load categories - HTTP " + response.code());
                    fetchBooks();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Failed to load categories: " + t.getMessage());
                fetchBooks();
            }
        });
    }

    private void mapCategoryNamesToBooks(List<Book> books) {
        for (Book book : books) {
            if (book != null) {
                int categoryId = book.getCategory();
                String categoryName = categoryMap.get(categoryId);
                
                if (categoryName == null || categoryName.isEmpty()) {
                    categoryName = categoryMap.get(Integer.valueOf(categoryId));
                }
                
                if ((categoryName == null || categoryName.isEmpty()) && !categoryMap.isEmpty()) {
                    for (Integer key : categoryMap.keySet()) {
                        if (key != null && key.intValue() == categoryId) {
                            categoryName = categoryMap.get(key);
                            break;
                        }
                    }
                }
                
                if (categoryName != null && !categoryName.isEmpty()) {
                    book.setCategoryName(categoryName);
                }
            }
        }
    }
    

    private void loadBooks() {
        String accessToken = authManager.getAccessToken();
        
        if (accessToken == null || accessToken.isEmpty()) {
            Toast.makeText(requireContext(), "Please login as admin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (layoutEmpty != null) {
            layoutEmpty.setVisibility(View.GONE);
        }

        fetchBooks();
    }

    private void fetchBooks() {
        String accessToken = authManager.getAccessToken();
        if (TextUtils.isEmpty(accessToken)) {
            Toast.makeText(requireContext(), "Please login as admin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (layoutEmpty != null) {
            layoutEmpty.setVisibility(View.GONE);
        }

        Call<ApiResponse<BooksResponse>> call;
        if (isSearching && !TextUtils.isEmpty(currentQuery)) {
            call = apiService.searchBooks(currentQuery, currentPage, itemsPerPage);
        } else {
            call = apiService.getAllBooks("Bearer " + accessToken, currentPage, itemsPerPage);
        }

        call.enqueue(new Callback<ApiResponse<BooksResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<BooksResponse>> call, Response<ApiResponse<BooksResponse>> response) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }

                if (!response.isSuccessful() || response.body() == null || !response.body().isSuccess()) {
                    Log.e(TAG, "Failed to load books. Code: " + response.code());
                    if (bookList.isEmpty()) {
                        showEmptyState();
                    }
                    return;
                }

                BooksResponse booksResponse = response.body().getData();
                List<Book> books = booksResponse != null && booksResponse.getBooks() != null
                        ? booksResponse.getBooks()
                        : new ArrayList<>();
                if (books.size() > ADMIN_ITEMS_PER_PAGE) {
                    books = new ArrayList<>(books.subList(0, ADMIN_ITEMS_PER_PAGE));
                }

                if (categoriesLoaded) {
                    mapCategoryNamesToBooks(books);
                }

                bookList = new ArrayList<>(books);
                bookAdapter.updateBookList(bookList);
                updateListView();

                BooksResponse.Pagination pagination = booksResponse != null ? booksResponse.getPagination() : null;
                if (pagination != null) {
                    currentPage = pagination.getPage();
                    totalPages = Math.max(pagination.getTotalPages(), 1);
                    totalItems = pagination.getTotal();
                    itemsPerPage = ADMIN_ITEMS_PER_PAGE;
                } else {
                    totalPages = (int) Math.ceil((double) totalItems / ADMIN_ITEMS_PER_PAGE);
                    totalItems = Math.max(totalItems, bookList.size());
                    itemsPerPage = ADMIN_ITEMS_PER_PAGE;
                }

                if (paginationManager != null) {
                    paginationManager.setPaginationData(currentPage, totalPages, totalItems, ADMIN_ITEMS_PER_PAGE);
                    boolean showPager = totalPages > 1;
                    paginationManager.setVisible(showPager);
                    if (paginationContainer != null) {
                        paginationContainer.setVisibility(showPager ? View.VISIBLE : View.GONE);
                    }
                }

                isDataLoaded = true;
            }

            @Override
            public void onFailure(Call<ApiResponse<BooksResponse>> call, Throwable t) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                Log.e(TAG, "Error loading books: " + t.getMessage());
                if (bookList.isEmpty()) {
                    showEmptyState();
                }
            }
        });
    }

    private void showEmptyState() {
        if (layoutEmpty != null) {
            layoutEmpty.setVisibility(View.VISIBLE);
        }
        if (rvBook != null) {
            rvBook.setVisibility(View.GONE);
        }
    }

    private void hideEmptyState() {
        if (layoutEmpty != null) {
            layoutEmpty.setVisibility(View.GONE);
        }
        if (rvBook != null) {
            rvBook.setVisibility(View.VISIBLE);
        }
    }
}

