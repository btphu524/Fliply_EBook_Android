package com.example.myreadbookapplication.activity.User;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myreadbookapplication.R;
import com.example.myreadbookapplication.activity.Admin.AdminMainActivity;
import com.example.myreadbookapplication.model.ApiResponse;
import com.example.myreadbookapplication.model.User;
import com.example.myreadbookapplication.network.ApiService;
import com.example.myreadbookapplication.network.RetrofitClient;
import com.example.myreadbookapplication.utils.AuthManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IntroActivity extends AppCompatActivity {

    private static final String TAG = "IntroActivity";
    private static final int SPLASH_TIME_OUT = 2000; // 2 giây
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_intro);

        // Initialize RetrofitClient với context
        RetrofitClient.init(this);
        authManager = AuthManager.getInstance(this);

        // Kiểm tra auto-login sau khi delay
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                checkAutoLogin();
            }
        }, SPLASH_TIME_OUT);
    }

    /**
     * Kiểm tra và tự động đăng nhập nếu user đã đăng nhập trước đó
     */
    private void checkAutoLogin() {
        // Kiểm tra xem user đã đăng nhập chưa
        if (authManager.isLoggedIn() && authManager.getAccessToken() != null) {
            Log.d(TAG, "User is logged in, validating token...");
            // Validate token với backend để đảm bảo token còn hợp lệ
            validateTokenAndNavigate();
        } else {
            Log.d(TAG, "User is not logged in, redirecting to SignInActivity");
            // Chưa đăng nhập, chuyển đến SignInActivity
            navigateToSignIn();
        }
    }

    /**
     * Validate token với backend và navigate đến màn hình phù hợp
     */
    private void validateTokenAndNavigate() {
        String userId = authManager.getUserId();
        String accessToken = authManager.getAccessToken();
        
        if (userId == null || userId.isEmpty() || accessToken == null || accessToken.isEmpty()) {
            Log.w(TAG, "Missing userId or accessToken, redirecting to SignInActivity");
            navigateToSignIn();
            return;
        }

        // Gọi API để validate token (sử dụng getUserProfile)
        ApiService apiService = RetrofitClient.getApiService();
        Call<ApiResponse<User>> call = apiService.getUserProfile(userId, "Bearer " + accessToken);
        
        call.enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // Token hợp lệ, tự động đăng nhập
                    Log.d(TAG, "Token is valid, auto-login successful");
                    User user = response.body().getData();
                    if (user != null) {
                        // Cập nhật thông tin user nếu cần
                        String role = authManager.getUserRole();
                        navigateToMainScreen(role);
                    } else {
                        navigateToSignIn();
                    }
                } else {
                    // Token không hợp lệ hoặc đã hết hạn
                    Log.w(TAG, "Token validation failed, redirecting to SignInActivity");
                    // Clear invalid token
                    authManager.logout();
                    navigateToSignIn();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                // Network error - vẫn cho phép auto-login với token local
                // Hoặc có thể navigate đến SignInActivity nếu muốn chắc chắn
                Log.w(TAG, "Token validation network error: " + t.getMessage());
                // Vẫn cho phép auto-login với token local (offline support)
                String role = authManager.getUserRole();
                navigateToMainScreen(role);
            }
        });
    }

    /**
     * Navigate đến màn hình chính dựa trên role
     */
    private void navigateToMainScreen(String role) {
        Intent intent;
        if ("admin".equalsIgnoreCase(role)) {
            intent = new Intent(IntroActivity.this, AdminMainActivity.class);
            Log.d(TAG, "Auto-login: Redirecting to AdminMainActivity");
        } else {
            intent = new Intent(IntroActivity.this, HomeActivity.class);
            Log.d(TAG, "Auto-login: Redirecting to HomeActivity");
        }
        startActivity(intent);
        finish(); // Đóng IntroActivity để không quay lại khi bấm Back
    }

    /**
     * Navigate đến SignInActivity
     */
    private void navigateToSignIn() {
        Intent intent = new Intent(IntroActivity.this, SignInActivity.class);
        startActivity(intent);
        finish(); // Đóng IntroActivity để không quay lại khi bấm Back
    }
}