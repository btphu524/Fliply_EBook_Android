package com.example.myreadbookapplication.activity.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myreadbookapplication.R;
import com.example.myreadbookapplication.activity.User.HomeActivity;
import com.example.myreadbookapplication.fragment.admin.AccountFragment;
import com.example.myreadbookapplication.fragment.admin.BookFragment;
import com.example.myreadbookapplication.fragment.admin.CategoryFragment;
import com.example.myreadbookapplication.fragment.admin.FeedbackFragment;
import com.example.myreadbookapplication.utils.AuthManager;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class AdminMainActivity extends AppCompatActivity {

    private static final String TAG = "AdminMainActivity";
    
    private ViewPager2 viewPager;
    private AdminFragmentPagerAdapter pagerAdapter;
    private TextView tvHeaderTitle;
    
    // Bottom Navigation
    private LinearLayout navCategory, navBook, navFeedback, navAccount;
    private ImageView ivCategory, ivBook, ivFeedback, ivAccount;
    private TextView tvCategory, tvBook, tvFeedback, tvAccount;
    
    private AuthManager authManager;
    private int currentPosition = 0;
    
    // Fragment references để có thể gọi reload
    private CategoryFragment categoryFragment;
    private BookFragment bookFragment;
    private FeedbackFragment feedbackFragment;
    private AccountFragment accountFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_main);

        authManager = AuthManager.getInstance(this);

        // Kiểm tra xem user có phải admin không
        if (!authManager.isAdmin()) {
            Log.e(TAG, "User is not admin, redirecting to home");
            Toast.makeText(this, "You don't have admin access", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        Log.d(TAG, "Admin user logged in, opening admin dashboard");
        
        initViews();
        setupViewPager();
        setupBottomNavigation();
    }

    private void initViews() {
        viewPager = findViewById(R.id.view_pager);
        tvHeaderTitle = findViewById(R.id.tv_header_title);
        
        // Bottom Navigation
        navCategory = findViewById(R.id.nav_category);
        navBook = findViewById(R.id.nav_book);
        navFeedback = findViewById(R.id.nav_feedback);
        navAccount = findViewById(R.id.nav_account);
        
        ivCategory = findViewById(R.id.iv_category);
        ivBook = findViewById(R.id.iv_book);
        ivFeedback = findViewById(R.id.iv_feedback);
        ivAccount = findViewById(R.id.iv_account);
        
        tvCategory = findViewById(R.id.tv_category);
        tvBook = findViewById(R.id.tv_book);
        tvFeedback = findViewById(R.id.tv_feedback);
        tvAccount = findViewById(R.id.tv_account);
    }

    private void setupViewPager() {
        // Tạo các Fragment
        categoryFragment = new CategoryFragment();
        bookFragment = new BookFragment();
        feedbackFragment = new FeedbackFragment();
        accountFragment = new AccountFragment();
        
        // Tạo adapter
        pagerAdapter = new AdminFragmentPagerAdapter(this);
        pagerAdapter.addFragment(categoryFragment, "Category");
        pagerAdapter.addFragment(bookFragment, "Book");
        pagerAdapter.addFragment(feedbackFragment, "Feedback");
        pagerAdapter.addFragment(accountFragment, "Account");
        
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(3); // Giữ 3 fragment trong memory để tối ưu
        
        // Disable swipe để chỉ dùng bottom navigation
        viewPager.setUserInputEnabled(false);
        
        // Listen to page changes để update UI và reload data
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentPosition = position;
                updateHeaderTitle(position);
                updateBottomNavigation(position);
                reloadFragmentData(position);
            }
        });
    }

    private void setupBottomNavigation() {
        navCategory.setOnClickListener(v -> {
            if (currentPosition != 0) {
                viewPager.setCurrentItem(0, false);
            }
        });

        navBook.setOnClickListener(v -> {
            if (currentPosition != 1) {
                viewPager.setCurrentItem(1, false);
            }
        });

        navFeedback.setOnClickListener(v -> {
            if (currentPosition != 2) {
                viewPager.setCurrentItem(2, false);
            }
        });

        navAccount.setOnClickListener(v -> {
            if (currentPosition != 3) {
                viewPager.setCurrentItem(3, false);
            }
        });
    }

    private void updateHeaderTitle(int position) {
        String[] titles = {"Category", "Book", "Feedback", "Account"};
        if (position >= 0 && position < titles.length) {
            tvHeaderTitle.setText(titles[position]);
        }
    }

    private void updateBottomNavigation(int position) {
        // Reset tất cả
        resetBottomNavigation();
        
        // Highlight tab active
        switch (position) {
            case 0: // Category
                ivCategory.setColorFilter(getColor(R.color.admin_footer_text_color));
                tvCategory.setTextColor(getColor(R.color.admin_footer_text_color));
                tvCategory.setTypeface(null, android.graphics.Typeface.BOLD);
                break;
            case 1: // Book
                ivBook.setColorFilter(getColor(R.color.admin_footer_text_color));
                tvBook.setTextColor(getColor(R.color.admin_footer_text_color));
                tvBook.setTypeface(null, android.graphics.Typeface.BOLD);
                break;
            case 2: // Feedback
                ivFeedback.setColorFilter(getColor(R.color.admin_footer_text_color));
                tvFeedback.setTextColor(getColor(R.color.admin_footer_text_color));
                tvFeedback.setTypeface(null, android.graphics.Typeface.BOLD);
                break;
            case 3: // Account
                ivAccount.setColorFilter(getColor(R.color.admin_footer_text_color));
                tvAccount.setTextColor(getColor(R.color.admin_footer_text_color));
                tvAccount.setTypeface(null, android.graphics.Typeface.BOLD);
                break;
        }
    }

    private void resetBottomNavigation() {
        int whiteColor = getColor(R.color.white);
        int defaultTextColor = whiteColor;
        
        // Reset Category
        ivCategory.setColorFilter(whiteColor);
        tvCategory.setTextColor(defaultTextColor);
        tvCategory.setTypeface(null, android.graphics.Typeface.NORMAL);
        
        // Reset Book
        ivBook.setColorFilter(whiteColor);
        tvBook.setTextColor(defaultTextColor);
        tvBook.setTypeface(null, android.graphics.Typeface.NORMAL);
        
        // Reset Feedback
        ivFeedback.setColorFilter(whiteColor);
        tvFeedback.setTextColor(defaultTextColor);
        tvFeedback.setTypeface(null, android.graphics.Typeface.NORMAL);
        
        // Reset Account
        ivAccount.setColorFilter(whiteColor);
        tvAccount.setTextColor(defaultTextColor);
        tvAccount.setTypeface(null, android.graphics.Typeface.NORMAL);
    }

    /**
     * Reload data của Fragment khi được chọn
     */
    private void reloadFragmentData(int position) {
        Log.d(TAG, "Reloading fragment data at position: " + position);
        
        switch (position) {
            case 0: // Category
                if (categoryFragment != null) {
                    categoryFragment.reloadData();
                }
                break;
            case 1: // Book
                if (bookFragment != null) {
                    bookFragment.reloadData();
                }
                break;
            case 2: // Feedback
                if (feedbackFragment != null) {
                    feedbackFragment.reloadData();
                }
                break;
            case 3: // Account
                if (accountFragment != null) {
                    accountFragment.reloadData();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        // Forward result to current fragment
        Fragment currentFragment = pagerAdapter.getFragment(currentPosition);
        if (currentFragment != null) {
            currentFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Method để Fragment có thể update header title
     */
    public void setHeaderTitle(String title) {
        if (tvHeaderTitle != null) {
            tvHeaderTitle.setText(title);
        }
    }
}
