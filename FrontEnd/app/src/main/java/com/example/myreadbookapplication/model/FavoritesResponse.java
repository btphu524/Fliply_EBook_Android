package com.example.myreadbookapplication.model;

import java.util.List;

public class FavoritesResponse {
    private List<Book> favoriteBooks;
    private List<String> favoriteBookIds;
    private FavoritesResponse.Pagination pagination;

    public List<Book> getFavoriteBooks() {
        return favoriteBooks;
    }

    public void setFavoriteBooks(List<Book> favoriteBooks) {
        this.favoriteBooks = favoriteBooks;
    }

    public List<String> getFavoriteBookIds() {
        return favoriteBookIds;
    }

    public void setFavoriteBookIds(List<String> favoriteBookIds) {
        this.favoriteBookIds = favoriteBookIds;
    }

    public FavoritesResponse.Pagination getPagination() {
        return pagination;
    }

    public void setPagination(FavoritesResponse.Pagination pagination) {
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


