package com.example.myreadbookapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myreadbookapplication.R;
import com.example.myreadbookapplication.model.Feedback;

import java.util.ArrayList;
import java.util.List;

public class AdminFeedbackAdapter extends RecyclerView.Adapter<AdminFeedbackAdapter.FeedbackViewHolder> {

    private List<Feedback> feedbackList;
    private OnFeedbackClickListener listener;

    public AdminFeedbackAdapter() {
        this.feedbackList = new ArrayList<>();
    }

    public interface OnFeedbackClickListener {
        void onFeedbackClick(Feedback feedback);
    }

    public void setOnFeedbackClickListener(OnFeedbackClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public FeedbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_feedback_admin, parent, false);
        return new FeedbackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedbackViewHolder holder, int position) {
        Feedback feedback = feedbackList.get(position);
        holder.bind(feedback);
    }

    @Override
    public int getItemCount() {
        return feedbackList != null ? feedbackList.size() : 0;
    }

    public void setFeedbackList(List<Feedback> feedbackList) {
        this.feedbackList = feedbackList;
        notifyDataSetChanged();
    }

    public void addFeedback(Feedback feedback) {
        feedbackList.add(feedback);
        notifyItemInserted(feedbackList.size() - 1);
    }

    public void clear() {
        feedbackList.clear();
        notifyDataSetChanged();
    }

    class FeedbackViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivUserIcon;
        private TextView tvEmail;
        private TextView tvFeedbackMessage;

        public FeedbackViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserIcon = itemView.findViewById(R.id.iv_user_icon);
            tvEmail = itemView.findViewById(R.id.tv_email);
            tvFeedbackMessage = itemView.findViewById(R.id.tv_feedback_message);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onFeedbackClick(feedbackList.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Feedback feedback) {
            // Display user email
            if (feedback.getEmail() != null && !feedback.getEmail().isEmpty()) {
                tvEmail.setText(feedback.getEmail());
            } else {
                tvEmail.setText("Anonymous");
            }

            // Display feedback message
            if (feedback.getComment() != null && !feedback.getComment().isEmpty()) {
                tvFeedbackMessage.setText(feedback.getComment());
            } else {
                tvFeedbackMessage.setText("No message");
            }
        }
    }
}


