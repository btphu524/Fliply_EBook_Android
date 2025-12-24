package com.example.myreadbookapplication.activity.User;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myreadbookapplication.R;
import com.example.myreadbookapplication.model.ApiResponse;
import com.example.myreadbookapplication.model.ResetPasswordRequest;
import com.example.myreadbookapplication.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewPasswordActivity extends AppCompatActivity {

    private EditText etNewPassword;
    private EditText etConfirmPassword;
    private String userEmail;
    private android.app.ProgressDialog progressDialog;
    private Handler handler = new Handler(Looper.getMainLooper());
    private static final int MIN_PASSWORD_LENGTH = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_new_password);

        // Khởi tạo ProgressDialog
        progressDialog = new android.app.ProgressDialog(this);
        progressDialog.setMessage("Resetting password...");
        progressDialog.setCancelable(false);

        // Lấy email từ Intent
        userEmail = getIntent().getStringExtra("email");
        if (TextUtils.isEmpty(userEmail)) {
            Toast.makeText(this, "Email not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Ánh xạ (thêm EditText)
        ImageView backButonNewPassword = findViewById(R.id.back_button_new_password);
        LinearLayout submitButtonNewPassword = findViewById(R.id.submit_button_container);
        etNewPassword = findViewById(R.id.edit_text_new_password);
        etConfirmPassword = findViewById(R.id.edit_text_confirm_password);

        if (etNewPassword == null || etConfirmPassword == null) {
            Toast.makeText(this, "Layout error: Missing password inputs", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Xử lý
        backButonNewPassword.setOnClickListener(v -> finish());

        submitButtonNewPassword.setOnClickListener(v -> {
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            // Validate
            if (TextUtils.isEmpty(newPassword)) {
                etNewPassword.setError("Please enter new password");
                return;
            }
            if (newPassword.length() < MIN_PASSWORD_LENGTH) {
                etNewPassword.setError("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                etConfirmPassword.setError("Passwords do not match");
                return;
            }

            // Gọi API reset password
            progressDialog.show();
            resetPassword(userEmail, newPassword, confirmPassword);
        });
    }

    private void resetPassword(String email, String newPassword, String confirmPassword) {
        ResetPasswordRequest request = new ResetPasswordRequest(email, newPassword, confirmPassword);

        RetrofitClient.getApiService().resetPassword(request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                handler.post(() -> {
                    if (!isFinishing() && !isDestroyed()) {
                        progressDialog.dismiss();
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse apiResponse = response.body();
                            Toast.makeText(NewPasswordActivity.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                            if (apiResponse.isSuccess()) {
                                // Thành công → Chuyển về SignInActivity
                                Intent intent = new Intent(NewPasswordActivity.this, SignInActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            String errorMessage = "Reset failed";
                            int statusCode = response.code();
                            if (statusCode == 400) {
                                errorMessage = "Invalid request";
                            } else if (statusCode == 500) {
                                errorMessage = "Server error. Try again later.";
                            }
                            Toast.makeText(NewPasswordActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                handler.post(() -> {
                    if (!isFinishing() && !isDestroyed()) {
                        progressDialog.dismiss();
                        Toast.makeText(NewPasswordActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}