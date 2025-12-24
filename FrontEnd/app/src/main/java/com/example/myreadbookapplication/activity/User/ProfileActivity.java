package com.example.myreadbookapplication.activity.User;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myreadbookapplication.R;
import com.example.myreadbookapplication.model.ApiResponse;
import com.example.myreadbookapplication.model.User;
import com.example.myreadbookapplication.network.ApiService;
import com.example.myreadbookapplication.network.RetrofitClient;
import com.example.myreadbookapplication.utils.AuthManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    
    private ImageView ivBack;
    private TextView tvUserName;
    private TextView tvUserEmail;
    private TextView tvUserPhone;
    private TextView tvAccountCreated;
    private TextView btnEdit;
    private ProgressBar progressBar;
    
    private ApiService apiService;
    private User currentUser;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        // Ánh xạ các view
        ivBack = findViewById(R.id.iv_back);
        tvUserName = findViewById(R.id.tv_user_name);
        tvUserEmail = findViewById(R.id.tv_user_email);
        tvUserPhone = findViewById(R.id.tv_user_phone);
        tvAccountCreated = findViewById(R.id.tv_account_created);
        btnEdit = findViewById(R.id.btn_edit);
        progressBar = findViewById(R.id.progress_bar);

        // Initialize API service
        apiService = RetrofitClient.getApiService();
        authManager = AuthManager.getInstance(this);

        // Load thông tin user từ API
        loadUserInfo();

        // Bắt sự kiện click
        ivBack.setOnClickListener(v -> {
            finish(); // Quay lại màn hình trước
        });

        btnEdit.setOnClickListener(v -> {
            // Chuyển đến màn hình Edit Profile
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });
    }

    private void loadUserInfo() {
        String userId = authManager.getUserId();
        String token = authManager.getAccessToken();

        if (userId == null || token == null || token.isEmpty()) {
            Toast.makeText(this, "Bạn cần đăng nhập để xem thông tin cá nhân", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Show loading
        progressBar.setVisibility(View.VISIBLE);

        // Gọi API để lấy thông tin user
        Call<ApiResponse<User>> call = apiService.getUserProfile(userId, "Bearer " + token);
        call.enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    currentUser = response.body().getData();
                    displayUserInfo(currentUser);
                } else {
                    Log.e(TAG, "Failed to load user info: " + response.code());
                    Toast.makeText(ProfileActivity.this, "Không thể tải thông tin người dùng", Toast.LENGTH_SHORT).show();
                    // Fallback to SharedPreferences
                    loadUserInfoFromPrefs();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error loading user info: " + t.getMessage());
                Toast.makeText(ProfileActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
                // Fallback to SharedPreferences
                loadUserInfoFromPrefs();
            }
        });
    }

    private void displayUserInfo(User user) {
        if (user == null) return;

        tvUserName.setText(user.getFullName() != null ? user.getFullName() : "Chưa cập nhật");
        tvUserEmail.setText(user.getEmail() != null ? user.getEmail() : "Chưa cập nhật");
        tvUserPhone.setText(user.getPhoneNumber() != null ? user.getPhoneNumber() : "Chưa cập nhật");
        
        // Format created date
        if (user.getCreatedAt() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String createdDate = sdf.format(new Date(user.getCreatedAt()));
            tvAccountCreated.setText(createdDate);
        } else {
            tvAccountCreated.setText("Chưa xác định");
        }
    }

    private void loadUserInfoFromPrefs() {
        String email = authManager.getUserEmail();
        String name = authManager.getUserFullName();
        String phone = authManager.getUserPhone();

        // Hiển thị thông tin từ AuthManager
        tvUserEmail.setText(email != null ? email : "Chưa cập nhật");
        tvUserName.setText(name != null ? name : "Chưa cập nhật");
        tvUserPhone.setText(phone != null ? phone : "Chưa cập nhật");
        tvAccountCreated.setText("Chưa xác định");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload user info when returning from EditProfile
        loadUserInfo();
    }
}
