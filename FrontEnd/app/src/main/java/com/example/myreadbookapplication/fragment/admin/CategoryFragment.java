package com.example.myreadbookapplication.fragment.admin;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
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
import com.example.myreadbookapplication.activity.Admin.AdminAddCategoryActivity;
import com.example.myreadbookapplication.activity.Admin.AdminEditCategoryActivity;
import com.example.myreadbookapplication.adapter.AdminCategoryAdapter;
import com.example.myreadbookapplication.model.ApiResponse;
import com.example.myreadbookapplication.model.Category;
import com.example.myreadbookapplication.network.ApiService;
import com.example.myreadbookapplication.network.RetrofitClient;
import com.example.myreadbookapplication.utils.AuthManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Collections;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryFragment extends Fragment {

    private static final String TAG = "CategoryFragment";
    private static final int REQUEST_ADD_CATEGORY = 1;
    private static final int REQUEST_EDIT_CATEGORY = 2;

    private RecyclerView rvCategory;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private FloatingActionButton fabAddCategory;
    private EditText etSearch;

    private ApiService apiService;
    private AuthManager authManager;
    private List<Category> categoryList = new ArrayList<>();
    private List<Category> allCategoriesList = new ArrayList<>();
    private AdminCategoryAdapter categoryAdapter;
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    
    private boolean isDataLoaded = false; // Flag để track data đã load chưa

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiService = RetrofitClient.getApiService();
        authManager = AuthManager.getInstance(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category, container, false);
        
        initViews(view);
        setupRecyclerView();
        setupClickListeners();
        setupSearchListener();
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Load data lần đầu
        if (!isDataLoaded) {
            loadCategories();
        }
    }

    private void initViews(View view) {
        rvCategory = view.findViewById(R.id.rv_category);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        progressBar = view.findViewById(R.id.progress_bar);
        fabAddCategory = view.findViewById(R.id.fab_add_category);
        etSearch = view.findViewById(R.id.et_search);
    }

    private void setupRecyclerView() {
        categoryAdapter = new AdminCategoryAdapter(requireContext(), categoryList);
        categoryAdapter.setOnCategoryActionListener(new AdminCategoryAdapter.OnCategoryActionListener() {
            @Override
            public void onEditClick(Category category) {
                if (category != null) {
                    openEditCategory(category);
                }
            }

            @Override
            public void onDeleteClick(Category category) {
                if (category != null) {
                    showDeleteConfirmDialog(category);
                }
            }
        });
        rvCategory.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvCategory.setAdapter(categoryAdapter);
    }

    private void setupClickListeners() {
        if (fabAddCategory != null) {
            fabAddCategory.setOnClickListener(v -> openAddCategory());
        }
    }

    private void openAddCategory() {
        Intent intent = new Intent(requireContext(), AdminAddCategoryActivity.class);
        startActivityForResult(intent, REQUEST_ADD_CATEGORY);
    }

    private void openEditCategory(Category category) {
        try {
            Intent intent = new Intent(requireContext(), AdminEditCategoryActivity.class);
            intent.putExtra(AdminEditCategoryActivity.EXTRA_CATEGORY, category);
            startActivityForResult(intent, REQUEST_EDIT_CATEGORY);
        } catch (Exception e) {
            Log.e(TAG, "Error opening edit category: " + e.getMessage());
            Toast.makeText(requireContext(), "Error opening edit screen", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ADD_CATEGORY && resultCode == AdminAddCategoryActivity.RESULT_CATEGORY_ADDED) {
            Toast.makeText(requireContext(), "Category added!", Toast.LENGTH_SHORT).show();
            loadCategories();
        } else if (requestCode == REQUEST_EDIT_CATEGORY && resultCode == AdminEditCategoryActivity.RESULT_CATEGORY_UPDATED) {
            Toast.makeText(requireContext(), "Category updated!", Toast.LENGTH_SHORT).show();
            loadCategories();
        }
    }

    private void showDeleteConfirmDialog(Category category) {
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
                deleteCategory(category);
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    private void deleteCategory(Category category) {
        if (category == null) {
            Toast.makeText(requireContext(), "Invalid category", Toast.LENGTH_SHORT).show();
            return;
        }

        String accessToken = authManager != null ? authManager.getAccessToken() : null;
        if (accessToken == null || accessToken.isEmpty()) {
            Toast.makeText(requireContext(), "Please login as admin", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        int categoryId = category.getId();
        String categoryName = category.getName();
        Log.d(TAG, "Hard deleting category - ID: " + categoryId + ", Name: " + categoryName);

        apiService.hardDeleteCategory(categoryId, "Bearer " + accessToken).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful()) {
                    if (response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(requireContext(), "Category permanently deleted!", Toast.LENGTH_SHORT).show();
                        
                        // Remove from lists
                        categoryList.remove(category);
                        allCategoriesList.remove(category);
                        
                        // Update adapter
                        categoryAdapter.updateCategoryList(categoryList);
                        
                        // Update UI
                        if (categoryList.isEmpty()) {
                            showEmptyState();
                        } else {
                            hideEmptyState();
                        }
                        
                        Log.d(TAG, "Category permanently deleted - ID: " + categoryId);
                    } else {
                        String errorMsg = "Delete failed";
                        if (response.body() != null && response.body().getMessage() != null) {
                            errorMsg = response.body().getMessage();
                        }
                        Log.e(TAG, "Delete failed - " + errorMsg);
                        Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorMsg = "Delete failed (HTTP " + response.code() + ")";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Delete error response: " + errorBody);
                            if (errorBody.contains("message")) {
                                int msgStart = errorBody.indexOf("\"message\"");
                                if (msgStart > 0) {
                                    int colonIdx = errorBody.indexOf(":", msgStart);
                                    int quoteStart = errorBody.indexOf("\"", colonIdx);
                                    int quoteEnd = errorBody.indexOf("\"", quoteStart + 1);
                                    if (quoteStart > 0 && quoteEnd > quoteStart) {
                                        errorMsg = errorBody.substring(quoteStart + 1, quoteEnd);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error response: " + e.getMessage());
                    }
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Delete category network error: " + t.getMessage(), t);
                String errorMsg = "Network error";
                if (t.getMessage() != null) {
                    errorMsg += ": " + t.getMessage();
                }
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
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

    private void performSearch() {
        if (etSearch == null) return;

        String query = etSearch.getText().toString().trim().toLowerCase();

        if (query.isEmpty()) {
            categoryList = new ArrayList<>(allCategoriesList);
            categoryAdapter.updateCategoryList(categoryList);
            updateListView();
            return;
        }

        List<Category> filtered = new ArrayList<>();
        for (Category category : allCategoriesList) {
            if (category != null && category.getName() != null) {
                if (category.getName().toLowerCase().contains(query)) {
                    filtered.add(category);
                }
            }
        }

        categoryList = filtered;
        categoryAdapter.updateCategoryList(categoryList);
        updateListView();
    }

    private void updateListView() {
        if (categoryList == null || categoryList.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
        }
    }

    /**
     * Public method để reload data từ Activity
     */
    public void reloadData() {
        Log.d(TAG, "Reloading categories data...");
        loadCategories();
    }

    private void loadCategories() {
        if (progressBar == null) return;

        progressBar.setVisibility(View.VISIBLE);
        if (layoutEmpty != null) {
            layoutEmpty.setVisibility(View.GONE);
        }

        Log.d(TAG, "Loading all categories...");
        Call<ResponseBody> call = apiService.getAllCategoriesRaw();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseString = response.body().string();
                        Log.d(TAG, "Response string length: " + responseString.length());

                        Gson gson = new Gson();
                        JsonObject jsonResponse = JsonParser.parseString(responseString).getAsJsonObject();

                        // Check success
                        if (!jsonResponse.has("success") || !jsonResponse.get("success").getAsBoolean()) {
                            String errorMsg = jsonResponse.has("message") ?
                                jsonResponse.get("message").getAsString() : "Failed to load categories";
                            Log.e(TAG, errorMsg);
                            Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
                            showEmptyState();
                            return;
                        }

                        // Get data object
                        JsonElement dataElement = jsonResponse.get("data");
                        if (dataElement == null || !dataElement.isJsonObject()) {
                            Log.w(TAG, "Data element is null or not an object");
                            showEmptyState();
                            return;
                        }

                        JsonObject dataObj = dataElement.getAsJsonObject();
                        JsonElement categoriesElement = dataObj.get("categories");

                        List<Category> parsedCategories = new ArrayList<>();

                        if (categoriesElement != null && categoriesElement.isJsonObject()) {
                            // Parse Firebase object format: {"2": {...}, "3": {...}}
                            Log.d(TAG, "Parsing categories as object format");
                            JsonObject categoriesObj = categoriesElement.getAsJsonObject();
                            for (String key : categoriesObj.keySet()) {
                                try {
                                    JsonObject catJson = categoriesObj.get(key).getAsJsonObject();
                                    Category category = gson.fromJson(catJson, Category.class);
                                    if (category != null) {
                                        // Set ID từ key nếu chưa có hoặc là 0
                                        if (category.getId() == 0) {
                                            try {
                                                category.setId(Integer.parseInt(key));
                                            } catch (NumberFormatException e) {
                                                Log.w(TAG, "Cannot parse key as ID: " + key);
                                                continue;
                                            }
                                        }
                                        parsedCategories.add(category);
                                    }
                                } catch (Exception e) {
                                    Log.w(TAG, "Failed to parse category " + key + ": " + e.getMessage());
                                }
                            }
                            Log.d(TAG, "Parsed " + parsedCategories.size() + " categories from object format");
                        } else if (categoriesElement != null && categoriesElement.isJsonArray()) {
                            // Parse as array
                            parsedCategories = gson.fromJson(categoriesElement, new TypeToken<List<Category>>(){}.getType());
                            Log.d(TAG, "Parsed " + parsedCategories.size() + " categories as array");
                        } else {
                            Log.w(TAG, "Categories element is null or invalid format");
                        }

                        if (!parsedCategories.isEmpty()) {
                            LinkedHashMap<Integer, Category> uniqueMap = new LinkedHashMap<>();
                            for (Category category : parsedCategories) {
                                if (category != null) {
                                    uniqueMap.put(category.getId(), category);
                                }
                            }
                            parsedCategories = new ArrayList<>(uniqueMap.values());

                            Collections.sort(parsedCategories, (c1, c2) -> {
                                if (c1 == null || c2 == null) return 0;
                                return c1.getName().compareToIgnoreCase(c2.getName());
                            });

                            allCategoriesList.clear();
                            allCategoriesList.addAll(parsedCategories);

                            categoryList.clear();
                            categoryList.addAll(allCategoriesList);

                            categoryAdapter.updateCategoryList(categoryList);
                            updateListView();

                            isDataLoaded = true;
                            Log.d(TAG, "Loaded " + categoryList.size() + " categories successfully");
                        } else {
                            Log.w(TAG, "No categories found after parsing");
                            showEmptyState();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading response body: " + e.getMessage(), e);
                        Toast.makeText(requireContext(), "Error reading response", Toast.LENGTH_SHORT).show();
                        showEmptyState();
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing response: " + e.getMessage(), e);
                        Toast.makeText(requireContext(), "Error parsing response: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        showEmptyState();
                    }
                } else {
                    Log.e(TAG, "Load categories failed - HTTP " + response.code());
                    String errorMsg = "Failed to load categories (HTTP " + response.code() + ")";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBody);
                            if (errorBody.contains("message")) {
                                int msgStart = errorBody.indexOf("\"message\"");
                                if (msgStart > 0) {
                                    int colonIdx = errorBody.indexOf(":", msgStart);
                                    int quoteStart = errorBody.indexOf("\"", colonIdx);
                                    int quoteEnd = errorBody.indexOf("\"", quoteStart + 1);
                                    if (quoteStart > 0 && quoteEnd > quoteStart) {
                                        errorMsg = errorBody.substring(quoteStart + 1, quoteEnd);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error response: " + e.getMessage());
                    }
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                Log.e(TAG, "Load categories network error: " + t.getMessage(), t);
                String errorMsg = "Network error";
                if (t.getMessage() != null) {
                    errorMsg += ": " + t.getMessage();
                }
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }

    private void showEmptyState() {
        if (layoutEmpty != null) {
            layoutEmpty.setVisibility(View.VISIBLE);
        }
        if (rvCategory != null) {
            rvCategory.setVisibility(View.GONE);
        }
    }

    private void hideEmptyState() {
        if (layoutEmpty != null) {
            layoutEmpty.setVisibility(View.GONE);
        }
        if (rvCategory != null) {
            rvCategory.setVisibility(View.VISIBLE);
        }
    }
}

