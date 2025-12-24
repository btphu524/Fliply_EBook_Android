package com.example.myreadbookapplication.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BooksResponse {
    @SerializedName("books")
    private List<Book> books;
    private BooksResponse.Pagination pagination;

    private Book book;
    public Book getBook(){ return book;}

    // Getters/Setters
    public List<Book> getBooks() { return books; }
    public void setBooks(List<Book> books) { this.books = books; }
    public BooksResponse.Pagination getPagination() { return pagination; }
    public void setPagination(BooksResponse.Pagination pagination) { this.pagination = pagination; }

    // Public static inner class to ensure visibility across packages
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