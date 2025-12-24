package com.example.myreadbookapplication.model;

import java.util.List;

public class ReadingHistoryResponse {
    private List<HistoryItem> histories;
    private Pagination pagination;

    public List<HistoryItem> getHistories() {
        return histories;
    }

    public void setHistories(List<HistoryItem> histories) {
        this.histories = histories;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    public static class Pagination {
        private int page;
        private int limit;
        private int total;
        private int totalPages;

        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }
        public int getLimit() { return limit; }
        public void setLimit(int limit) { this.limit = limit; }
        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }
        public int getTotalPages() { return totalPages; }
        public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    }
}


