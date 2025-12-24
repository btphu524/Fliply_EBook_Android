package com.example.myreadbookapplication.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myreadbookapplication.R;
import com.example.myreadbookapplication.model.epub.EpubModels;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChapterListAdapter extends RecyclerView.Adapter<ChapterListAdapter.ChapterViewHolder> {

    public interface OnChapterClickListener {
        void onChapterClicked(EpubModels.ChapterItem chapter, int position);
    }

    private final LayoutInflater inflater;
    private final OnChapterClickListener listener;
    private final List<EpubModels.ChapterItem> items = new ArrayList<>();
    private String currentChapterKey = "";

    public ChapterListAdapter(Context context, OnChapterClickListener listener) {
        this.inflater = LayoutInflater.from(context);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_chapter_list, parent, false);
        return new ChapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChapterViewHolder holder, int position) {
        holder.bind(items.get(position), position, isCurrentChapter(items.get(position)));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void submitList(List<EpubModels.ChapterItem> chapters) {
        items.clear();
        if (chapters != null) {
            items.addAll(chapters);
        }
        notifyDataSetChanged();
    }

    public void setCurrentChapterKey(String chapterKey) {
        currentChapterKey = normalizeKey(chapterKey);
        notifyDataSetChanged();
    }

    public int getCurrentPosition() {
        if (TextUtils.isEmpty(currentChapterKey)) return -1;
        for (int i = 0; i < items.size(); i++) {
            if (isCurrentChapter(items.get(i))) {
                return i;
            }
        }
        return -1;
    }

    private boolean isCurrentChapter(EpubModels.ChapterItem chapter) {
        if (chapter == null || TextUtils.isEmpty(currentChapterKey)) return false;
        return TextUtils.equals(currentChapterKey, normalizeKey(chapter.id))
                || TextUtils.equals(currentChapterKey, normalizeKey(chapter.href));
    }

    private String normalizeKey(String key) {
        if (key == null) return "";
        String normalized = key.trim().toLowerCase(Locale.ROOT).replace("\\", "/");
        int hash = normalized.indexOf('#');
        if (hash >= 0) normalized = normalized.substring(0, hash);
        if (normalized.startsWith("./")) normalized = normalized.substring(2);
        if (normalized.startsWith("oebps/")) normalized = normalized.substring("oebps/".length());
        if (normalized.startsWith("/")) normalized = normalized.substring(1);
        if (normalized.endsWith(".xhtml") || normalized.endsWith(".html")) {
            normalized = normalized.substring(0, normalized.lastIndexOf('.'));
        }
        return normalized;
    }

    private String stripExtension(String value) {
        if (TextUtils.isEmpty(value)) return value;
        int dot = value.lastIndexOf('.');
        if (dot > 0) {
            return value.substring(0, dot);
        }
        return value;
    }

    class ChapterViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final TextView tvOrder;
        private final TextView tvTitle;
        private final TextView tvSubtitle;
        private final TextView tvStatus;

        ChapterViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            tvOrder = itemView.findViewById(R.id.tv_chapter_order);
            tvTitle = itemView.findViewById(R.id.tv_chapter_title);
            tvSubtitle = itemView.findViewById(R.id.tv_chapter_subtitle);
            tvStatus = itemView.findViewById(R.id.tv_chapter_status);
        }

        void bind(EpubModels.ChapterItem item, int position, boolean isCurrent) {
            tvOrder.setText(String.format(Locale.getDefault(), "%02d", position + 1));

            String chapterLabel = itemView.getContext().getString(R.string.chapter_label_number, position + 1);
            tvTitle.setText(chapterLabel);

            String detail = buildSubtitle(item);
            if (!TextUtils.isEmpty(detail)) {
                tvSubtitle.setText(detail);
                tvSubtitle.setVisibility(View.VISIBLE);
            } else {
                tvSubtitle.setVisibility(View.GONE);
            }

            tvStatus.setVisibility(isCurrent ? View.VISIBLE : View.GONE);
            cardView.setStrokeWidth(isCurrent ? 2 : 1);
            cardView.setStrokeColor(ContextCompat.getColor(itemView.getContext(), isCurrent ? R.color.ping : R.color.light_gray));
            cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), isCurrent ? R.color.ping_blur : R.color.white));

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onChapterClicked(item, position);
                }
            });
        }
    }

    private String buildSubtitle(EpubModels.ChapterItem item) {
        if (item == null) return "";
        if (!TextUtils.isEmpty(item.title)) {
            return prettyText(item.title);
        }
        if (!TextUtils.isEmpty(item.href)) {
            return prettyText(stripExtension(normalizeKey(item.href)));
        }
        if (!TextUtils.isEmpty(item.id)) {
            return prettyText(normalizeKey(item.id));
        }
        return "";
    }

    private String prettyText(String raw) {
        if (TextUtils.isEmpty(raw)) return "";
        String cleaned = raw.replace('_', ' ')
                .replace('-', ' ')
                .replaceAll("\\s+", " ")
                .trim();
        if (cleaned.length() == 0) return "";
        if (!containsLetters(cleaned)) return "";
        return cleaned.substring(0, 1).toUpperCase(Locale.getDefault()) + cleaned.substring(1);
    }

    private boolean containsLetters(String text) {
        if (TextUtils.isEmpty(text)) return false;
        for (int i = 0; i < text.length(); i++) {
            if (Character.isLetter(text.charAt(i))) {
                return true;
            }
        }
        return false;
    }
}

