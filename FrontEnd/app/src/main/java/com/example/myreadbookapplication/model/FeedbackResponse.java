package com.example.myreadbookapplication.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class FeedbackResponse {
    @SerializedName("feedbacks")
    private List<Feedback> feedbacks;
    
    @SerializedName("pagination")
    private Pagination pagination;

    public FeedbackResponse() {}

    public List<Feedback> getFeedbacks() {
        return feedbacks;
    }

    public void setFeedbacks(List<Feedback> feedbacks) {
        this.feedbacks = feedbacks;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    public static class Pagination {
        @SerializedName("page")
        private int page;
        
        @SerializedName("limit")
        private int limit;
        
        @SerializedName("total")
        private int total;
        
        @SerializedName("totalPages")
        private int totalPages;

        public Pagination() {}

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public int getLimit() {
            return limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }
    }
}
