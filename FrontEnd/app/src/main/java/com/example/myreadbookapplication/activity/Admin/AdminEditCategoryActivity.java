package com.example.myreadbookapplication.activity.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myreadbookapplication.R;
import com.example.myreadbookapplication.model.ApiResponse;
import com.example.myreadbookapplication.model.Category;
import com.example.myreadbookapplication.model.CreateCategoryRequest;
import com.example.myreadbookapplication.network.ApiService;
import com.example.myreadbookapplication.network.RetrofitClient;
import com.example.myreadbookapplication.utils.AuthManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminEditCategoryActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORY = "category";
    public static final int RESULT_CATEGORY_UPDATED = 101;

    private ImageView ivBack;
    private TextView tvTitle;
    private EditText etName;
    private EditText etImageUrl;
    private Button btnUpdate;
    private ProgressBar progressBar;

    private ApiService apiService;
    private AuthManager authManager;
    private Category currentCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_add_category);

        apiService = RetrofitClient.getApiService();
        authManager = AuthManager.getInstance(this);

        initViews();
        setupListeners();

        Intent intent = getIntent();
        if (intent == null || !intent.hasExtra(EXTRA_CATEGORY)) {
            Toast.makeText(this, "Missing category to edit", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentCategory = (Category) intent.getSerializableExtra(EXTRA_CATEGORY);
        btnUpdate.setText("UPDATE");
        if (tvTitle != null) tvTitle.setText("Edit category");
        populateFields();
        updateButtonState();
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back_add_category_admin);
        tvTitle = findViewById(R.id.tv_title);
        etName = findViewById(R.id.et_name);
        etImageUrl = findViewById(R.id.et_image_url);
        btnUpdate = findViewById(R.id.btn_add);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupListeners() {
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> onBackPressed());
        }

        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { updateButtonState(); }
        };
        etName.addTextChangedListener(watcher);
        etImageUrl.addTextChangedListener(watcher);

        btnUpdate.setOnClickListener(v -> {
            if (validateInputs()) {
                updateCategory();
            }
        });
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
        finish();
    }

    private void populateFields() {
        if (currentCategory == null) return;
        etName.setText(currentCategory.getName());
        etImageUrl.setText(currentCategory.getImageUrl());
    }

    private void updateButtonState() {
        boolean hasName = !etName.getText().toString().trim().isEmpty();
        btnUpdate.setEnabled(hasName);
    }

    private boolean validateInputs() {
        if (etName.getText().toString().trim().isEmpty()) {
            etName.setError("Name is required");
            etName.requestFocus();
            return false;
        }
        return true;
    }

    private void updateCategory() {
        String token = authManager != null ? authManager.getAccessToken() : null;
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Please login as admin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentCategory == null) {
            Toast.makeText(this, "Invalid category", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnUpdate.setEnabled(false);

        String name = etName.getText().toString().trim();
        String imageUrl = etImageUrl.getText().toString().trim();

        CreateCategoryRequest req = new CreateCategoryRequest();
        req.name = name;
        req.image_url = imageUrl.isEmpty() ? "" : imageUrl;

        int categoryId = currentCategory.getId();
        android.util.Log.d("AdminEditCategory", "Updating category - ID: " + categoryId + ", Name: " + name);
        
        apiService.updateCategory(categoryId, req, "Bearer " + token).enqueue(new Callback<ApiResponse<Category>>() {
            @Override
            public void onResponse(Call<ApiResponse<Category>> call, Response<ApiResponse<Category>> response) {
                progressBar.setVisibility(View.GONE);
                btnUpdate.setEnabled(true);
                
                if (response.isSuccessful()) {
                    if (response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(AdminEditCategoryActivity.this, "Category updated successfully!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_CATEGORY_UPDATED);
                        finish();
                    } else {
                        String msg = "Update failed";
                        if (response.body() != null && response.body().getMessage() != null) {
                            msg = response.body().getMessage();
                        }
                        Toast.makeText(AdminEditCategoryActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String msg = "Update failed (HTTP " + response.code() + ")";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            android.util.Log.e("AdminEditCategory", "Error response: " + errorBody);
                            // Try to extract message from JSON error
                            if (errorBody.contains("message")) {
                                int msgStart = errorBody.indexOf("\"message\"");
                                if (msgStart > 0) {
                                    int colonIdx = errorBody.indexOf(":", msgStart);
                                    int quoteStart = errorBody.indexOf("\"", colonIdx);
                                    int quoteEnd = errorBody.indexOf("\"", quoteStart + 1);
                                    if (quoteStart > 0 && quoteEnd > quoteStart) {
                                        msg = errorBody.substring(quoteStart + 1, quoteEnd);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        android.util.Log.e("AdminEditCategory", "Error parsing: " + e.getMessage());
                    }
                    Toast.makeText(AdminEditCategoryActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Category>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnUpdate.setEnabled(true);
                android.util.Log.e("AdminEditCategory", "Network error: " + t.getMessage(), t);
                String errorMsg = "Network error";
                if (t.getMessage() != null) {
                    errorMsg += ": " + t.getMessage();
                }
                Toast.makeText(AdminEditCategoryActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}

