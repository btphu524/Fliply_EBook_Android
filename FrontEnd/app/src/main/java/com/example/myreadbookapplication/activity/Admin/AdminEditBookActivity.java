package com.example.myreadbookapplication.activity.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Spinner;
import android.widget.ArrayAdapter;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myreadbookapplication.R;
import com.example.myreadbookapplication.model.ApiResponse;
import com.example.myreadbookapplication.model.Book;
import com.example.myreadbookapplication.model.UpdateBookRequest;
import com.example.myreadbookapplication.model.Category;
import com.example.myreadbookapplication.network.ApiService;
import com.example.myreadbookapplication.network.RetrofitClient;
import com.example.myreadbookapplication.utils.AuthManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import okhttp3.ResponseBody;

public class AdminEditBookActivity extends AppCompatActivity {

    public static final String EXTRA_BOOK = "book";
    public static final int RESULT_BOOK_UPDATED = 101;
    private ImageView ivBack;
    private TextView tvTitle;
    private EditText etName;
    private Spinner spCategory;
    private EditText etImage;
    private EditText etAuthor;
    private EditText etDescription;
    private EditText etLinkPdf;
    private Button btnUpdate;
    private ProgressBar progressBar;
    private ApiService apiService;
    private AuthManager authManager;
    private Book currentBook;
    private final List<String> categoryNames = new ArrayList<>();
    private final List<Integer> categoryIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_add_book);

        apiService = RetrofitClient.getApiService();
        authManager = AuthManager.getInstance(this);

        initViews();
        setupListeners();

        Intent intent = getIntent();
        if (intent == null || !intent.hasExtra(EXTRA_BOOK)) {
            Toast.makeText(this, "Missing book to edit", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentBook = (Book) intent.getSerializableExtra(EXTRA_BOOK);
        btnUpdate.setText("UPDATE");
        if (tvTitle != null) tvTitle.setText("Edit book");
        populateFields();
        updateButtonState();
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back_add_book_admin);
        tvTitle = findViewById(R.id.tv_title);
        etName = findViewById(R.id.et_name);
        spCategory = findViewById(R.id.sp_category);
        etImage = findViewById(R.id.et_image);
        etAuthor = findViewById(R.id.et_author);
        etDescription = findViewById(R.id.et_description);
        etLinkPdf = findViewById(R.id.et_link_pdf);
        btnUpdate = findViewById(R.id.btn_add);
        progressBar = findViewById(R.id.progress_bar);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categoryNames);
        spCategory.setAdapter(adapter);
    }

    private void setupListeners() {
        if (ivBack != null) ivBack.setOnClickListener(v -> onBackPressed());

        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { updateButtonState(); }
        };
        etName.addTextChangedListener(watcher);
        etAuthor.addTextChangedListener(watcher);
        etImage.addTextChangedListener(watcher);
        etDescription.addTextChangedListener(watcher);
        etLinkPdf.addTextChangedListener(watcher);

        spCategory.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) { updateButtonState(); }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) { updateButtonState(); }
        });

        btnUpdate.setOnClickListener(v -> {
            if (validateInputs()) { updateBook(); }
        });
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
        finish();
    }

    private void populateFields() {
        if (currentBook == null) return;
        etName.setText(currentBook.getTitle());
        etAuthor.setText(currentBook.getAuthor());
        etDescription.setText(currentBook.getDescription());
        etImage.setText(currentBook.getCoverUrl());
        etLinkPdf.setText(currentBook.getBookUrl());

        loadCategoriesAndSelect(currentBook.getCategory());
    }

    private void updateButtonState() {
        if (btnUpdate == null) return;
        
        try {
            // Backend only requires title and author, other fields are optional
            boolean hasName = etName != null && !etName.getText().toString().trim().isEmpty();
            boolean hasAuthor = etAuthor != null && !etAuthor.getText().toString().trim().isEmpty();
            
            // Category is optional in backend, but if categories are loaded, we need selection
            boolean hasCategory = true;
            if (spCategory != null && !categoryIds.isEmpty()) {
                // Categories loaded, need valid selection
                hasCategory = spCategory.getSelectedItemPosition() >= 0;
            }
            // If categories not loaded yet, allow button enabled (category will be optional in update)
            
            // Enable button if we have required fields (title and author)
            boolean enabled = hasName && hasAuthor && hasCategory;
            btnUpdate.setEnabled(enabled);
            btnUpdate.setAlpha(enabled ? 1.0f : 0.5f); // Visual feedback
            
            // Debug log
            android.util.Log.d("AdminEditBook", "Button state - Name: " + hasName + ", Author: " + hasAuthor + 
                    ", Category: " + hasCategory + ", CategoryIds size: " + categoryIds.size() + 
                    ", Selected position: " + (spCategory != null ? spCategory.getSelectedItemPosition() : -1) +
                    ", Enabled: " + enabled);
        } catch (Exception e) {
            android.util.Log.e("AdminEditBook", "Error updating button state: " + e.getMessage(), e);
            btnUpdate.setEnabled(true); // Enable by default to allow updates
        }
    }

    private boolean validateInputs() {
        // Only title and author are required by backend, others are optional
        if (etName.getText().toString().trim().isEmpty()) { 
            etName.setError("Title is required"); 
            return false; 
        }
        if (etAuthor.getText().toString().trim().isEmpty()) { 
            etAuthor.setError("Author is required"); 
            return false; 
        }
        // Category is optional, but if categories are loaded and none selected, warn
        if (!categoryIds.isEmpty() && spCategory.getSelectedItemPosition() < 0) { 
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show(); 
            return false; 
        }
        return true;
    }

    private void updateBook() {
        String token = authManager != null ? authManager.getAccessToken() : null;
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Please login as admin", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(android.view.View.VISIBLE);
        btnUpdate.setEnabled(false);

        UpdateBookRequest req = new UpdateBookRequest();
        String title = etName.getText().toString().trim();
        if (!title.isEmpty()) req.title = title;

        String image = etImage.getText().toString().trim();
        if (!image.isEmpty()) req.cover_url = image;

        String pdfUrl = etLinkPdf.getText().toString().trim();
        if (!pdfUrl.isEmpty()) req.book_url = pdfUrl;

        String author = etAuthor.getText().toString().trim();
        if (!author.isEmpty()) req.author = author;
        String description = etDescription.getText().toString().trim();
        if (!description.isEmpty()) req.description = description;

        // category id from spinner
        if (spCategory.getSelectedItemPosition() >= 0 && spCategory.getSelectedItemPosition() < categoryIds.size()) {
            req.category = categoryIds.get(spCategory.getSelectedItemPosition());
        }

        int bookId = currentBook != null ? currentBook.getIdAsInt() : -1;
        if (bookId <= 0) {
            progressBar.setVisibility(android.view.View.GONE);
            btnUpdate.setEnabled(true);
            Toast.makeText(this, "Invalid book id", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<ApiResponse<Book>> call = apiService.updateBook(bookId, req, "Bearer " + token);
        call.enqueue(new Callback<ApiResponse<Book>>() {
            @Override
            public void onResponse(Call<ApiResponse<Book>> call, Response<ApiResponse<Book>> response) {
                progressBar.setVisibility(android.view.View.GONE);
                btnUpdate.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(AdminEditBookActivity.this, "Book updated!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_BOOK_UPDATED);
                    finish();
                } else {
                    String msg = "Update failed";
                    try {
                        if (response.errorBody() != null) {
                            msg += ": " + response.errorBody().string();
                        } else if (response.body() != null) {
                            msg += ": " + response.body().getMessage();
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(AdminEditBookActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Book>> call, Throwable t) {
                progressBar.setVisibility(android.view.View.GONE);
                btnUpdate.setEnabled(true);
                Toast.makeText(AdminEditBookActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCategoriesAndSelect(final int currentCategoryId) {
        apiService.getCategoriesRawBody("active").enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!response.isSuccessful() || response.body() == null) return;
                
                try {
                    String responseString = response.body().string();
                    Gson gson = new Gson();
                    JsonObject jsonResponse = JsonParser.parseString(responseString).getAsJsonObject();
                    
                    if (!jsonResponse.has("success") || !jsonResponse.get("success").getAsBoolean()) return;
                    
                    JsonElement dataElement = jsonResponse.get("data");
                    if (dataElement == null || !dataElement.isJsonObject()) return;
                    
                    JsonObject dataObj = dataElement.getAsJsonObject();
                    JsonElement categoriesElement = dataObj.get("categories");
                    
                    List<Category> parsedCategories = new ArrayList<>();
                    
                    if (categoriesElement != null && categoriesElement.isJsonObject()) {
                        JsonObject categoriesObj = categoriesElement.getAsJsonObject();
                        for (String key : categoriesObj.keySet()) {
                            try {
                                JsonObject catJson = categoriesObj.get(key).getAsJsonObject();
                                Category category = gson.fromJson(catJson, Category.class);
                                if (category != null) {
                                    if (category.getId() == 0) {
                                        try {
                                            category.setId(Integer.parseInt(key));
                                        } catch (NumberFormatException e) {
                                            continue;
                                        }
                                    }
                                    if ("active".equals(category.getStatus())) {
                                        parsedCategories.add(category);
                                    }
                                }
                            } catch (Exception e) {
                                // Ignore parse errors
                            }
                        }
                    } else if (categoriesElement != null && categoriesElement.isJsonArray()) {
                        parsedCategories = gson.fromJson(categoriesElement, new TypeToken<List<Category>>(){}.getType());
                        parsedCategories.removeIf(c -> c == null || !"active".equals(c.getStatus()));
                    }
                    
                    categoryNames.clear();
                    categoryIds.clear();
                    for (Category c : parsedCategories) {
                        if (c == null) continue;
                        String name = c.getName();
                        int id;
                        try { 
                            id = c.getId(); 
                        } catch (Exception e) { 
                            continue; 
                        }
                        if (name == null || name.trim().isEmpty()) continue;
                        categoryNames.add(name);
                        categoryIds.add(id);
                    }
                    
                    ArrayAdapter<String> adapter = (ArrayAdapter<String>) spCategory.getAdapter();
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                    int index = categoryIds.indexOf(currentCategoryId);
                    if (index >= 0) {
                        spCategory.setSelection(index);
                    } else if (!categoryIds.isEmpty()) {
                        spCategory.setSelection(0);
                    }
                    
                    updateButtonState();
                } catch (Exception e) {
                    // Ignore parse errors silently
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) { }
        });
    }
}


