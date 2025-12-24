package com.example.myreadbookapplication.model.epub;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class EpubModels {

    public static class EpubUrlRequest {
        @SerializedName("epub_url")
        public String epubUrl;
        public EpubUrlRequest(String epubUrl) { this.epubUrl = epubUrl; }
    }

    public static class EpubChapterContentRequest {
        @SerializedName("epub_url")
        public String epubUrl;
        @SerializedName("chapter_id")
        public String chapterId;
        public EpubChapterContentRequest(String epubUrl, String chapterId) {
            this.epubUrl = epubUrl;
            this.chapterId = chapterId;
        }
    }

    // Metadata response
    public static class EpubMetadataData {
        public Metadata metadata;
        public Integer totalChapters;
        public List<TocItem> toc;
    }

    public static class Metadata {
        public String title;
        public String creator;
        public String publisher;
        public String language;
        public String description;
        public String subject;
        public String date;
        public String rights;
    }

    public static class TocItem {
        public String id;
        public String title;
        public String href;
        public Integer level;
    }

    // Chapters response
    public static class EpubChaptersData {
        public List<ChapterItem> chapters;
        public Integer totalChapters;
    }

    public static class ChapterItem {
        public String id;
        public String title;
        public String href;
        public Integer level;
    }

    // Chapter content response
    public static class EpubChapterContentData {
        public String chapterId;
        public String title;
        public String content; // HTML string
    }
}


