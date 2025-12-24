package com.example.myreadbookapplication.model;

import com.google.gson.annotations.SerializedName;

public class HistoryItem {
    @SerializedName("_id")
    private String id;
    private int userId;
    private int bookId;
    private int page;
    private String chapterId;
    private long lastReadAt;
    private long createdAt;
    private long updatedAt;
    private Book book;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public String getChapterId() { return chapterId; }
    public void setChapterId(String chapterId) { this.chapterId = chapterId; }
    public long getLastReadAt() { return lastReadAt; }
    public void setLastReadAt(long lastReadAt) { this.lastReadAt = lastReadAt; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }
}


