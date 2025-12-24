package com.example.myreadbookapplication.adapter;

import android.content.Context;
import android.content.SharedPreferences;
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
import com.example.myreadbookapplication.model.ApiResponse;
import com.example.myreadbookapplication.model.Book;
import com.example.myreadbookapplication.network.ApiService;
import com.example.myreadbookapplication.network.RetrofitClient;
import com.example.myreadbookapplication.utils.AuthManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllBooksAdapter extends RecyclerView.Adapter<AllBooksAdapter.ViewHolder> {
    private final List<Book> books;
    private final Context context;
    private final java.util.Map<Integer, String> categoryIdToName;

    public AllBooksAdapter(List<Book> books, Context context) {
        this(books, context, null);
    }

    public AllBooksAdapter(List<Book> books, Context context, java.util.Map<Integer, String> categoryIdToName) {
        this.books = books;
        this.context = context;
        this.categoryIdToName = categoryIdToName;
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
        holder.bookTitle.setText(book.getTitle());
        holder.bookTitle.setSelected(false);
        String catName = book.getCategoryName();
        if ((catName == null || catName.isEmpty()) && categoryIdToName != null) {
            String mapped = categoryIdToName.get(book.getCategory());
            catName = mapped != null ? mapped : "";
        }
        holder.bookCategory.setText(catName);

        if (book.getCoverUrl() != null && !book.getCoverUrl().isEmpty()) {
            Glide.with(context)
                    .load(book.getCoverUrl())
                    .placeholder(R.drawable.default_book_cover)
                    .error(R.drawable.default_book_cover)
                    .into(holder.bookCover);
        } else {
            holder.bookCover.setImageResource(R.drawable.default_book_cover);
        }

        String bookIdStr = book.getId();
        boolean isFavorite = isBookFavorite(bookIdStr);
        // Desired mapping: not favorite -> ic_favorite, favorite -> ic_favorite_image
        holder.ivFavorite.setImageResource(isFavorite ? R.drawable.ic_favorite_image : R.drawable.ic_favorite);

        holder.ivFavorite.setOnClickListener(v -> toggleFavorite(bookIdStr, holder.ivFavorite));

        // Create final variable for lambda
        final String finalCatName = catName;
        holder.itemView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(context, ReadBookActivity.class);
            intent.putExtra("title", book.getTitle());
            intent.putExtra("cover_url", book.getCoverUrl());
            intent.putExtra("txt_url", book.getTxtUrl());
            intent.putExtra("book_url", book.getBookUrl());
            intent.putExtra("epub_url", book.getEpubUrl());
            intent.putExtra("book_id", book.getId());
            intent.putExtra("author", book.getAuthor());
            intent.putExtra("category", finalCatName);
            context.startActivity(intent);
        });

        // Touch/hover to marquee title
        View.OnTouchListener marqueeTouch = (v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                case android.view.MotionEvent.ACTION_MOVE:
                    holder.bookTitle.setSelected(true); // required to start marquee
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    holder.bookTitle.setSelected(false);
                    break;
            }
            return false;
        };
        holder.itemView.setOnTouchListener(marqueeTouch);
        holder.itemView.setOnHoverListener((v, e) -> {
            if (e.getAction() == android.view.MotionEvent.ACTION_HOVER_ENTER) {
                holder.bookTitle.setSelected(true);
            } else if (e.getAction() == android.view.MotionEvent.ACTION_HOVER_EXIT) {
                holder.bookTitle.setSelected(false);
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView bookCover;
        TextView bookCategory;
        TextView bookTitle;
        ImageView ivFavorite;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            bookCover = itemView.findViewById(R.id.book_cover);
            bookCategory = itemView.findViewById(R.id.book_category);
            bookTitle = itemView.findViewById(R.id.book_title);
            ivFavorite = itemView.findViewById(R.id.iv_favorite);
        }
    }

    private boolean isBookFavorite(String bookId) {
        SharedPreferences prefs = context.getSharedPreferences("app_prefs", context.MODE_PRIVATE);
        String favoritesJson = prefs.getString("favorite_books", "[]");
        Gson gson = new Gson();
        Type type = new TypeToken<List<String>>(){}.getType();
        List<String> favorites = gson.fromJson(favoritesJson, type);
        return favorites != null && favorites.contains(bookId);
    }

    private void toggleFavorite(String bookId, ImageView ivFavorite) {
        SharedPreferences prefs = context.getSharedPreferences("app_prefs", context.MODE_PRIVATE);
        String favoritesJson = prefs.getString("favorite_books", "[]");
        Gson gson = new Gson();
        Type type = new TypeToken<List<String>>(){}.getType();
        List<String> favorites = gson.fromJson(favoritesJson, type);
        if (favorites == null) favorites = new ArrayList<>();

        boolean add;
        if (favorites.contains(bookId)) {
            favorites.remove(bookId);
            // Now not favorite -> show ic_favorite
            ivFavorite.setImageResource(R.drawable.ic_favorite);
            Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show();
            add = false;
        } else {
            favorites.add(bookId);
            // Now favorite -> show ic_favorite_image
            ivFavorite.setImageResource(R.drawable.ic_favorite_image);
            Toast.makeText(context, "You have added to favorites list", Toast.LENGTH_SHORT).show();
            add = true;
        }

        prefs.edit().putString("favorite_books", gson.toJson(favorites)).apply();
        syncBackendFavorite(bookId, add);
    }

    private void syncBackendFavorite(String bookId, boolean add) {
        try {
            AuthManager authManager = AuthManager.getInstance(context);
            String userId = authManager.getUserId();
            String token = authManager.getAccessToken();
            if (userId == null || token == null || token.isEmpty()) {
                return; // local only
            }
            ApiService api = RetrofitClient.getApiService();
            Call<ApiResponse> call = add
                    ? api.addFavorite(userId, bookId, "Bearer " + token)
                    : api.removeFavorite(userId, bookId, "Bearer " + token);
            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {}
                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {}
            });
        } catch (Exception ignored) {}
    }
}


