package com.example.myreadbookapplication.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.myreadbookapplication.R;

import java.util.ArrayList;
import java.util.List;

public class PaginationManager {
    public static final int DEFAULT_ITEMS_PER_PAGE = 12;
    private static final String TAG = "PaginationManager";
    
    // UI Components
    private View paginationView;
    private View btnFirstPage;
    private View btnPrevPage;
    private View btnNextPage;
    private View btnLastPage;
    private LinearLayout containerPageNumbers;
    
    // Pagination State
    private int currentPage = 1;
    private int totalPages = 1;
    private int totalItems = 0;
    private int itemsPerPage = 10;
    private int maxVisiblePages = 7;
    
    // Callbacks
    private OnPageChangeListener pageChangeListener;
    private OnPageJumpListener pageJumpListener;
    
    // Page number buttons
    private List<Button> pageButtons = new ArrayList<>();
    
    public interface OnPageChangeListener {
        void onPageChanged(int page);
    }
    
    public interface OnPageJumpListener {
        void onPageJump(int page);
    }
    
    public PaginationManager(Context context, ViewGroup parent) {
        initViews(context, parent);
        setupClickListeners();
    }
    
    private void initViews(Context context, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        paginationView = inflater.inflate(R.layout.pagination_footer, parent, false);
        
        // Initialize UI components
        btnFirstPage = paginationView.findViewById(R.id.btn_first_page);
        btnPrevPage = paginationView.findViewById(R.id.btn_prev_page);
        btnNextPage = paginationView.findViewById(R.id.btn_next_page);
        btnLastPage = paginationView.findViewById(R.id.btn_last_page);
        containerPageNumbers = paginationView.findViewById(R.id.container_page_numbers);

        // Add to parent
        parent.addView(paginationView);
    }
    
    private void setupClickListeners() {
        btnFirstPage.setOnClickListener(v -> goToFirstPage());
        btnPrevPage.setOnClickListener(v -> goToPreviousPage());
        btnNextPage.setOnClickListener(v -> goToNextPage());
        btnLastPage.setOnClickListener(v -> goToLastPage());
    }
    
    public void setPaginationData(int currentPage, int totalPages, int totalItems, int itemsPerPage) {
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.totalItems = totalItems;
        this.itemsPerPage = itemsPerPage;
        
        updateUI();
    }
    
    public void setOnPageChangeListener(OnPageChangeListener listener) {
        this.pageChangeListener = listener;
    }
    
    public void setOnPageJumpListener(OnPageJumpListener listener) {
        this.pageJumpListener = listener;
    }
    
    private void updateUI() {
        updateNavigationButtons();
        updatePageNumbers();
    }

    public void setMaxVisiblePages(int maxVisiblePages) {
        this.maxVisiblePages = Math.max(1, maxVisiblePages);
        updatePageNumbers();
    }
    
    private void updateNavigationButtons() {
        btnFirstPage.setEnabled(currentPage > 1);
        btnPrevPage.setEnabled(currentPage > 1);
        btnNextPage.setEnabled(currentPage < totalPages);
        btnLastPage.setEnabled(currentPage < totalPages);
    }
    
    private void updatePageNumbers() {
        // Clear existing page buttons
        containerPageNumbers.removeAllViews();
        pageButtons.clear();
        
        if (totalPages <= 1) {
            return;
        }
        
        // Calculate visible page range
        int halfWindow = maxVisiblePages / 2;
        int startPage = currentPage - halfWindow;
        if (startPage < 1) {
            startPage = 1;
        }
        if (startPage + maxVisiblePages - 1 > totalPages) {
            startPage = Math.max(1, totalPages - maxVisiblePages + 1);
        }
        int endPage = Math.min(totalPages, startPage + maxVisiblePages - 1);

        for (int i = startPage; i <= endPage; i++) {
            addPageButton(i);
        }
    }
    
    private void addPageButton(int pageNumber) {
        Button button = new Button(paginationView.getContext(), null, android.R.attr.buttonStyleSmall);
        button.setText(String.valueOf(pageNumber));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(dpToPx(2), 0, dpToPx(2), 0);
        button.setLayoutParams(params);
        button.setBackgroundResource(R.drawable.pagination_cell_background);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        button.setMinWidth(dpToPx(32));
        button.setMinHeight(dpToPx(28));
        button.setPadding(dpToPx(10), dpToPx(4), dpToPx(10), dpToPx(4));
        if (pageNumber == currentPage) {
            button.setSelected(true);
            button.setTextColor(paginationView.getContext().getColor(R.color.pagination_active_text));
        } else {
            button.setSelected(false);
            button.setTextColor(paginationView.getContext().getColor(R.color.secondary_text));
        }
        button.setAllCaps(false);
        button.setOnClickListener(v -> goToPage(pageNumber));
        containerPageNumbers.addView(button);
        pageButtons.add(button);
    }
    
    private void goToFirstPage() {
        if (currentPage > 1) {
            goToPage(1);
        }
    }
    
    private void goToPreviousPage() {
        if (currentPage > 1) {
            goToPage(currentPage - 1);
        }
    }
    
    private void goToNextPage() {
        if (currentPage < totalPages) {
            goToPage(currentPage + 1);
        }
    }
    
    private void goToLastPage() {
        if (currentPage < totalPages) {
            goToPage(totalPages);
        }
    }
    
    private void goToPage(int page) {
        if (page < 1 || page > totalPages || page == currentPage) {
            return;
        }
        
        // Animate page change
        animatePageChange(() -> {
            currentPage = page;
            updateUI();
            
            if (pageChangeListener != null) {
                pageChangeListener.onPageChanged(page);
            }
        });
    }

    private int dpToPx(int dp) {
        return Math.round(dp * paginationView.getResources().getDisplayMetrics().density);
    }
    
    private void jumpToPage(int page) {
        if (page < 1 || page > totalPages) {
            Log.w(TAG, "Invalid page number for jump: " + page);
            return;
        }
        
        animatePageChange(() -> {
            currentPage = page;
            updateUI();
            
            if (pageJumpListener != null) {
                pageJumpListener.onPageJump(page);
            }
        });
        
        // Hide quick jump and reset toggle button
        // no-op
    }
    
    private void animatePageChange(Runnable onComplete) {
        // Fade out animation
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(paginationView, "alpha", 1f, 0.5f);
        fadeOut.setDuration(150);
        
        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                onComplete.run();
                
                // Fade in animation
                ObjectAnimator fadeIn = ObjectAnimator.ofFloat(paginationView, "alpha", 0.5f, 1f);
                fadeIn.setDuration(150);
                fadeIn.start();
            }
        });
        
        fadeOut.start();
    }

    
    
    public void setVisible(boolean visible) {
        paginationView.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
    
    public View getView() {
        return paginationView;
    }
    
    public int getCurrentPage() {
        return currentPage;
    }
    
    public int getTotalPages() {
        return totalPages;
    }
    
    public int getTotalItems() {
        return totalItems;
    }
    
    public int getItemsPerPage() {
        return itemsPerPage;
    }
}
