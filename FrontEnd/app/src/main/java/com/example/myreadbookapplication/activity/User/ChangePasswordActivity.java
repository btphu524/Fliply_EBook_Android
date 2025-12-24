package com.example.myreadbookapplication.activity.User;

import android.os.Bundle;
import android.text.InputType;
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
import com.example.myreadbookapplication.network.ApiService;
import com.example.myreadbookapplication.network.RetrofitClient;
import com.example.myreadbookapplication.utils.AuthManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordActivity extends AppCompatActivity {

    private static final String TAG = "ChangePasswordActivity";

    private EditText etOldPassword;
    private EditText etNewPassword;
    private EditText etConfirmPassword;
    private TextView btnCancel;
    private TextView btnChangePassword;
    private ProgressBar progressBar;
    
    // Eye icons for password visibility
    private ImageView ivOldPasswordEye;
    private ImageView ivNewPasswordEye;
    private ImageView ivConfirmPasswordEye;
    
    // Password visibility states
    private boolean isOldPasswordVisible = false;
    private boolean isNewPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;
    
    private ApiService apiService;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_password);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        // Initialize views
        etOldPassword = findViewById(R.id.et_old_password);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnCancel = findViewById(R.id.btn_cancel);
        btnChangePassword = findViewById(R.id.btn_change_password);
        
        // Try to find progress bar (might not exist in layout)
        try {
            progressBar = findViewById(R.id.progress_bar);
        } catch (Exception e) {
            Log.d(TAG, "ProgressBar not found in layout");
        }
        
        // Initialize eye icons
        ivOldPasswordEye = findViewById(R.id.iv_old_password_eye);
        ivNewPasswordEye = findViewById(R.id.iv_new_password_eye);
        ivConfirmPasswordEye = findViewById(R.id.iv_confirm_password_eye);
        
        // Initialize API service and AuthManager
        apiService = RetrofitClient.getApiService();
        authManager = AuthManager.getInstance(this);
    }

    private void setupClickListeners() {
        // Back button
        ImageView backIcon = findViewById(R.id.back_icon);
        backIcon.setOnClickListener(v -> finish());

        // Eye icons for password visibility
        ivOldPasswordEye.setOnClickListener(v -> togglePasswordVisibility(etOldPassword, ivOldPasswordEye, isOldPasswordVisible));
        ivNewPasswordEye.setOnClickListener(v -> togglePasswordVisibility(etNewPassword, ivNewPasswordEye, isNewPasswordVisible));
        ivConfirmPasswordEye.setOnClickListener(v -> togglePasswordVisibility(etConfirmPassword, ivConfirmPasswordEye, isConfirmPasswordVisible));

        // Cancel button
        btnCancel.setOnClickListener(v -> finish());

        // Change password button
        btnChangePassword.setOnClickListener(v -> {
            if (validateInput()) {
                changePassword();
            }
        });
    }

    private void togglePasswordVisibility(EditText editText, ImageView eyeIcon, boolean isVisible) {
        if (isVisible) {
            // Hide password
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            eyeIcon.setImageResource(R.drawable.ic_eye_off);
        } else {
            // Show password
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            eyeIcon.setImageResource(R.drawable.ic_eye);
        }
        
        // Update state
        if (editText == etOldPassword) {
            isOldPasswordVisible = !isOldPasswordVisible;
        } else if (editText == etNewPassword) {
            isNewPasswordVisible = !isNewPasswordVisible;
        } else if (editText == etConfirmPassword) {
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
        }
        
        // Move cursor to end
        editText.setSelection(editText.getText().length());
    }

    private boolean validateInput() {
        String oldPassword = etOldPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (oldPassword.isEmpty()) {
            etOldPassword.setError("Old Password is required");
            etOldPassword.requestFocus();
            return false;
        }

        if (newPassword.isEmpty()) {
            etNewPassword.setError("New Password is required");
            etNewPassword.requestFocus();
            return false;
        }

        if (!isValidPassword(newPassword)) {
            etNewPassword.setError("Min. 8 character, 1 letter, 1 number and 1 special character");
            etNewPassword.requestFocus();
            return false;
        }

        if (confirmPassword.isEmpty()) {
            etConfirmPassword.setError("Confirm Password is required");
            etConfirmPassword.requestFocus();
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return false;
        }

        if (oldPassword.equals(newPassword)) {
            etNewPassword.setError("New password must be different from old password");
            etNewPassword.requestFocus();
            return false;
        }

        return true;
    }

    private boolean isValidPassword(String password) {
        if (password.length() < 8) return false;
        
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasNumber = password.matches(".*[0-9].*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*(),.?\":{}|<>].*");
        
        return hasLetter && hasNumber && hasSpecial;
    }

    private void changePassword() {
        String oldPassword = etOldPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        String token = authManager.getAccessToken();

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "You are not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        btnChangePassword.setEnabled(false);

        // Call API to change password
        Call<ApiResponse> call = apiService.changePassword(oldPassword, newPassword, confirmPassword, "Bearer " + token);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                btnChangePassword.setEnabled(true);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(ChangePasswordActivity.this, 
                        response.body().getMessage() != null ? response.body().getMessage() : "Password changed successfully!",
                        Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Log.e(TAG, "Old password is incorrect with code: " + response.code());
                    String errorMessage = "Old password is incorrect";
                    
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBody);
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading error body", e);
                        }
                    }
                    
                    Toast.makeText(ChangePasswordActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                btnChangePassword.setEnabled(true);
                Log.e(TAG, "Error changing password: " + t.getMessage());
                Toast.makeText(ChangePasswordActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
