package com.example.myreadbookapplication.activity.User;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myreadbookapplication.R;
import com.example.myreadbookapplication.adapter.CategoryBookAdapter;
import com.example.myreadbookapplication.model.ApiResponse;
import com.example.myreadbookapplication.model.Book;
import com.example.myreadbookapplication.model.BooksResponse;
import com.example.myreadbookapplication.model.HistoryItem;
import com.example.myreadbookapplication.model.ReadingHistoryResponse;
import com.example.myreadbookapplication.network.ApiService;
import com.example.myreadbookapplication.network.RetrofitClient;
import com.example.myreadbookapplication.utils.AuthManager;
import com.example.myreadbookapplication.utils.PaginationManager;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryActivity extends AppCompatActivity {

    private ImageView backIconHistory;
    private ProgressBar progressBarHistoryBooks;
    private RecyclerView rvHistoryBook;
    private AuthManager authManager;
    private ApiService apiService;
    private FrameLayout paginationContainer;

    //Pagination state
    private PaginationManager paginationManager; //Quản lý phân trang
    private CategoryBookAdapter historyBookAdapter;
    private int currentPage = 1;
    private int totalPages = 1;
    private final int pageSize = 10;
    private final List<Book> historyBooks = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        initViews();
        loadHistoryBooks(); //Load ra trang đầu
        setupPagination();
        setupClickListeners();
    }

    private void initViews(){
        authManager = AuthManager.getInstance(this);
        apiService = RetrofitClient.getApiService();
        backIconHistory = findViewById(R.id.back_history_book_icon);
        progressBarHistoryBooks = findViewById(R.id.progressBar_history_books);
        paginationContainer = findViewById(R.id.pagination_container);
        rvHistoryBook = findViewById(R.id.rv_history_books);

        //setup adapter recyclerView
        rvHistoryBook.setLayoutManager(new GridLayoutManager(this, 2));
        historyBookAdapter = new CategoryBookAdapter(historyBooks, this, "History");
        rvHistoryBook.setAdapter(historyBookAdapter);
    }
    private void setupClickListeners() {
        backIconHistory.setOnClickListener(v -> finish());
    }

    private void setupPagination() {
        if(paginationContainer == null ) return;

        //Create paginationManager and add to container
        paginationManager = new PaginationManager(this, paginationContainer);
        //set click
        paginationManager.setOnPageChangeListener(page -> {
            currentPage = page;
            loadHistoryBooks();
        });

        paginationManager.setVisible(false);
    }

    private void loadHistoryBooks() {
        if(!authManager.isLoggedIn()){
            Toast.makeText(this, "You are not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = authManager.getUserId();
        String authHeader= authManager.getAuthorizationHeader();
        if(userId == null || authHeader == null){
            Toast.makeText(this, "lack of authentic information", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        //Call get all history book
        apiService.getReadingHistory(userId, authHeader, currentPage, pageSize, "lastReadAt", "desc")
                .enqueue(new Callback<ApiResponse<ReadingHistoryResponse>>() {

                    @Override
                    public void onResponse(Call<ApiResponse<ReadingHistoryResponse>> call, Response<ApiResponse<ReadingHistoryResponse>> response) {
                        showLoading(false);
                        ApiResponse<ReadingHistoryResponse> body = response.body();
                        if(!response.isSuccessful() || body == null || !body.isSuccess()){
                            handlError(response.code(), body != null ? body.getMessage() : null);
                            return;
                        }

                        ReadingHistoryResponse data = body.getData();
                        List<HistoryItem> historyItems = (data != null) ? data.getHistories() : null;

                        // Nếu rỗng thì clear và thoát
                        if (historyItems == null || historyItems.isEmpty()) {
                            historyBooks.clear();
                            historyBookAdapter.notifyDataSetChanged();
                            updatePagination(data);
                            return;
                        }

                        historyBooks.clear();
                        for ( HistoryItem hi : historyItems){
                            if(hi == null || hi.getBook() == null) continue;
                            Book book = hi.getBook();
                            String chapter = (hi.getChapterId() != null && !hi.getChapterId().isEmpty()) ? hi.getChapterId() : "?";
                            book.setTitle(book.getTitle() + " - chapter " +chapter);
                            historyBooks.add(book); ///chi co metadata chua co epuburl
                        }
                        historyBookAdapter.notifyDataSetChanged();
                        updatePagination(data);
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<ReadingHistoryResponse>> call, Throwable t) {
                        showLoading(false);
                        Log.e("HistoryActivity", "API failure: " + t.getMessage());
                        Toast.makeText(HistoryActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                        // Ẩn pagination khi failure
                        if (paginationManager != null) {
                            paginationManager.setVisible(false);
                        }
                    }
                });
    }

    private void updatePagination(ReadingHistoryResponse data) {
        if(data == null || data.getPagination() == null ){
            // An progressbar nếu không có dữ liệu
            if(paginationContainer != null ){
                paginationContainer.setVisibility(View.GONE);
            }
            return;
        }

        ReadingHistoryResponse.Pagination pagination = data.getPagination();

        int newCurrentPage = pagination.getPage();
        int totalPagesFromApi = pagination.getTotalPages();
        int totalItems = pagination.getTotal();

        //update state và UI
        currentPage = newCurrentPage;
        paginationManager.setPaginationData(
                currentPage,
                totalPagesFromApi,
                totalItems,
                pageSize
        );

        // Hiển thị nếu >1 trang
        boolean visible = totalPagesFromApi > 1;
        paginationManager.setVisible(visible);
        if (paginationContainer != null) {
            paginationContainer.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }


    private void updateBooks(List<HistoryItem> historyItems) {
        historyBooks.clear();
        if (historyItems != null){
            for (HistoryItem historyItem : historyItems) {
                if(historyItem == null || historyItem.getBook() == null) continue;
                Book book = historyItem.getBook();
                String chapter = historyItem.getChapterId() != null ? historyItem.getChapterId() : "?";
                book.setTitle(book.getTitle() + " - chapter " + chapter);
                historyBooks.add(book);
            }
        }
        historyBookAdapter.notifyDataSetChanged();
    }

    private void handlError(int code, String message) {
        String userMessage;
        if(code == 401){
            userMessage = "You are not logged in";
        }else if(code == 403){
            userMessage = "You are not allowed to access this resource";
        }else{
            userMessage = message != null ? message : "Something went wrong";
        }
        Toast.makeText(this, userMessage, Toast.LENGTH_SHORT).show();
    }

    private void showLoading(boolean show) {
        progressBarHistoryBooks.setVisibility(show? View.VISIBLE : View.GONE);
        rvHistoryBook.setVisibility(show? View.GONE : View.VISIBLE);

    }
}

