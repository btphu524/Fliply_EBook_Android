package com.example.myreadbookapplication.activity.User;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.example.myreadbookapplication.model.SignUpRequest;
import com.example.myreadbookapplication.network.RetrofitClient;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        // Anh xạ
        TextView tvAlreadyHaveAccount = findViewById(R.id.tv_already_have_account);
        LinearLayout btnSignUp = findViewById(R.id.btn_signup_layout);
        ImageView imBackIcon = findViewById(R.id.back_icon);
        EditText edtName = findViewById(R.id.input_name);
        EditText edtEmail = findViewById(R.id.input_email);
        EditText edtPhone = findViewById(R.id.input_phone);
        EditText edtPassword = findViewById(R.id.input_password);
        EditText edtConfirmPassword = findViewById(R.id.input_confirm_password);
        ProgressBar progressBarSignup = findViewById(R.id.progress_bar_signup);
        TextView btnSignupText = findViewById(R.id.btn_signup_text);

        // bắt sự kiện và xử lý cho text already have an acount, sign in
        tvAlreadyHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                startActivity(intent);
            }
        });

        // bắt sự kiện và xử lý cho button đăng ký
        btnSignUp.setOnClickListener(v -> {

            // lấy dữ liệu cuả các edit text nhập vào từ người dùng như email, passwword,
            // ...
            String fullName = edtName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String phoneNumber = edtPhone.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            String confirmPassword = edtConfirmPassword.getText().toString().trim();

            // nếu người dùng không nhập đầy đủ thông tin thì thông báo
            if (fullName.isEmpty() || email.isEmpty() || phoneNumber.isEmpty() || password.isEmpty()
                    || confirmPassword.isEmpty()) {
                Toast.makeText(SignUpActivity.this, "You need fill in all of the fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // nếu email không đúng định dạng thì thông báo luon
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(SignUpActivity.this, "Invalid email format", Toast.LENGTH_SHORT).show();
                return;
            }

            // nếu passwword khác confirm passwword thì cũng thông báo
            if (!confirmPassword.equals(password)) {
                Toast.makeText(SignUpActivity.this, "Password do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // nếu password nhỏ hơn 8 ký tự thì thông báo
            if (password.length() < 8) {
                edtPassword.setError("Password must be >= 8 characters");
                return;
            }

            // nếu password toàn chũ hoặc số thì thông báo
            if (password.matches("[a-zA-Z]+") || password.matches("[0-9]+")) {
                edtPassword.setError("Password must contain both letters and numbers");
                return;
            }

            // Disable nút để tránh bấm liên tục và hiển thị spinner
            btnSignUp.setEnabled(false);
            progressBarSignup.setVisibility(View.VISIBLE);
            btnSignupText.setText("Registering...");

            // Tạo request object và gọi API
            SignUpRequest request = new SignUpRequest(fullName, email, password, confirmPassword, phoneNumber);

            Call<ApiResponse> call = RetrofitClient.getApiService().signUp(request);
            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    btnSignUp.setEnabled(true);
                    progressBarSignup.setVisibility(View.GONE);
                    btnSignupText.setText("Sign Up");
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse apiResponse = response.body();
                        Toast.makeText(SignUpActivity.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        if (apiResponse.isSuccess()) {
                            // Lưu email vào SharedPreferences
                            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                            prefs.edit().putString("user_email", email).apply();
                            
                            // Clear favorite_books để đảm bảo user mới không thấy favorites của user cũ
                            SharedPreferences appPrefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
                            appPrefs.edit().putString("favorite_books", "[]").apply();

                            // Chuyển sang verification với email
                            Intent intent = new Intent(SignUpActivity.this, VerificationActivity.class);
                            intent.putExtra("email", email);
                            intent.putExtra("from_forgot", false); // Mặc định false cho signup
                            startActivity(intent);
                        }
                    } else {
                        String errorMessage = "Registration failed";
                        try {
                            if (response.errorBody() != null) {
                                String errorBody = response.errorBody().string();
                                Log.e(TAG, "Error body: " + errorBody);
                                JSONObject jsonObject = new JSONObject(errorBody);
                                String serverMessage = jsonObject.optString("message", "Registration failed");

                                // Ánh xạ thông báo lỗi từ backend
                                switch (serverMessage) {
                                    case "Email already in use":
                                        errorMessage = "Email already exists. Please use a different email.";
                                        break;
                                    case "Invalid email":
                                        errorMessage = "Invalid email format. Please check your email.";
                                        break;
                                    case "Password too weak":
                                        errorMessage = "Password is too weak. Please use a stronger password.";
                                        break;
                                    case "Unable to create user":
                                        errorMessage = "Server error. Please try again later.";
                                        break;
                                    default:
                                        errorMessage = serverMessage;
                                        break;
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing error body", e);
                        }

                        Toast.makeText(SignUpActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    btnSignUp.setEnabled(true);
                    progressBarSignup.setVisibility(View.GONE);
                    btnSignupText.setText("Sign Up");
                    Log.e(TAG, "API failure", t);
                    Toast.makeText(SignUpActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        // bắt sự kiện và xử lý cho icon back
        imBackIcon.setOnClickListener(v -> {
            finish();
        });
    }
}