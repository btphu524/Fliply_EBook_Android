package com.example.myreadbookapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myreadbookapplication.R;
import com.example.myreadbookapplication.model.Category;

import java.util.List;

public class AdminCategoryAdapter extends RecyclerView.Adapter<AdminCategoryAdapter.ViewHolder> {
    private List<Category> categories;
    private Context context;
    private OnCategoryActionListener listener;

    public interface OnCategoryActionListener {
        void onEditClick(Category category);
        void onDeleteClick(Category category);
    }

    public AdminCategoryAdapter(Context context, List<Category> categories) {
        this.context = context;
        this.categories = categories;
    }

    public void setOnCategoryActionListener(OnCategoryActionListener listener) {
        this.listener = listener;
    }

    public void updateCategoryList(List<Category> newCategories) {
        this.categories = newCategories;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categories.get(position);
        if (category == null) return;

        holder.tvCategoryName.setText(category.getName());

        // Load image using Glide with error handling
        if (category.getImageUrl() != null && !category.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(category.getImageUrl())
                    .placeholder(R.drawable.default_book_cover)
                    .error(R.drawable.default_book_cover)
                    .into(holder.ivCategoryImage);
        } else {
            holder.ivCategoryImage.setImageResource(R.drawable.default_book_cover);
        }

        // Edit button
        holder.ivEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(category);
            }
        });

        // Delete button
        holder.ivDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories != null ? categories.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCategoryImage;
        TextView tvCategoryName;
        ImageView ivEdit;
        ImageView ivDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategoryImage = itemView.findViewById(R.id.iv_category_image);
            tvCategoryName = itemView.findViewById(R.id.tv_category_name);
            ivEdit = itemView.findViewById(R.id.iv_edit);
            ivDelete = itemView.findViewById(R.id.iv_delete);
        }
    }
}

