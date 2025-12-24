package com.example.myreadbookapplication.activity.User;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myreadbookapplication.R;
import com.example.myreadbookapplication.model.ApiResponse;
import com.example.myreadbookapplication.model.ForgotPasswordRequest;
import com.example.myreadbookapplication.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmailForgotPassword;
    private ProgressDialog progressDialog;
    private Handler handler = new Handler(Looper.getMainLooper());
    private static final String TAG = "ForgotPasswordActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        // Khởi tạo ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending OTP...");
        progressDialog.setCancelable(false);

        // Ánh xạ (thêm etEmail)
        ImageView backButonForgotPassword = findViewById(R.id.back_button_forgot_password);
        TextView linkBackToSignIn = findViewById(R.id.link_back_to_sign_in);
        LinearLayout sendButtonFogotPassword = findViewById(R.id.send_button_container);
        LinearLayout signUpButtonFogotPassword = findViewById(R.id.signup_button_container);
        etEmailForgotPassword = findViewById(R.id.edit_text_email_forgot_password);

        if (etEmailForgotPassword == null) {
            Toast.makeText(this, "Layout error: Missing email input", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Xử lý
        backButonForgotPassword.setOnClickListener(v -> finish());

        linkBackToSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPasswordActivity.this, SignInActivity.class);
            startActivity(intent);
        });

        sendButtonFogotPassword.setOnClickListener(v -> {
            String email = etEmailForgotPassword.getText().toString().trim();
            if (email.isEmpty()) {
                etEmailForgotPassword.setError("Please enter your email");
                return;
            }
            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                etEmailForgotPassword.setError("Please enter a valid email address");
            }
            progressDialog.show();
            forgotPassword(email); // Gọi API gửi OTP
        });

        signUpButtonFogotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPasswordActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }

    private void forgotPassword(String email) {
        ForgotPasswordRequest request = new ForgotPasswordRequest(email);

        RetrofitClient.getApiService().forgotPassword(request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                handler.post(() -> {
                    if (!isFinishing() && !isDestroyed()) {
                        progressDialog.dismiss();
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse apiResponse = response.body();
                            Toast.makeText(ForgotPasswordActivity.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                            if (apiResponse.isSuccess()) {
                                // Chuyển sang Verification với extra
                                Intent intent = new Intent(ForgotPasswordActivity.this, VerificationActivity.class);
                                intent.putExtra("email", email);
                                intent.putExtra("from_forgot", true);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            String errorMessage = "Failed to send OTP";
                            int statusCode = response.code();
                            if (statusCode == 400) {
                                errorMessage = "Email not registered";
                            } else if (statusCode == 500) {
                                errorMessage = "Server error. Try again later.";
                            }
                            Toast.makeText(ForgotPasswordActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                handler.post(() -> {
                    if (!isFinishing() && !isDestroyed()) {
                        progressDialog.dismiss();
                        Toast.makeText(ForgotPasswordActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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