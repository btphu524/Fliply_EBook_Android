package com.example.myreadbookapplication.activity.User;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myreadbookapplication.R;
import com.example.myreadbookapplication.model.ApiResponse;
import com.example.myreadbookapplication.model.ForgotPasswordRequest;
import com.example.myreadbookapplication.model.ResendOtpRequest;
import com.example.myreadbookapplication.model.VerifyOtpRequest;
import com.example.myreadbookapplication.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerificationActivity extends AppCompatActivity {

    private EditText etOtp;
    private String userEmail;
    private static final String TAG = "VerificationActivity";
    private ProgressBar progressBar;
    private LinearLayout btnVerify;
    private TextView tvResendOtp;
    private TextView tvCountdown;
    private CountDownTimer countDownTimer;
    private static final long COUNTDOWN_DURATION = 90000; // 1:30 phút (90000ms)
    private static final long COUNTDOWN_INTERVAL = 1000; // 1 giây
    private ProgressDialog progressDialog;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean fromForgot = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_verification);

        // Khởi tạo ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Processing...");
        progressDialog.setCancelable(false);

        // Lấy email từ Intent hoặc SharedPreferences
        userEmail = getIntent().getStringExtra("email");
        fromForgot = getIntent().getBooleanExtra("from_forgot", false);
        if (userEmail == null) {
            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            userEmail = prefs.getString("user_email", "");
        }

        // Ánh xạ
        btnVerify = findViewById(R.id.btn_verify);
        ImageView ivBackVerification = findViewById(R.id.iv_back);
        tvResendOtp = findViewById(R.id.tv_resend);
        etOtp = findViewById(R.id.et_otp);
        progressBar = findViewById(R.id.progress_bar);
        tvCountdown = findViewById(R.id.tv_countdown);

        if (btnVerify == null || ivBackVerification == null || tvResendOtp == null ||
                etOtp == null || progressBar == null || tvCountdown == null) {
            Toast.makeText(this, "Layout error", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Khởi tạo đồng hồ đếm ngược
        startCountdown();

        // Nút Verify → gọi API verify OTP
        btnVerify.setOnClickListener(v -> {
            String otp = etOtp.getText().toString().trim();
            if (otp.length() == 6 && !userEmail.isEmpty()) {
                hideKeyboard();
                progressDialog.setMessage("Verifying OTP...");
                progressDialog.show();
                verifyOtp(userEmail, otp);
            } else {
                etOtp.setError("Please enter 6-digit OTP");
            }
        });

        // Nút Resend OTP
        tvResendOtp.setOnClickListener(v -> {
            if (!userEmail.isEmpty()) {
                progressDialog.setMessage("Resending OTP...");
                progressDialog.show();
                resendOtp(userEmail);
            } else {
                Toast.makeText(this, "Email not found", Toast.LENGTH_SHORT).show();
            }
        });

        // Nút Back
        ivBackVerification.setOnClickListener(v -> finish());

        // Khi vừa vào màn hình tự động bật bàn phím
        etOtp.requestFocus();
        etOtp.postDelayed(this::showKeyboard, 200);
    }

    private void startCountdown() {
        if (tvCountdown == null) {
            return;
        }
        tvResendOtp.setEnabled(false); // Vô hiệu hóa nút Resend OTP
        if (countDownTimer != null) {
            countDownTimer.cancel(); // Hủy timer cũ nếu có
        }
        countDownTimer = new CountDownTimer(COUNTDOWN_DURATION, COUNTDOWN_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (tvCountdown != null) {
                    int minutes = (int) (millisUntilFinished / 1000) / 60;
                    int seconds = (int) (millisUntilFinished / 1000) % 60;
                    tvCountdown.setText(String.format("%d:%02d", minutes, seconds));
                }
            }

            @Override
            public void onFinish() {
                if (tvCountdown != null) {
                    tvCountdown.setText("0:00");
                }
                tvResendOtp.setEnabled(true); // Kích hoạt lại nút Resend OTP
            }
        }.start();
    }

    private void showKeyboard() {
        etOtp.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(etOtp, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(etOtp.getWindowToken(), 0);
        }
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnVerify.setEnabled(!loading);
        tvResendOtp.setEnabled(!loading && countDownTimer == null);
    }

    private void verifyOtp(String email, String otp) {
        VerifyOtpRequest request = new VerifyOtpRequest(email, otp);

        RetrofitClient.getApiService().verifyOtp(request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                handler.post(() -> {
                    if (!isFinishing() && !isDestroyed()) {
                        progressDialog.dismiss();
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse apiResponse = response.body();
                            Toast.makeText(VerificationActivity.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                            if (apiResponse.isSuccess()) {
                                // Lưu email vào prefs nếu cần (cho resend sau)
                                SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                                prefs.edit().putString("user_email", userEmail).apply();

                                if (fromForgot) {
                                    // Từ forgot password → NewPassword
                                    Intent intent = new Intent(VerificationActivity.this, NewPasswordActivity.class);
                                    intent.putExtra("email", userEmail); // Gửi email cho NewPassword nếu cần
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // Từ signup → SignIn
                                    startActivity(new Intent(VerificationActivity.this, SignInActivity.class));
                                    finish();
                                }
                            }
                        } else {
                            String errorMessage = "Verification failed";
                            int statusCode = response.code();
                            if (statusCode == 400) {
                                errorMessage = "Invalid OTP";
                            } else if (statusCode == 500) {
                                errorMessage = "Server error. Please try again later.";
                            }
                            Toast.makeText(VerificationActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                handler.post(() -> {
                    if (!isFinishing() && !isDestroyed()) {
                        progressDialog.dismiss();
                        Toast.makeText(VerificationActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void resendOtp(String email) {
        if (fromForgot) {
            // Nếu từ forgot password, gọi forgotPassword API
            ForgotPasswordRequest request = new ForgotPasswordRequest(email);
            RetrofitClient.getApiService().forgotPassword(request).enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    handler.post(() -> {
                        if (!isFinishing() && !isDestroyed()) {
                            progressDialog.dismiss();
                            if (response.isSuccessful() && response.body() != null) {
                                ApiResponse apiResponse = response.body();
                                Toast.makeText(VerificationActivity.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                                // Khởi động lại đồng hồ đếm ngược sau khi gửi lại OTP
                                startCountdown();
                            } else {
                                String errorMessage = "Resend failed";
                                int statusCode = response.code();
                                if (statusCode == 400) {
                                    errorMessage = "Invalid request";
                                } else if (statusCode == 500) {
                                    errorMessage = "Server error. Please try again later.";
                                }
                                Toast.makeText(VerificationActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    handler.post(() -> {
                        if (!isFinishing() && !isDestroyed()) {
                            progressDialog.dismiss();
                            Toast.makeText(VerificationActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        } else {
            // Nếu từ signup, gọi resendOtp API cũ
            ResendOtpRequest request = new ResendOtpRequest(email);
            RetrofitClient.getApiService().resendOtp(request).enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    handler.post(() -> {
                        if (!isFinishing() && !isDestroyed()) {
                            progressDialog.dismiss();
                            if (response.isSuccessful() && response.body() != null) {
                                Toast.makeText(VerificationActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                                // Khởi động lại đồng hồ đếm ngược sau khi gửi lại OTP
                                startCountdown();
                            } else {
                                String errorMessage = "Resend failed";
                                int statusCode = response.code();
                                if (statusCode == 400) {
                                    errorMessage = "Invalid request";
                                } else if (statusCode == 500) {
                                    errorMessage = "Server error. Please try again later.";
                                }
                                Toast.makeText(VerificationActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    handler.post(() -> {
                        if (!isFinishing() && !isDestroyed()) {
                            progressDialog.dismiss();
                            Toast.makeText(VerificationActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}