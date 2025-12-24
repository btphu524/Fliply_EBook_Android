package com.example.myreadbookapplication.model;

import java.util.List;

public class UpdateBookRequest {
    public String title;
    public String author; // optional when updating
    public Integer category; // nullable
    public String description;
    public String release_date;
    public String cover_url;
    public String txt_url;
    public String book_url;
    public String epub_url;
    public List<String> keywords;
    public String status;
}


