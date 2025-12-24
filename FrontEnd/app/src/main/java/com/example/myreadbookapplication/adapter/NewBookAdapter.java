package com.example.myreadbookapplication.adapter;

import android.content.Context;
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
import com.example.myreadbookapplication.model.Book;
import com.example.myreadbookapplication.model.Category;
import com.example.myreadbookapplication.model.ApiResponse;
import com.example.myreadbookapplication.network.ApiService;
import com.example.myreadbookapplication.network.RetrofitClient;
import com.example.myreadbookapplication.utils.AuthManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class NewBookAdapter extends RecyclerView.Adapter<NewBookAdapter.ViewHolder> {
    private List<Book> newBooks;
    private Context context;
    private List<Category> categoriesList; // Thêm danh sách categories để map

    public NewBookAdapter(List<Book> newBooks, Context context, List<Category> categoriesList) {
        this.newBooks = newBooks;
        this.context = context;
        this.categoriesList = categoriesList;
    }
    
    // Constructor cũ để backward compatibility
    public NewBookAdapter(List<Book> newBooks, Context context) {
        this.newBooks = newBooks;
        this.context = context;
        this.categoriesList = null;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Book book = newBooks.get(position);
        holder.bookTitle.setText(book.getTitle());
        
        // Set category name
        String categoryName = book.getCategoryName();
        Log.d("NewBookAdapter", "Book: " + book.getTitle() + ", CategoryName: " + categoryName + ", Category ID: " + book.getCategory());
        
        if (categoryName != null && !categoryName.isEmpty()) {
            holder.bookCategory.setText(categoryName);
        } else if (categoriesList != null) {
            // Map category ID sang category name từ danh sách categories
            String mappedCategoryName = mapCategoryIdToName(book.getCategory());
            holder.bookCategory.setText(mappedCategoryName != null ? mappedCategoryName : "Unknown Category");
        } else {
            // Fallback: hiển thị category ID nếu không có category name
            holder.bookCategory.setText("Category " + book.getCategory());
        }

        // Load cover với Glide
        if (book.getCoverUrl() != null && !book.getCoverUrl().isEmpty()) {
            Glide.with(context)
                    .load(book.getCoverUrl())
                    .placeholder(R.drawable.default_book_cover)
                    .error(R.drawable.default_book_cover)
                    .into(holder.bookCover);
        } else {
            holder.bookCover.setImageResource(R.drawable.default_book_cover);
        }

        // Thêm favorite icon (mới)
        if (holder.ivFavorite != null) {
            String bookIdStr = book.getId();
            boolean isFavorite = isBookFavorite(bookIdStr);
            holder.ivFavorite.setImageResource(isFavorite ? R.drawable.ic_favorite_image : R.drawable.ic_favorite);

            holder.ivFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleFavorite(bookIdStr, holder.ivFavorite);
                }
            });
        } else {
            Log.w("NewBookAdapter", "iv_favorite ImageView not found in layout");
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.content.Intent intent = new android.content.Intent(context, ReadBookActivity.class);
                intent.putExtra("title", book.getTitle());
                intent.putExtra("cover_url", book.getCoverUrl());
                intent.putExtra("txt_url", book.getTxtUrl());
                intent.putExtra("book_url", book.getBookUrl());
                intent.putExtra("epub_url", book.getEpubUrl());
                intent.putExtra("book_id", book.getId());
                intent.putExtra("author", book.getAuthor());
                intent.putExtra("category", book.getCategoryName());
                context.startActivity(intent);
            }
        });
    }

    // Copy toggleFavorite từ CategoryBookAdapter
    private void toggleFavorite(String bookId, ImageView ivFavorite) {
        SharedPreferences prefs = context.getSharedPreferences("app_prefs", context.MODE_PRIVATE);
        String favoritesJson = prefs.getString("favorite_books", "[]");
        Gson gson = new Gson();
        Type type = new TypeToken<List<String>>(){}.getType();
        List<String> favorites = gson.fromJson(favoritesJson, type);
        if (favorites == null) favorites = new ArrayList<>();

        if (favorites.contains(bookId)) {
            favorites.remove(bookId);
            ivFavorite.setImageResource(R.drawable.ic_favorite);
            Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show();
            syncBackendFavorite(bookId, false);
        } else {
            favorites.add(bookId);
            ivFavorite.setImageResource(R.drawable.ic_favorite_image);
            Toast.makeText(context, "Added to favorites", Toast.LENGTH_SHORT).show();
            syncBackendFavorite(bookId, true);
        }

        prefs.edit().putString("favorite_books", gson.toJson(favorites)).apply();
    }

    // Copy isBookFavorite từ CategoryBookAdapter
    private boolean isBookFavorite(String bookId) {
        SharedPreferences prefs = context.getSharedPreferences("app_prefs", context.MODE_PRIVATE);
        String favoritesJson = prefs.getString("favorite_books", "[]");
        Gson gson = new Gson();
        Type type = new TypeToken<List<String>>(){}.getType();
        List<String> favorites = gson.fromJson(favoritesJson, type);
        return favorites != null && favorites.contains(bookId);
    }
    
    // Map category ID sang category name
    private String mapCategoryIdToName(int categoryId) {
        if (categoriesList != null) {
            for (Category category : categoriesList) {
                if (category.getId() == categoryId) {
                    return category.getName();
                }
            }
        }
        return null;
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
            retrofit2.Call<ApiResponse> call = add
                    ? api.addFavorite(userId, bookId, "Bearer " + token)
                    : api.removeFavorite(userId, bookId, "Bearer " + token);
            call.enqueue(new retrofit2.Callback<ApiResponse>() {
                @Override
                public void onResponse(retrofit2.Call<ApiResponse> call, retrofit2.Response<ApiResponse> response) {
                    // No-op for now
                }

                @Override
                public void onFailure(retrofit2.Call<ApiResponse> call, Throwable t) {
                    // No-op
                }
            });
        } catch (Exception ignored) {}
    }

    @Override
    public int getItemCount() {
        return newBooks.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView bookCover;
        TextView bookTitle;
        TextView bookCategory;
        ImageView ivFavorite;  // Mới: Thêm field

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            bookCover = itemView.findViewById(R.id.book_cover);
            bookTitle = itemView.findViewById(R.id.book_title);
            bookCategory = itemView.findViewById(R.id.book_category);
            ivFavorite = itemView.findViewById(R.id.iv_favorite);  // Ánh xạ
        }
    }
}