package com.example.myreadbookapplication.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myreadbookapplication.R;
import com.example.myreadbookapplication.activity.User.ReadBookActivity;
import com.example.myreadbookapplication.activity.User.HistoryActivity;
import com.example.myreadbookapplication.model.Book;
import com.example.myreadbookapplication.model.BooksResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.example.myreadbookapplication.model.ApiResponse;
import com.example.myreadbookapplication.network.ApiService;
import com.example.myreadbookapplication.network.RetrofitClient;
import com.example.myreadbookapplication.utils.AuthManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryBookAdapter extends RecyclerView.Adapter<CategoryBookAdapter.ViewHolder> {
    private List<Book> books;
    private Context context;
    private String categoryName;

    public CategoryBookAdapter(List<Book> books, Context context, String categoryName) {
        this.books = books;
        this.context = context;
        this.categoryName = categoryName;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Book book = books.get(position);
        holder.bookTitle.setText(book.getTitle() + " - " + book.getAuthor());
        
        // Set category name - sử dụng categoryName từ constructor
        if (categoryName != null && !categoryName.isEmpty()) {
            holder.bookCategory.setText(categoryName);
        } else if (book.getCategoryName() != null && !book.getCategoryName().isEmpty()) {
            holder.bookCategory.setText(book.getCategoryName());
        } else {
            holder.bookCategory.setText("Unknown Category");
        }

        // Load cover
        if (book.getCoverUrl() != null && !book.getCoverUrl().isEmpty()) {
            Glide.with(context)
                    .load(book.getCoverUrl())
                    .placeholder(R.drawable.default_book_cover)
                    .error(R.drawable.default_book_cover)
                    .into(holder.bookCover);
        } else {
            holder.bookCover.setImageResource(R.drawable.default_book_cover);
        }

        if (holder.ivFavorite != null) {
            // Set icon dựa trên prefs
            String bookIdStr = book.getId();
            boolean isFavorite = isBookFavorite(bookIdStr);
            holder.ivFavorite.setImageResource(isFavorite ? R.drawable.ic_favorite_image : R.drawable.ic_favorite);

            holder.ivFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleFavorite(bookIdStr, holder.ivFavorite);  // Pass String ID
                }
            });
        } else {
            Log.w("CategoryBookAdapter", "iv_favorite ImageView not found in layout");
        }

        holder.itemView.setOnClickListener(v -> {
            if ("History".equals(categoryName) && isMissingContentUrl(book)) {
                fetchBookAndOpen(book.getId());
            } else {
                openReader(book);
            }
        });

    }

    private void fetchBookAndOpen(String bookId) {
        RetrofitClient.getApiService().getBookById(bookId).enqueue(new Callback<ApiResponse<BooksResponse>>() {

            @Override
            public void onResponse(Call<ApiResponse<BooksResponse>> call, Response<ApiResponse<BooksResponse>> response) {
                if(response.isSuccessful() && response.body() != null && response.body().isSuccess()){
                    openReader(response.body().getData().getBook());
                }else {
                    Toast.makeText(context, "Unable to get book information", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<BooksResponse>> call, Throwable t) {
                Toast.makeText(context, "Network error " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isMissingContentUrl(Book book) {
        return book.getEpubUrl() == null || book.getEpubUrl().isEmpty();
    }

    private void openReader(Book book) {
        Intent intent = new Intent(context, ReadBookActivity.class);
        intent.putExtra("title", book.getTitle());
        intent.putExtra("cover_url", book.getCoverUrl());
        intent.putExtra("txt_url", book.getTxtUrl());
        intent.putExtra("book_url", book.getBookUrl());
        intent.putExtra("epub_url", book.getEpubUrl());
        intent.putExtra("book_id", book.getId());
        intent.putExtra("author", book.getAuthor());
        intent.putExtra("category", categoryName != null ? categoryName : book.getCategoryName());
        context.startActivity(intent);
    }

    // toggleFavorite: Nhận String bookId, dùng List<String>
    private void toggleFavorite(String bookId, ImageView ivFavorite) {
        SharedPreferences prefs = context.getSharedPreferences("app_prefs", context.MODE_PRIVATE);
        String favoritesJson = prefs.getString("favorite_books", "[]");  // Key thống nhất
        Gson gson = new Gson();
        Type type = new TypeToken<List<String>>(){}.getType();
        List<String> favorites = gson.fromJson(favoritesJson, type);
        if (favorites == null) favorites = new ArrayList<>();

        if (favorites.contains(bookId)) {
            favorites.remove(bookId);  // Remove String
            ivFavorite.setImageResource(R.drawable.ic_favorite);
            Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show();
            syncBackendFavorite(bookId, false);
        } else {
            favorites.add(bookId);  // Add String
            ivFavorite.setImageResource(R.drawable.ic_favorite_image);
            Toast.makeText(context, "You have added to favorites list", Toast.LENGTH_SHORT).show();  // Message mới
            syncBackendFavorite(bookId, true);
        }

        // Lưu lại
        prefs.edit().putString("favorite_books", gson.toJson(favorites)).apply();
    }

    private void syncBackendFavorite(String bookId, boolean add) {
        try {
            AuthManager authManager = AuthManager.getInstance(context);
            String userId = authManager.getUserId();
            String token = authManager.getAccessToken();
            if (userId == null || token == null || token.isEmpty()) {
                return; // not logged in; local only
            }
            ApiService api = RetrofitClient.getApiService();
            Call<ApiResponse> call = add
                    ? api.addFavorite(userId, bookId, "Bearer " + token)
                    : api.removeFavorite(userId, bookId, "Bearer " + token);
            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    // No-op; optimistic UI already updated
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    // Optional: could revert local on failure
                }
            });
        } catch (Exception ignored) {}
    }

    // isBookFavorite: Nhận String bookId
    private boolean isBookFavorite(String bookId) {
        SharedPreferences prefs = context.getSharedPreferences("app_prefs", context.MODE_PRIVATE);
        String favoritesJson = prefs.getString("favorite_books", "[]");  // Key thống nhất
        Gson gson = new Gson();
        Type type = new TypeToken<List<String>>(){}.getType();
        List<String> favorites = gson.fromJson(favoritesJson, type);
        return favorites != null && favorites.contains(bookId);
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    /**
     * Hiển thị dialog xác nhận xóa bookmark
     */
    private void showDeleteBookmarkDialog(Book book) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Remove Bookmark");
        builder.setMessage("Are you sure you want to remove the bookmark for \"" + book.getTitle() + "\"?");
        
        builder.setPositiveButton("Remove", (dialog, which) -> {
            try {
                int bookId = Integer.parseInt(book.getId());
                // Gọi HistoryActivity để xóa bookmark
                if (context instanceof HistoryActivity) {
                    //((HistoryActivity) context).deleteBookmark(bookId);
                }
            } catch (NumberFormatException e) {
                Log.e("CategoryBookAdapter", "Invalid book ID: " + book.getId());
                Toast.makeText(context, "Error: Book ID is invalid", Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        });
        
        builder.show();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView bookCover;
        TextView bookTitle;
        TextView bookCategory;
        ImageView ivFavorite;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            bookCover = itemView.findViewById(R.id.book_cover);
            bookTitle = itemView.findViewById(R.id.book_title);
            bookCategory = itemView.findViewById(R.id.book_category);
            ivFavorite = itemView.findViewById(R.id.iv_favorite);
        }
    }
}