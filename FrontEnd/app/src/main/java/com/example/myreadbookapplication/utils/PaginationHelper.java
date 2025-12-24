package com.example.myreadbookapplication.utils;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.myreadbookapplication.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class để tạo pagination footer cho các Activity khác
 * Có thể sử dụng cho BooksActivity, CategoryActivity, SearchActivity...
 */
public class PaginationHelper {
    private static final String TAG = "PaginationHelper";
    
    /**
     * Tạo pagination footer cho Activity
     * @param context Context của Activity
     * @param parent ViewGroup để chứa pagination footer
     * @param onPageChangeListener Callback khi page thay đổi
     * @return PaginationManager instance
     */
    
    /**
     * Tạo pagination footer với cả page jump listener
     */
    public static PaginationManager createPaginationFooter(
            Context context, 
            ViewGroup parent, 
            PaginationManager.OnPageChangeListener onPageChangeListener,
            PaginationManager.OnPageJumpListener onPageJumpListener) {
        
        PaginationManager paginationManager = new PaginationManager(context, parent);
        paginationManager.setOnPageChangeListener(onPageChangeListener);
        paginationManager.setOnPageJumpListener(onPageJumpListener);
        
        return paginationManager;
    }
    
    /**
     * Cập nhật pagination data từ API response
     */
    public static void updatePaginationData(
            PaginationManager paginationManager,
            int currentPage,
            int totalPages,
            int totalItems,
            int itemsPerPage) {
        
        if (paginationManager != null) {
            paginationManager.setPaginationData(currentPage, totalPages, totalItems, itemsPerPage);
            paginationManager.setVisible(totalPages > 1);
        }
    }
    
    /**
     * Hiển thị loading state cho pagination
     */
    public static void showPaginationLoading(PaginationManager paginationManager, boolean show) {
        if (paginationManager != null) {
        }
    }
    
    /**
     * Ẩn pagination footer
     */
    public static void hidePagination(PaginationManager paginationManager) {
        if (paginationManager != null) {
            paginationManager.setVisible(false);
        }
    }

}