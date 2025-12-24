package com.example.myreadbookapplication.activity.User;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myreadbookapplication.R;
import com.example.myreadbookapplication.model.ApiResponse;
import com.example.myreadbookapplication.model.FeedbackRequest;
import com.example.myreadbookapplication.network.ApiService;
import com.example.myreadbookapplication.network.RetrofitClient;
import com.example.myreadbookapplication.utils.AuthManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeedbackActivity extends AppCompatActivity {

    private EditText etFullName;
    private EditText etPhone;
    private EditText etEmail;
    private EditText etComment;
    private Button btnSend;
    
    private ApiService apiService;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_feedback);

        // Initialize services
        apiService = RetrofitClient.getApiService();
        authManager = AuthManager.getInstance(this);

        initViews();
        setupClickListeners();
        loadUserData();
    }

    private void initViews() {
        // Initialize views
        etFullName = findViewById(R.id.et_full_name);
        etPhone = findViewById(R.id.et_phone);
        etEmail = findViewById(R.id.et_email);
        etComment = findViewById(R.id.et_comment);
        btnSend = findViewById(R.id.btn_send);
    }

    private void setupClickListeners() {
        // Back the button
        ImageView backIcon = findViewById(R.id.back_icon);
        backIcon.setOnClickListener(v -> finish());

        // Send button
        btnSend.setOnClickListener(v -> {
            if (validateInput()) {
                sendFeedback();
            }
        });
    }

    private void loadUserData() {
        // Load user data from AuthManager
        String userEmail = authManager.getUserEmail();
        String userFullName = authManager.getUserFullName();
        
        if (userEmail != null && !userEmail.isEmpty()) {
            etEmail.setText(userEmail);
        }
        
        if (userFullName != null && !userFullName.isEmpty()) {
            etFullName.setText(userFullName);
        }
    }

    private boolean validateInput() {
        String fullName = etFullName.getText().toString().trim();
        String comment = etComment.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (fullName.isEmpty()) {
            etFullName.setError("Họ tên là bắt buộc");
            etFullName.requestFocus();
            return false;
        }

        if (fullName.length() < 2 || fullName.length() > 100) {
            etFullName.setError("Họ tên phải có từ 2-100 ký tự");
            etFullName.requestFocus();
            return false;
        }

        if (comment.isEmpty()) {
            etComment.setError("Bình luận là bắt buộc");
            etComment.requestFocus();
            return false;
        }

        if (comment.length() < 10 || comment.length() > 1000) {
            etComment.setError("Bình luận phải có từ 10-1000 ký tự");
            etComment.requestFocus();
            return false;
        }

        if (!email.isEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không hợp lệ");
            etEmail.requestFocus();
            return false;
        }

        if (!phone.isEmpty() && !phone.matches("^[0-9]{10,11}$")) {
            etPhone.setError("Số điện thoại phải có 10-11 chữ số");
            etPhone.requestFocus();
            return false;
        }

        return true;
    }

    private void sendFeedback() {
        if (!authManager.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập để gửi phản hồi", Toast.LENGTH_LONG).show();
            return;
        }

        String fullName = etFullName.getText().toString().trim();
        String phoneNumber = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String comment = etComment.getText().toString().trim();

        // Create feedback request
        FeedbackRequest request = new FeedbackRequest(fullName, phoneNumber, email, comment);

        // Disable send button to prevent multiple submissions
        btnSend.setEnabled(false);
        btnSend.setText("Đang gửi...");

        // Get authorization header
        String authHeader = authManager.getAuthorizationHeader();
        if (authHeader == null) {
            Toast.makeText(this, "Lỗi xác thực. Vui lòng đăng nhập lại", Toast.LENGTH_LONG).show();
            btnSend.setEnabled(true);
            btnSend.setText("Gửi");
            return;
        }

        Log.d("FeedbackActivity", "Sending feedback: " + request.toString());
        Log.d("FeedbackActivity", "Auth header: " + authHeader);

        // Call API
        Call<ApiResponse> call = apiService.createFeedback(request, authHeader);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                btnSend.setEnabled(true);
                btnSend.setText("Gửi");

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(FeedbackActivity.this, "Gửi phản hồi thành công!", Toast.LENGTH_SHORT).show();
                        Log.d("FeedbackActivity", "Feedback sent successfully");
                        finish(); // Close activity
                    } else {
                        Toast.makeText(FeedbackActivity.this, "Lỗi: " + apiResponse.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("FeedbackActivity", "API returned success=false: " + apiResponse.getMessage());
                    }
                } else {
                    String errorMessage = "Gửi phản hồi thất bại";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e("FeedbackActivity", "Error response: " + errorBody);
                        }
                    } catch (Exception e) {
                        Log.e("FeedbackActivity", "Error reading error body", e);
                    }
                    Toast.makeText(FeedbackActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    Log.e("FeedbackActivity", "Response not successful: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                btnSend.setEnabled(true);
                btnSend.setText("Gửi");
                
                Toast.makeText(FeedbackActivity.this, "Lỗi kết nối. Vui lòng thử lại", Toast.LENGTH_LONG).show();
                Log.e("FeedbackActivity", "Network error: " + t.getMessage(), t);
            }
        });
    }
}
