package com.example.myreadbookapplication.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.myreadbookapplication.R;
import com.example.myreadbookapplication.activity.User.SignInActivity;
import com.example.myreadbookapplication.model.LogoutRequest;
import com.example.myreadbookapplication.network.ApiService;
import com.example.myreadbookapplication.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LogoutManager {
    private static final String TAG = "LogoutManager";
    
    private Context context;
    private ApiService apiService;
    private AuthManager authManager;
    
    public LogoutManager(Context context) {
        this.context = context;
        this.apiService = RetrofitClient.getApiService();
        this.authManager = AuthManager.getInstance(context);
    }

    /**
     * Hiển thị dialog xác nhận và đăng xuất nếu người dùng chọn "Đăng xuất"
     */
    public void confirmLogout() {
        confirmLogout(null);
    }

    /**
     * Hiển thị dialog xác nhận và đăng xuất nếu người dùng chọn "Đăng xuất" với callback
     */
    public void confirmLogout(LogoutCallback callback) {
        Activity activity = context instanceof Activity ? (Activity) context : null;
        if (activity == null || activity.isFinishing()) {
            logout(callback);
            return;
        }

        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_confirm_logout, null);
        TextView btnStay = dialogView.findViewById(R.id.btn_logout_cancel);
        TextView btnLogout = dialogView.findViewById(R.id.btn_logout_confirm);

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        btnStay.setOnClickListener(v -> {
            dialog.dismiss();
            if (callback != null) {
                callback.onLogoutCancelled();
            }
        });

        btnLogout.setOnClickListener(v -> {
            dialog.dismiss();
            logout(callback);
        });

        dialog.show();
    }
    
    /**
     * Thực hiện đăng xuất
     */
    public void logout() {
        Log.d(TAG, "logout() method called");
        String userEmail = authManager.getUserEmail();
        
        if (userEmail == null || userEmail.isEmpty()) {
            Log.w(TAG, "No user email found, performing local logout only");
            Toast.makeText(context, "No account information found. Signing out locally.", Toast.LENGTH_SHORT).show();
            performLocalLogout();
            return;
        }
        
        Log.d(TAG, "Starting logout process for user: " + userEmail);
        Toast.makeText(context, "Signing out " + userEmail + "...", Toast.LENGTH_SHORT).show();
        
        // Tạo request logout
        LogoutRequest logoutRequest = new LogoutRequest(userEmail);
        
        // Gọi API logout
        Call<com.example.myreadbookapplication.model.ApiResponse> call = apiService.logout(logoutRequest);
        call.enqueue(new Callback<com.example.myreadbookapplication.model.ApiResponse>() {
            @Override
            public void onResponse(Call<com.example.myreadbookapplication.model.ApiResponse> call, 
                                 Response<com.example.myreadbookapplication.model.ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess()) {
                        Log.d(TAG, "Logout API call successful");
                        performLocalLogout();
                    } else {
                        Log.w(TAG, "Logout API returned success=false: " + response.body().getMessage());
                        // Vẫn thực hiện local logout ngay cả khi API trả về lỗi
                        performLocalLogout();
                    }
                } else {
                    Log.e(TAG, "Logout API call failed: " + response.code());
                    // Vẫn thực hiện local logout ngay cả khi API call thất bại
                    performLocalLogout();
                }
            }
            
            @Override
            public void onFailure(Call<com.example.myreadbookapplication.model.ApiResponse> call, Throwable t) {
                Log.e(TAG, "Logout API call failed with exception: " + t.getMessage());
                // Vẫn thực hiện local logout ngay cả khi có exception
                performLocalLogout();
            }
        });
    }
    
    /**
     * Thực hiện đăng xuất local (xóa dữ liệu và chuyển màn hình)
     */
    private void performLocalLogout() {
        Log.d(TAG, "Performing local logout");
        Toast.makeText(context, "Signing out locally...", Toast.LENGTH_SHORT).show();
        
        // Xóa tất cả dữ liệu authentication
        authManager.logout();
        
        // Hiển thị thông báo
        Toast.makeText(context, "You have signed out successfully.", Toast.LENGTH_SHORT).show();
        
        // Chuyển về màn hình đăng nhập
        navigateToSignIn();
    }
    
    /**
     * Chuyển về màn hình đăng nhập
     */
    private void navigateToSignIn() {
        Log.d(TAG, "Navigating to SignInActivity");
        
        Intent intent = new Intent(context, SignInActivity.class);
        // Sử dụng FLAG_ACTIVITY_NEW_TASK và FLAG_ACTIVITY_CLEAR_TOP để clear stack
        // FLAG_ACTIVITY_NEW_TASK: Tạo task mới hoặc sử dụng task hiện có
        // FLAG_ACTIVITY_CLEAR_TOP: Nếu SignInActivity đã tồn tại trong stack, clear tất cả activities phía trên nó
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
        
        // Finish activity hiện tại nếu context là Activity
        // Điều này đảm bảo activity hiện tại được đóng ngay lập tức
        if (context instanceof Activity) {
            ((Activity) context).finish();
            Log.d(TAG, "Current activity finished");
        }
        
        Log.d(TAG, "SignInActivity started");
    }
    
    /**
     * Đăng xuất với callback
     */
    public void logout(LogoutCallback callback) {
        String userEmail = authManager.getUserEmail();
        
        if (userEmail == null || userEmail.isEmpty()) {
            Log.w(TAG, "No user email found, performing local logout only");
            performLocalLogoutWithCallback(callback);
            return;
        }
        
        Log.d(TAG, "Starting logout process for user: " + userEmail);
        
        // Tạo request logout
        LogoutRequest logoutRequest = new LogoutRequest(userEmail);
        
        // Gọi API logout
        Call<com.example.myreadbookapplication.model.ApiResponse> call = apiService.logout(logoutRequest);
        call.enqueue(new Callback<com.example.myreadbookapplication.model.ApiResponse>() {
            @Override
            public void onResponse(Call<com.example.myreadbookapplication.model.ApiResponse> call, 
                                 Response<com.example.myreadbookapplication.model.ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess()) {
                        Log.d(TAG, "Logout API call successful");
                        performLocalLogoutWithCallback(callback);
                    } else {
                        Log.w(TAG, "Logout API returned success=false: " + response.body().getMessage());
                        performLocalLogoutWithCallback(callback);
                    }
                } else {
                    Log.e(TAG, "Logout API call failed: " + response.code());
                    performLocalLogoutWithCallback(callback);
                }
            }
            
            @Override
            public void onFailure(Call<com.example.myreadbookapplication.model.ApiResponse> call, Throwable t) {
                Log.e(TAG, "Logout API call failed with exception: " + t.getMessage());
                performLocalLogoutWithCallback(callback);
            }
        });
    }
    
    /**
     * Thực hiện đăng xuất local với callback
     */
    private void performLocalLogoutWithCallback(LogoutCallback callback) {
        Log.d(TAG, "Performing local logout with callback");
        Toast.makeText(context, "Signing out locally...", Toast.LENGTH_SHORT).show();
        
        // Xóa tất cả dữ liệu authentication
        authManager.logout();
        
        // Hiển thị thông báo
        Toast.makeText(context, "You have signed out successfully.", Toast.LENGTH_SHORT).show();
        
        // Chuyển về màn hình đăng nhập
        navigateToSignIn();
        
        if (callback != null) {
            callback.onLogoutSuccess();
        }
    }
    
    /**
     * Interface callback cho logout
     */
    public interface LogoutCallback {
        void onLogoutSuccess();

        default void onLogoutCancelled() {}
    }
}
