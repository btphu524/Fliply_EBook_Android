package com.example.myreadbookapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.myreadbookapplication.model.User;

public class AuthManager {
    private static final String PREFS_NAME = "auth_prefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_FULL_NAME = "user_full_name";
    private static final String KEY_USER_PHONE = "user_phone";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    
    private static AuthManager instance;
    private SharedPreferences prefs;
    private Context context;
    
    private AuthManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    public static synchronized AuthManager getInstance(Context context) {
        if (instance == null) {
            instance = new AuthManager(context);
        }
        return instance;
    }
    
    /**
     * Lưu thông tin đăng nhập
     */
    public void saveLoginData(String accessToken, String refreshToken, String email, String userId, String fullName) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_ACCESS_TOKEN, accessToken);
        editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_FULL_NAME, fullName);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
        
        Log.d("AuthManager", "Login data saved for user: " + email);
    }
    
    /**
     * Lưu thông tin đăng nhập với role
     */
    public void saveLoginData(String accessToken, String refreshToken, String email, String userId, String fullName, String role) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_ACCESS_TOKEN, accessToken);
        editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_FULL_NAME, fullName);
        editor.putString(KEY_USER_ROLE, role);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
        
        Log.d("AuthManager", "Login data saved for user: " + email + ", role: " + role);
    }
    
    /**
     * Lưu thông tin user
     */
    public void saveUserData(User user) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.putString(KEY_USER_ID, user.getId());
        editor.putString(KEY_USER_FULL_NAME, user.getFullName());
        if (user.getPhoneNumber() != null) {
            editor.putString(KEY_USER_PHONE, user.getPhoneNumber());
        }
        editor.apply();
        
        Log.d("AuthManager", "User data saved: " + user.getEmail() + ", phone: " + user.getPhoneNumber());
    }
    
    /**
     * Lấy số điện thoại người dùng
     */
    public String getUserPhone() {
        return prefs.getString(KEY_USER_PHONE, null);
    }
    
    /**
     * Kiểm tra trạng thái đăng nhập
     */
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) && 
               getAccessToken() != null && 
               !getAccessToken().isEmpty();
    }
    
    /**
     * Lấy access token
     */
    public String getAccessToken() {
        return prefs.getString(KEY_ACCESS_TOKEN, null);
    }
    
    /**
     * Lấy refresh token
     */
    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH_TOKEN, null);
    }
    
    /**
     * Lấy email người dùng
     */
    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, null);
    }
    
    /**
     * Lấy ID người dùng
     */
    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }
    
    /**
     * Lấy tên đầy đủ người dùng
     */
    public String getUserFullName() {
        return prefs.getString(KEY_USER_FULL_NAME, null);
    }
    
    /**
     * Lấy role người dùng
     */
    public String getUserRole() {
        return prefs.getString(KEY_USER_ROLE, "user");
    }
    
    /**
     * Kiểm tra có phải admin không
     */
    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(getUserRole());
    }
    
    /**
     * Tạo Authorization header
     */
    public String getAuthorizationHeader() {
        String token = getAccessToken();
        return token != null ? "Bearer " + token : null;
    }
    
    /**
     * Đăng xuất - xóa tất cả dữ liệu
     */
    public void logout() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_ACCESS_TOKEN);
        editor.remove(KEY_REFRESH_TOKEN);
        editor.remove(KEY_USER_EMAIL);
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_USER_FULL_NAME);
        editor.remove(KEY_USER_PHONE);
        editor.remove(KEY_USER_ROLE);
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.apply();
        
        // Clear favorite_books trong app_prefs để tránh hiển thị sai icon yêu thích cho user mới
        SharedPreferences appPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        appPrefs.edit().remove("favorite_books").apply();
        
        Log.d("AuthManager", "User logged out, all data cleared including favorites");
    }
    
    /**
     * Cập nhật access token
     */
    public void updateAccessToken(String newAccessToken) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_ACCESS_TOKEN, newAccessToken);
        editor.apply();
        
        Log.d("AuthManager", "Access token updated");
    }
    
    /**
     * Kiểm tra token có hợp lệ không
     */
    public boolean isTokenValid() {
        String token = getAccessToken();
        if (token == null || token.isEmpty()) {
            return false;
        }
        
        // Có thể thêm logic kiểm tra JWT expiry ở đây
        // Hiện tại chỉ kiểm tra token có tồn tại
        return true;
    }
    
    /**
     * Lấy thông tin user hiện tại
     */
    public User getCurrentUser() {
        if (!isLoggedIn()) {
            return null;
        }
        
        User user = new User();
        user.setId(getUserId());
        user.setEmail(getUserEmail());
        user.setFullName(getUserFullName());
        return user;
    }
}
