package com.example.myreadbookapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.bumptech.glide.Glide;  // Để load image từ URL
import androidx.recyclerview.widget.RecyclerView;

import com.example.myreadbookapplication.R;
import com.example.myreadbookapplication.model.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
    private List<Category> categories;
    private Context context;
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category); // truyen 1 cai category object
    }

    public CategoryAdapter(List<Category> categories, Context context, OnCategoryClickListener listener) {
        this.categories = categories;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.categoryName.setText(category.getName());

        // Load image từ BE imageUrl bằng Glide
        if(category.getImageUrl() != null && !category.getImageUrl().isEmpty()){
            Glide.with(context)
                    .load(category.getImageUrl())
                    .placeholder(R.drawable.default_book_cover) // ảnh mặc định
                    .error(R.drawable.default_book_cover) // ảnh khi lỗi
                    .into(holder.category_icon);
        } else {
            holder.category_icon.setImageResource(R.drawable.default_book_cover);
        }
        // Xử lý sự kiện click vào category
        holder.itemView.setOnClickListener(v -> listener.onCategoryClick(category));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName;
        ImageView category_icon;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.category_name);
            category_icon = itemView.findViewById(R.id.category_icon);
        }
    }
}
