package com.example.myreadbookapplication.fragment.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myreadbookapplication.R;
import com.example.myreadbookapplication.activity.User.ChangePasswordActivity;
import com.example.myreadbookapplication.utils.AuthManager;
import com.example.myreadbookapplication.utils.LogoutManager;

public class AccountFragment extends Fragment {

    private static final String TAG = "AccountFragment";
    
    private TextView tvEmail;
    private TextView tvAvatarInitial;
    private LinearLayout tvChangePassword;
    private LinearLayout tvSignOut;
    
    private AuthManager authManager;
    private LogoutManager logoutManager;
    
    private boolean isDataLoaded = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authManager = AuthManager.getInstance(requireContext());
        logoutManager = new LogoutManager(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        
        initViews(view);
        setupClickListeners();
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!isDataLoaded) {
            loadUserInfo();
        }
    }

    private void initViews(View view) {
        tvEmail = view.findViewById(R.id.tv_email);
        tvAvatarInitial = view.findViewById(R.id.tv_avatar_initial);
        tvChangePassword = view.findViewById(R.id.tv_change_password);
        tvSignOut = view.findViewById(R.id.tv_sign_out);
    }

    private void setupClickListeners() {
        tvChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ChangePasswordActivity.class);
            startActivity(intent);
        });

        tvSignOut.setOnClickListener(v -> {
            // Sử dụng callback để finish activity sau khi logout hoàn thành
            logoutManager.confirmLogout(new LogoutManager.LogoutCallback() {
                @Override
                public void onLogoutSuccess() {
                    // Logout thành công, LogoutManager đã start SignInActivity
                    // Finish activity hiện tại để đảm bảo không quay lại được
                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                }
            });
        });
    }

    /**
     * Public method để reload data từ Activity
     */
    public void reloadData() {
        Log.d(TAG, "Reloading user info...");
        loadUserInfo();
    }

    private void loadUserInfo() {
        String email = authManager.getUserEmail();
        if (email != null && !email.isEmpty()) {
            if (tvEmail != null) {
                tvEmail.setText(email);
            }
            // Set avatar initial from first letter of email
            String initial = email.substring(0, 1).toUpperCase();
            if (tvAvatarInitial != null) {
                tvAvatarInitial.setText(initial);
            }
        } else {
            if (tvEmail != null) {
                tvEmail.setText("admin@example.com");
            }
            if (tvAvatarInitial != null) {
                tvAvatarInitial.setText("A");
            }
        }
        isDataLoaded = true;
    }
}

