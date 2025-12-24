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

public class AdminAddCategoryActivity extends AppCompatActivity {

    public static final int RESULT_CATEGORY_ADDED = 100;

    private ImageView ivBack;
    private TextView tvTitle;
    private EditText etName;
    private EditText etImageUrl;
    private Button btnAdd;
    private ProgressBar progressBar;

    private ApiService apiService;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_add_category);

        apiService = RetrofitClient.getApiService();
        authManager = AuthManager.getInstance(this);

        initViews();
        setupListeners();
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back_add_category_admin);
        tvTitle = findViewById(R.id.tv_title);
        etName = findViewById(R.id.et_name);
        etImageUrl = findViewById(R.id.et_image_url);
        btnAdd = findViewById(R.id.btn_add);
        progressBar = findViewById(R.id.progress_bar);

        if (tvTitle != null) tvTitle.setText("Add category");
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

        btnAdd.setOnClickListener(v -> {
            if (validateInputs()) {
                addCategory();
            }
        });
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
        finish();
    }

    private void updateButtonState() {
        boolean hasName = !etName.getText().toString().trim().isEmpty();
        btnAdd.setEnabled(hasName);
    }

    private boolean validateInputs() {
        if (etName.getText().toString().trim().isEmpty()) {
            etName.setError("Name is required");
            etName.requestFocus();
            return false;
        }
        return true;
    }

    private void addCategory() {
        String token = authManager != null ? authManager.getAccessToken() : null;
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Please login as admin", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnAdd.setEnabled(false);

        String name = etName.getText().toString().trim();
        String imageUrl = etImageUrl.getText().toString().trim();

        CreateCategoryRequest req = new CreateCategoryRequest();
        req.name = name;
        req.image_url = imageUrl.isEmpty() ? "" : imageUrl;
        req.status = "active";

        apiService.createCategory(req, "Bearer " + token).enqueue(new Callback<ApiResponse<Category>>() {
            @Override
            public void onResponse(Call<ApiResponse<Category>> call, Response<ApiResponse<Category>> response) {
                progressBar.setVisibility(View.GONE);
                btnAdd.setEnabled(true);
                
                if (response.isSuccessful()) {
                    if (response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(AdminAddCategoryActivity.this, "Category added successfully!", Toast.LENGTH_SHORT).show();
                        Intent resultIntent = new Intent();
                        setResult(RESULT_CATEGORY_ADDED, resultIntent);
                        finish();
                    } else {
                        String msg = "Add failed";
                        if (response.body() != null && response.body().getMessage() != null) {
                            msg = response.body().getMessage();
                        }
                        Toast.makeText(AdminAddCategoryActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String msg = "Add failed (HTTP " + response.code() + ")";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            android.util.Log.e("AdminAddCategory", "Error response: " + errorBody);
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
                        android.util.Log.e("AdminAddCategory", "Error parsing: " + e.getMessage());
                    }
                    Toast.makeText(AdminAddCategoryActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Category>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnAdd.setEnabled(true);
                android.util.Log.e("AdminAddCategory", "Network error: " + t.getMessage(), t);
                String errorMsg = "Network error";
                if (t.getMessage() != null) {
                    errorMsg += ": " + t.getMessage();
                }
                Toast.makeText(AdminAddCategoryActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}

