package com.example.myreadbookapplication.activity.User;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myreadbookapplication.R;
import com.example.myreadbookapplication.activity.Admin.AdminMainActivity;
import com.example.myreadbookapplication.model.ApiResponse;
import com.example.myreadbookapplication.model.SignInRequest;
import com.example.myreadbookapplication.network.RetrofitClient;
import com.example.myreadbookapplication.utils.AuthManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignInActivity extends AppCompatActivity {

    private TextView tvSignUp;
    private LinearLayout btnSignInHomPage;
    private EditText etEmailSignIn;
    private EditText etPasswordSignIn;
    private TextView tvForgotPassword;
    private AuthManager authManager;
    private ImageView ivPasswordEye;
    private boolean isPasswordVisible = false;
    
    // Rate limiting protection
    private retrofit2.Call<ApiResponse> currentLoginCall;
    private boolean isLoginInProgress = false;
    private long lastLoginAttemptTime = 0;
    private static final long MIN_LOGIN_INTERVAL = 2000; // 2 seconds minimum between login attempts
    private static final long RATE_LIMIT_COOLDOWN = 60000; // 60 seconds cooldown after rate limit error

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);

        // ánh xạ
        tvSignUp = findViewById(R.id.tv_sign_up);
        btnSignInHomPage = findViewById(R.id.btn_sign_in);
        etEmailSignIn = findViewById(R.id.et_email_sign_in);
        etPasswordSignIn = findViewById(R.id.et_password_sign_in);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        ivPasswordEye = findViewById(R.id.iv_password_eye);
        
        authManager = AuthManager.getInstance(this);
        
        // Setup password eye icon
        ivPasswordEye.setOnClickListener(v -> togglePasswordVisibility());

        // bắt sự kiện và xử lý
        btnSignInHomPage.setOnClickListener(v -> performSignIn());

        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignInActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });
    }
    
    private void performSignIn() {
        // Lấy dữ liệu từ edit text
        String emailSignIn = etEmailSignIn.getText().toString().trim();
        String passwordSignIn = etPasswordSignIn.getText().toString().trim();

        // Validation
        if (emailSignIn.isEmpty() || passwordSignIn.isEmpty()) {
            Toast.makeText(SignInActivity.this, "You need fill in all of the fields", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailSignIn).matches()) {
            Toast.makeText(SignInActivity.this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Check if login is already in progress
        if (isLoginInProgress) {
            Toast.makeText(SignInActivity.this, "Login request is already in progress. Please wait...", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Rate limiting: Check minimum interval between login attempts
        long currentTime = System.currentTimeMillis();
        long timeSinceLastAttempt = currentTime - lastLoginAttemptTime;
        
        if (timeSinceLastAttempt < MIN_LOGIN_INTERVAL) {
            long remainingTime = (MIN_LOGIN_INTERVAL - timeSinceLastAttempt) / 1000;
            Toast.makeText(SignInActivity.this, "Please wait " + remainingTime + " seconds before trying again", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Cancel previous request if exists
        if (currentLoginCall != null && !currentLoginCall.isCanceled()) {
            currentLoginCall.cancel();
        }
        
        // Update state
        isLoginInProgress = true;
        lastLoginAttemptTime = currentTime;
        btnSignInHomPage.setEnabled(false);
        btnSignInHomPage.setAlpha(0.6f); // Visual feedback
        Toast.makeText(SignInActivity.this, "Signing in...", Toast.LENGTH_SHORT).show();

        // Create request and call API
        SignInRequest signInRequest = new SignInRequest(emailSignIn, passwordSignIn);
        currentLoginCall = RetrofitClient.getApiService().signIn(signInRequest);
        currentLoginCall.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    // Reset state
                    isLoginInProgress = false;
                    btnSignInHomPage.setEnabled(true);
                    btnSignInHomPage.setAlpha(1.0f);
                    currentLoginCall = null;
                    
                    if(response.isSuccessful() && response.body() != null){
                        ApiResponse apiResponse = response.body();
                        Toast.makeText(SignInActivity.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        if(apiResponse.isSuccess()){
                            //lay va luu email vao sharedPreference
                            String userEmail = "";
                            Object dataObj = apiResponse.getData();
                            if(dataObj != null){
                                try{
                                    Gson gson = new Gson();
                                    String dataJson = gson.toJson(dataObj); //convert data sang json string
                                    JsonObject jsonData = JsonParser.parseString(dataJson).getAsJsonObject();
                                    JsonObject userJson = jsonData.getAsJsonObject("user");
                                    if(userJson != null){
                                        userEmail = userJson.get("email").getAsString();
                                    }
                                }catch (Exception e){
                                    Log.e(TAG, "Error parsing data", e);
                                }
                            }
                            Log.d(TAG, "Login success, email: " + userEmail);
                            
                            // Lưu thông tin đăng nhập vào AuthManager
                            String userId = "";
                            String accessToken = "";
                            String refreshToken = "";
                            String fullName = "";
                            String role = "user"; // Default role
                            
                            try{
                                Gson gson = new Gson();
                                String dataJson = gson.toJson(apiResponse.getData());
                                JsonObject jsonData = JsonParser.parseString(dataJson).getAsJsonObject();
                                JsonObject user = jsonData.getAsJsonObject("user");
                                
                                if (user != null && user.get("_id") != null) {
                                    try {
                                        if (user.get("_id").isJsonPrimitive() && user.get("_id").getAsJsonPrimitive().isNumber()) {
                                            long idNum = user.get("_id").getAsLong();
                                            userId = String.valueOf(idNum);
                                        } else {
                                            userId = user.get("_id").getAsString();
                                        }
                                    } catch (Exception ignore) {
                                        userId = user.get("_id").getAsString();
                                    }
                                }
                                
                                if (user != null && user.get("fullName") != null) {
                                    fullName = user.get("fullName").getAsString();
                                }
                                
                                if (user != null && user.get("role") != null) {
                                    role = user.get("role").getAsString();
                                }
                                
                                if (jsonData.get("accessToken") != null) {
                                    accessToken = jsonData.get("accessToken").getAsString();
                                }
                                
                                if (jsonData.get("refreshToken") != null) {
                                    refreshToken = jsonData.get("refreshToken").getAsString();
                                }
                            }catch (Exception e){
                                Log.e(TAG, "Parse tokens failed", e);
                            }
                            
                            // Sử dụng AuthManager để lưu thông tin với role
                            authManager.saveLoginData(accessToken, refreshToken, userEmail, userId, fullName, role);
                            
                            // Debug log
                            Log.d(TAG, "AuthManager saveLoginData called");
                            Log.d(TAG, "Saved email: " + userEmail);
                            Log.d(TAG, "Saved userId: " + userId);
                            Log.d(TAG, "AuthManager isLoggedIn after save: " + authManager.isLoggedIn());

                            // Seed local favorite ids from backend user.favoriteBooks for correct icon state
                            try {
                                Gson gson = new Gson();
                                String dataJson = gson.toJson(apiResponse.getData());
                                JsonObject jsonData = JsonParser.parseString(dataJson).getAsJsonObject();
                                JsonObject userObj = jsonData.getAsJsonObject("user");
                                SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
                                
                                if (userObj != null && userObj.get("favoriteBooks") != null && userObj.get("favoriteBooks").isJsonArray()) {
                                    String favoritesJson = userObj.get("favoriteBooks").toString();
                                    // Luôn cập nhật favorite_books từ backend để đảm bảo đồng bộ
                                    prefs.edit().putString("favorite_books", favoritesJson).apply();
                                    Log.d(TAG, "Favorites synced from backend: " + favoritesJson);
                                } else {
                                    // Nếu không có favoriteBooks hoặc null, set về array rỗng để tránh hiển thị sai icon
                                    prefs.edit().putString("favorite_books", "[]").apply();
                                    Log.d(TAG, "No favorites from backend, set to empty array");
                                }
                            } catch (Exception e2) {
                                Log.w(TAG, "Unable to seed favorites from login response", e2);
                                // Nếu có lỗi, vẫn set về array rỗng để tránh hiển thị sai
                                SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
                                prefs.edit().putString("favorite_books", "[]").apply();
                            }

                            // Kiểm tra role và redirect
                            Intent intent;
                            if ("admin".equalsIgnoreCase(role)) {
                                // Chuyển đến AdminActivity nếu là admin
                                intent = new Intent(SignInActivity.this, AdminMainActivity.class);
                                Log.d(TAG, "Redirecting to AdminMainActivity for admin user");
                            } else {
                                // Chuyển đến HomeActivity nếu là user
                                intent = new Intent(SignInActivity.this, HomeActivity.class);
                                Log.d(TAG, "Redirecting to HomeActivity for regular user");
                            }
                            startActivity(intent);
                            finish(); // ket thuc intent de khong quay lai man hinh splash
                        }
                    }
                    else {
                        String errorMessage = "Sign in failed";
                        int statusCode = response.code();
                        
                        try{
                            if(response.errorBody() != null){
                                String errorBody = response.errorBody().string();
                                Log.e(TAG, "Error body: " + errorBody);
                                JSONObject jsonObject = new JSONObject(errorBody);
                                String serverMessage = jsonObject.optString("message", "Sign in failed");

                                // Xử lý rate limiting error
                                if (statusCode == 429 || serverMessage.toLowerCase().contains("too many requests")) {
                                    errorMessage = "Too many login attempts. Please wait a minute and try again.";
                                    // Set cooldown period
                                    lastLoginAttemptTime = System.currentTimeMillis() + RATE_LIMIT_COOLDOWN - MIN_LOGIN_INTERVAL;
                                    Toast.makeText(SignInActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                    return;
                                }
                                
                                // Ánh xạ thông báo lỗi từ backend
                                switch (serverMessage) {
                                    case "Invalid email":
                                        errorMessage = "Invalid email format. Please check your email.";
                                        break;
                                    case "User not found":
                                        errorMessage = "Email not found. Please sign up.";
                                        break;
                                    case "Wrong password":
                                        errorMessage = "Incorrect password. Please try again.";
                                        break;
                                    case "Unable to sign in":
                                        errorMessage = "Server error. Please try again later.";
                                        break;
                                    default:
                                        errorMessage = serverMessage;
                                        break;
                                }

                            }
                        }catch (Exception e) {
                            Log.e(TAG, "Error parsing error body", e);
                        }
                        
                        // Handle HTTP 429 (Too Many Requests)
                        if (statusCode == 429) {
                            errorMessage = "Too many login attempts. Please wait a minute and try again.";
                            lastLoginAttemptTime = System.currentTimeMillis() + RATE_LIMIT_COOLDOWN - MIN_LOGIN_INTERVAL;
                        }
                        
                        Toast.makeText(SignInActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    // Reset state
                    isLoginInProgress = false;
                    btnSignInHomPage.setEnabled(true);
                    btnSignInHomPage.setAlpha(1.0f);
                    currentLoginCall = null;
                    
                    // Don't show error if request was canceled
                    if (call.isCanceled()) {
                        Log.d(TAG, "Login request was canceled");
                        return;
                    }
                    
                    Log.e(TAG, "API failure", t);
                    String errorMessage = "Network error: " + t.getMessage();
                    
                    // Check if it's a rate limiting error
                    if (t.getMessage() != null && t.getMessage().toLowerCase().contains("too many requests")) {
                        errorMessage = "Too many login attempts. Please wait a minute and try again.";
                        lastLoginAttemptTime = System.currentTimeMillis() + RATE_LIMIT_COOLDOWN - MIN_LOGIN_INTERVAL;
                    }
                    
                    Toast.makeText(SignInActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Hide password
            etPasswordSignIn.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            ivPasswordEye.setImageResource(R.drawable.ic_eye_off);
            isPasswordVisible = false;
        } else {
            // Show password
            etPasswordSignIn.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            ivPasswordEye.setImageResource(R.drawable.ic_eye);
            isPasswordVisible = true;
        }
        // Move cursor to end
        etPasswordSignIn.setSelection(etPasswordSignIn.getText().length());
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel ongoing request if activity is destroyed
        if (currentLoginCall != null && !currentLoginCall.isCanceled()) {
            currentLoginCall.cancel();
            currentLoginCall = null;
        }
    }
}
