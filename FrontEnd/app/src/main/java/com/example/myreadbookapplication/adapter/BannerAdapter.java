package com.example.myreadbookapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myreadbookapplication.R;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

    private List<Integer> bannerImages;
    private OnBannerClickListener onBannerClickListener;

    public interface OnBannerClickListener {
        void onBannerClick(int position);
    }

    public BannerAdapter(List<Integer> bannerImages, OnBannerClickListener listener) {
        this.bannerImages = bannerImages;
        this.onBannerClickListener = listener;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner, parent, false);
        // Đảm bảo view fill toàn bộ ViewPager2
        view.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        int imageRes = bannerImages.get(position);
        holder.bannerImage.setImageResource(imageRes);

        holder.itemView.setOnClickListener(v -> {
            if (onBannerClickListener != null) {
                onBannerClickListener.onBannerClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bannerImages.size();
    }

    static class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView bannerImage;

        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            bannerImage = itemView.findViewById(R.id.banner_image);
        }
    }
}