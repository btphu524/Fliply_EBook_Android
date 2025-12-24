package com.example.myreadbookapplication.activity.User;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myreadbookapplication.R;
import com.example.myreadbookapplication.model.ApiResponse;
import com.example.myreadbookapplication.model.UpdateUserRequest;
import com.example.myreadbookapplication.model.User;
import com.example.myreadbookapplication.network.ApiService;
import com.example.myreadbookapplication.network.RetrofitClient;
import com.example.myreadbookapplication.utils.AuthManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";
    
    private ImageView ivBack;
    private EditText etName;
    private EditText etEmail;
    private EditText etPhone;
    private TextView btnCancel;
    private TextView btnSave;
    private ProgressBar progressBar;
    
    private ApiService apiService;
    private User currentUser;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);

        // Ánh xạ các view
        ivBack = findViewById(R.id.iv_back);
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        btnCancel = findViewById(R.id.btn_cancel);
        btnSave = findViewById(R.id.btn_save);
        progressBar = findViewById(R.id.progress_bar);

        // Initialize API service
        apiService = RetrofitClient.getApiService();
        authManager = AuthManager.getInstance(this);

        // Make email field non-editable (backend doesn't allow email updates)
        etEmail.setEnabled(false);
        etEmail.setFocusable(false);

        // Load thông tin user hiện tại
        loadCurrentUserInfo();

        // Bắt sự kiện click
        ivBack.setOnClickListener(v -> {
            finish(); // Quay lại màn hình trước
        });

        btnCancel.setOnClickListener(v -> {
            finish(); // Quay lại màn hình trước
        });

        btnSave.setOnClickListener(v -> {
            // Validate input
            if (validateInput()) {
                // Lưu thông tin và quay lại ProfileActivity
                saveUserInfo();
            }
        });
    }

    private void loadCurrentUserInfo() {
        String userId = authManager.getUserId();
        String token = authManager.getAccessToken();

        if (userId == null || token == null || token.isEmpty()) {
            Toast.makeText(this, "Bạn cần đăng nhập để chỉnh sửa thông tin", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Show loading
        progressBar.setVisibility(View.VISIBLE);

        // Load current user data from API
        Call<ApiResponse<User>> call = apiService.getUserProfile(userId, "Bearer " + token);
        call.enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    currentUser = response.body().getData();
                    // Populate form with current user data
                    etName.setText(currentUser.getFullName() != null ? currentUser.getFullName() : "");
                    etEmail.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");
                    etPhone.setText(currentUser.getPhoneNumber() != null ? currentUser.getPhoneNumber() : "");
                } else {
                    Log.e(TAG, "Failed to load user info: " + response.code());
                    Toast.makeText(EditProfileActivity.this, "Không thể tải thông tin người dùng", Toast.LENGTH_SHORT).show();
                    // Fallback to SharedPreferences
                    loadUserInfoFromPrefs();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error loading user info: " + t.getMessage());
                Toast.makeText(EditProfileActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
                // Fallback to SharedPreferences
                loadUserInfoFromPrefs();
            }
        });
    }

    private void loadUserInfoFromPrefs() {
        String email = authManager.getUserEmail();
        String name = authManager.getUserFullName();
        
        // Hiển thị thông tin từ AuthManager
        etName.setText(name != null ? name : "");
        etEmail.setText(email != null ? email : "");
        // Phone number is not stored in AuthManager, so leave it empty
    }

    private boolean validateInput() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên của bạn", Toast.LENGTH_SHORT).show();
            etName.requestFocus();
            return false;
        }

        if (phone.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số điện thoại", Toast.LENGTH_SHORT).show();
            etPhone.requestFocus();
            return false;
        }

        // Validate phone number format (10-11 digits)
        if (!phone.matches("^[0-9]{10,11}$")) {
            Toast.makeText(this, "Số điện thoại phải có 10-11 chữ số", Toast.LENGTH_SHORT).show();
            etPhone.requestFocus();
            return false;
        }

        return true;
    }

    private void saveUserInfo() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        String userId = authManager.getUserId();
        String token = authManager.getAccessToken();

        if (userId == null || token == null || token.isEmpty()) {
            Toast.makeText(this, "Bạn cần đăng nhập để cập nhật thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading
        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        // Create update request - only send name and phoneNumber (not email)
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setFullName(name);
        updateRequest.setPhoneNumber(phone);

        // Call API to update user profile
        Call<ApiResponse<User>> call = apiService.updateUserProfile(userId, updateRequest, "Bearer " + token);
        call.enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                progressBar.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    User updatedUser = response.body().getData();
                    
                    // Update AuthManager with new data
                    authManager.saveUserData(updatedUser);
                    
                    Toast.makeText(EditProfileActivity.this, "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Log.e(TAG, "Failed to update user profile: " + response.code());
                    String errorMessage = "Không thể cập nhật thông tin";
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBody);
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading error body", e);
                        }
                    }
                    Toast.makeText(EditProfileActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                Log.e(TAG, "Error updating user profile: " + t.getMessage());
                Toast.makeText(EditProfileActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
