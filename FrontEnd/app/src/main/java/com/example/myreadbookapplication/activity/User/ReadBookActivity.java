package com.example.myreadbookapplication.activity.User;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.bumptech.glide.Glide;
import com.example.myreadbookapplication.BuildConfig;
import com.example.myreadbookapplication.R;
import com.example.myreadbookapplication.adapter.ChapterListAdapter;
import com.example.myreadbookapplication.model.ApiResponse;
import com.example.myreadbookapplication.model.epub.EpubModels;
import com.example.myreadbookapplication.model.epub.EpubModels.EpubUrlRequest;
import com.example.myreadbookapplication.model.epub.EpubModels.EpubMetadataData;
import com.example.myreadbookapplication.model.epub.EpubModels.EpubChaptersData;
import com.example.myreadbookapplication.model.epub.EpubModels.EpubChapterContentData;
import com.example.myreadbookapplication.model.epub.EpubModels.EpubChapterContentRequest;
import com.example.myreadbookapplication.network.ApiService;
import com.example.myreadbookapplication.network.RetrofitClient;
import com.example.myreadbookapplication.utils.AuthManager;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReadBookActivity extends AppCompatActivity {

    private WebView webViewRef;
    private ApiService apiRef;
    private String currentEpubUrl;
    private final Map<String, String> hrefToId = new HashMap<>();
    private String currentBookId;
    private int currentPage = 1; // logical page index for non-epub
    private String currentChapterId; // for epub bookmarking
    private int currentScrollPosition = 0; // for tracking scroll position within chapter
    private android.os.Handler scrollSaveHandler = new android.os.Handler();
    private Runnable scrollSaveRunnable;
    
    // Menu dropdown variables
    private PopupWindow menuPopup;
    private boolean isNightMode = false;
    private boolean isFavorite = false;
    
    // New UI elements
    private TextView tvBookTitle;
    private TextView tvAuthor;
    private ProgressBar progressBar;
    private ImageView btnFontDecrease;
    private ImageView btnFontIncrease;
    private MaterialButton btnPrevChapter;
    private MaterialButton btnNextChapter;
    private MaterialButton btnShowChapters;
    private View chapterNavigationContainer;
    private TextView tvCurrentChapter;
    
    // Font size management
    private int currentFontSize = 30; // Default font size
    private final List<com.example.myreadbookapplication.model.epub.EpubModels.ChapterItem> chapterItems = new ArrayList<>();
    private final Map<String, Integer> chapterIndexMap = new HashMap<>();
    private final Map<String, String> chapterTitleHints = new HashMap<>();
    private String defaultChapterKey;
    private String pendingChapterId;
    private BottomSheetDialog chapterSheetDialog;
    private ChapterListAdapter chapterListAdapter;
    private RecyclerView chapterRecycler;
    private TextView sheetTitleView;
    private TextView sheetCountView;
    private View sheetEmptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_read_book);

        ImageView backIcon = findViewById(R.id.back_icon);
        TextView tvTitle = findViewById(R.id.tv_title);
        ImageView ivCover = findViewById(R.id.iv_cover);
        WebView webView = findViewById(R.id.web_view);
        ImageView menuInBook = findViewById(R.id.menu_in_book);
        
        // Initialize new UI elements
        tvBookTitle = findViewById(R.id.tv_book_title);
        tvAuthor = findViewById(R.id.tv_author);
        progressBar = findViewById(R.id.progress_bar);
        btnFontDecrease = findViewById(R.id.btn_font_decrease);
        btnFontIncrease = findViewById(R.id.btn_font_increase);
        btnPrevChapter = findViewById(R.id.btn_prev_chapter);
        btnNextChapter = findViewById(R.id.btn_next_chapter);
        btnShowChapters = findViewById(R.id.btn_show_chapters);
        chapterNavigationContainer = findViewById(R.id.chapter_navigation_container);
        if (chapterNavigationContainer != null) {
            chapterNavigationContainer.setVisibility(View.GONE);
        }
        if (tvCurrentChapter != null) {
            tvCurrentChapter.setText(getString(R.string.chapter_current_placeholder));
        }
        if (btnShowChapters != null) {
            btnShowChapters.setEnabled(false);
            btnShowChapters.setAlpha(0.5f);
        }
        if (btnPrevChapter != null) {
            btnPrevChapter.setOnClickListener(v -> openAdjacentChapter(-1));
        }
        if (btnNextChapter != null) {
            btnNextChapter.setOnClickListener(v -> openAdjacentChapter(1));
        }
        if (btnShowChapters != null) {
            btnShowChapters.setOnClickListener(v -> showChapterSheet());
        }
        
        // Load saved states
        loadSavedStates();
        
        // Setup menu dropdown
        setupMenuDropdown(menuInBook);
        
        // Setup click listeners
        backIcon.setOnClickListener(v -> finish());
        menuInBook.setOnClickListener(v -> showMenuDropdown(menuInBook));
        this.webViewRef = webView;

        String title = getIntent().getStringExtra("title"); // get intent from newBookAdapter
        String coverUrl = getIntent().getStringExtra("cover_url");
        String txtUrl = getIntent().getStringExtra("txt_url");
        String bookUrl = getIntent().getStringExtra("book_url");
        String epubUrl = getIntent().getStringExtra("epub_url");
        String author = getIntent().getStringExtra("author");
        this.currentBookId = getIntent().getStringExtra("book_id");

        // Setup header title
        tvTitle.setText(title != null ? title : "");
        setupTitleScrolling(tvTitle, title);
        
        // Setup book info
        setupBookInfo(title, author, coverUrl);
        
        // Setup font controls
        setupFontControls();

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleWebLink(url, view, tvTitle);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return handleWebLink(request.getUrl().toString(), view, tvTitle);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Restore scroll position if available
                restoreScrollPosition();
                // Start auto-save scroll position
                startAutoSaveScrollPosition();
                // Apply initial font size
                updateWebViewFontSize();
                if (currentEpubUrl != null && !currentEpubUrl.isEmpty()) {
                    view.clearHistory();
                }
            }
        });

        //CORE
        if (epubUrl != null && !epubUrl.isEmpty()) {
            // New flow: use backend EPUB APIs
            ApiService api = RetrofitClient.getApiService();
            this.apiRef = api;
            this.currentEpubUrl = epubUrl;
            // 1) Validate URL
            api.validateEpubUrl(new EpubUrlRequest(epubUrl)).enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        // 2) Fetch metadata and chapters in parallel, then load first chapter
                        fetchChaptersAndOpenFirst(api, epubUrl, webView, tvTitle);
                        // Try resuming from bookmark if available
                        resumeFromBookmarkIfAny();
                    } else {
                        // Fallback to direct URL flow if validation fails
                        fallbackDirectLoad(webView, bookUrl, txtUrl, epubUrl);
                    }
                }
                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    fallbackDirectLoad(webView, bookUrl, txtUrl, epubUrl);
                }
            });
        } else {
            // No epub url; fallback to prior behavior for book/txt urls
            fallbackDirectLoad(webView, bookUrl, txtUrl, null);
        }
        backIcon.setOnClickListener(v -> handleBack());
    }

    private void fetchChaptersAndOpenFirst(ApiService api, String epubUrl, WebView webView, TextView tvTitle) {
        chapterTitleHints.clear();
        defaultChapterKey = null;
        prepareChapterTitles(api, epubUrl, () -> requestChapters(api, epubUrl, webView, tvTitle));
    }

    private void prepareChapterTitles(ApiService api, String epubUrl, Runnable onComplete) {
        api.getEpubMetadata(new EpubUrlRequest(epubUrl)).enqueue(new Callback<ApiResponse<EpubMetadataData>>() {
            @Override
            public void onResponse(Call<ApiResponse<EpubMetadataData>> call, Response<ApiResponse<EpubMetadataData>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()
                        && response.body().getData() != null && response.body().getData().toc != null) {
                    for (EpubModels.TocItem item : response.body().getData().toc) {
                        if (item == null) continue;
                        String cleanTitle = sanitizeTitle(item.title);
                        if (TextUtils.isEmpty(cleanTitle)) continue;
                        storeChapterTitleHint(item.id, cleanTitle);
                        storeChapterTitleHint(item.href, cleanTitle);
                        if (defaultChapterKey == null && isLikelyContentTitle(cleanTitle)) {
                            defaultChapterKey = !TextUtils.isEmpty(item.id) ? item.id : item.href;
                        }
                    }
                }
                if (onComplete != null) onComplete.run();
            }
            @Override
            public void onFailure(Call<ApiResponse<EpubMetadataData>> call, Throwable t) {
                if (onComplete != null) onComplete.run();
            }
        });
    }

    private void requestChapters(ApiService api, String epubUrl, WebView webView, TextView tvTitle) {
        api.getEpubChapters(new EpubUrlRequest(epubUrl)).enqueue(new Callback<ApiResponse<EpubChaptersData>>() {
            @Override
            public void onResponse(Call<ApiResponse<EpubChaptersData>> call, Response<ApiResponse<EpubChaptersData>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess() &&
                        response.body().getData() != null && response.body().getData().chapters != null && !response.body().getData().chapters.isEmpty()) {
                    hrefToId.clear();
                    chapterItems.clear();
                    chapterIndexMap.clear();
                    if (chapterNavigationContainer != null) {
                        chapterNavigationContainer.setVisibility(View.GONE);
                    }

                    List<com.example.myreadbookapplication.model.epub.EpubModels.ChapterItem> chapters = response.body().getData().chapters;
                    Set<String> seenChapterKeys = new HashSet<>();
                    for (int i = 0; i < chapters.size(); i++) {
                        com.example.myreadbookapplication.model.epub.EpubModels.ChapterItem c = chapters.get(i);
                        if (c == null) continue;
                        if (c.href != null && c.id != null) {
                            hrefToId.put(c.href, c.id);
                        }
                        applyTitleHint(c);
                        if (!shouldDisplayChapter(c)) {
                            continue;
                        }
                        String uniqueKey = getChapterUniqueKey(c);
                        if (!TextUtils.isEmpty(uniqueKey) && !seenChapterKeys.add(uniqueKey)) {
                            continue;
                        }
                        int positionIndex = chapterItems.size();
                        chapterItems.add(c);
                        indexChapter(c.id, positionIndex);
                        indexChapter(c.href, positionIndex);
                    }
                    updateChapterSheet();
                    if (btnShowChapters != null) {
                        boolean hasChapters = !chapterItems.isEmpty();
                        btnShowChapters.setVisibility(hasChapters ? View.VISIBLE : View.GONE);
                        btnShowChapters.setEnabled(hasChapters);
                        btnShowChapters.setAlpha(hasChapters ? 1f : 0.5f);
                    }

                    boolean hasReadable = false;
                    for (com.example.myreadbookapplication.model.epub.EpubModels.ChapterItem c : chapterItems) {
                        if (isNavigableChapter(c)) {
                            hasReadable = true;
                            break;
                        }
                    }
                    if (chapterNavigationContainer != null) {
                        chapterNavigationContainer.setVisibility(hasReadable ? View.VISIBLE : View.GONE);
                    }

                    String chosenId = selectInitialChapterId();
                    if (!TextUtils.isEmpty(chosenId)) {
                        openChapter(api, epubUrl, chosenId, webView, tvTitle);
                        if (!TextUtils.isEmpty(pendingChapterId) && pendingChapterId.equals(chosenId)) {
                            pendingChapterId = null;
                        }
                    }
                } else {
                    Toast.makeText(ReadBookActivity.this, "No chapters found", Toast.LENGTH_SHORT).show();
                    if (btnShowChapters != null) {
                        btnShowChapters.setVisibility(View.GONE);
                    }
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<EpubChaptersData>> call, Throwable t) {
                Toast.makeText(ReadBookActivity.this, "Failed to load chapters", Toast.LENGTH_SHORT).show();
                if (btnShowChapters != null) {
                    btnShowChapters.setVisibility(View.GONE);
                }
            }
        });
    }

    private void storeChapterTitleHint(String rawKey, String rawTitle) {
        String cleanTitle = sanitizeTitle(rawTitle);
        if (TextUtils.isEmpty(cleanTitle)) return;
        String normalized = normalizeChapterKey(rawKey);
        putChapterTitleHint(normalized, cleanTitle);
        String base = stripExtension(normalized);
        putChapterTitleHint(base, cleanTitle);
    }

    private void putChapterTitleHint(String key, String title) {
        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(title)) return;
        chapterTitleHints.put(key, title);
    }

    private void applyTitleHint(com.example.myreadbookapplication.model.epub.EpubModels.ChapterItem chapter) {
        if (chapter == null) return;
        String hint = getChapterTitleHint(chapter);
        if (!TextUtils.isEmpty(hint)) {
            chapter.title = hint;
        }
    }

    private String getChapterTitleHint(com.example.myreadbookapplication.model.epub.EpubModels.ChapterItem chapter) {
        if (chapter == null) return null;
        String directTitle = sanitizeTitle(chapter.title);
        if (!TextUtils.isEmpty(directTitle)) return directTitle;

        String normalizedId = normalizeChapterKey(chapter.id);
        String normalizedHref = normalizeChapterKey(chapter.href);
        String baseId = stripExtension(normalizedId);
        String baseHref = stripExtension(normalizedHref);

        return firstNonEmpty(
                sanitizeTitle(chapterTitleHints.get(normalizedId)),
                sanitizeTitle(chapterTitleHints.get(baseId)),
                sanitizeTitle(chapterTitleHints.get(normalizedHref)),
                sanitizeTitle(chapterTitleHints.get(baseHref))
        );
    }

    private String fetchStoredTitleHint(String rawKey) {
        String normalized = normalizeChapterKey(rawKey);
        String base = stripExtension(normalized);
        return firstNonEmpty(chapterTitleHints.get(normalized), chapterTitleHints.get(base));
    }

    private String sanitizeTitle(String raw) {
        if (TextUtils.isEmpty(raw)) return null;
        String cleaned = raw.replace('_', ' ')
                .replace('-', ' ')
                .replaceAll("\\s+", " ")
                .trim();
        if (cleaned.length() == 0) return null;
        if (!containsLetters(cleaned)) return null;
        return cleaned.substring(0, 1).toUpperCase(Locale.getDefault()) + cleaned.substring(1);
    }

    private String firstNonEmpty(String... values) {
        if (values == null) return null;
        for (String value : values) {
            if (!TextUtils.isEmpty(value)) {
                return value;
            }
        }
        return null;
    }

    private boolean isLikelyContentTitle(String title) {
        if (TextUtils.isEmpty(title)) return false;
        String lower = title.toLowerCase(Locale.getDefault());
        if (lower.contains("cover") || lower.contains("project gutenberg") || lower.contains("license")) return false;
        if (lower.contains("table of contents") || lower.equals("contents") || lower.equals("toc")) return false;
        return true;
    }

    private String selectInitialChapterId() {
        String chosenId = null;
        if (!TextUtils.isEmpty(pendingChapterId) && getChapterIndex(pendingChapterId) != null) {
            return pendingChapterId;
        }

        if (!TextUtils.isEmpty(defaultChapterKey)) {
            chosenId = findChapterIdByKey(defaultChapterKey);
            if (!TextUtils.isEmpty(chosenId)) {
                return chosenId;
            }
        }

        for (com.example.myreadbookapplication.model.epub.EpubModels.ChapterItem item : chapterItems) {
            if (item == null) continue;
            String title = sanitizeTitle(item.title);
            if (!TextUtils.isEmpty(title) && isLikelyContentTitle(title)) {
                chosenId = toChapterId(item);
                if (!TextUtils.isEmpty(chosenId)) {
                    return chosenId;
                }
            }
        }

        for (com.example.myreadbookapplication.model.epub.EpubModels.ChapterItem c : chapterItems) {
            if (c == null) continue;
            if (isNavigableChapter(c)) {
                chosenId = toChapterId(c);
                if (!TextUtils.isEmpty(chosenId)) {
                    return chosenId;
                }
            }
        }

        if (!chapterItems.isEmpty()) {
            com.example.myreadbookapplication.model.epub.EpubModels.ChapterItem first = chapterItems.get(0);
            if (first != null) {
                chosenId = toChapterId(first);
            }
        }
        return chosenId;
    }

    private String findChapterIdByKey(String key) {
        if (TextUtils.isEmpty(key)) return null;
        Integer idx = getChapterIndex(key);
        if (idx != null && idx >= 0 && idx < chapterItems.size()) {
            com.example.myreadbookapplication.model.epub.EpubModels.ChapterItem item = chapterItems.get(idx);
            if (item != null) {
                return toChapterId(item);
            }
        }
        return null;
    }

    private String toChapterId(com.example.myreadbookapplication.model.epub.EpubModels.ChapterItem item) {
        if (item == null) return null;
        if (!TextUtils.isEmpty(item.id)) return item.id;
        if (!TextUtils.isEmpty(item.href)) return item.href;
        return null;
    }

    private void openChapter(ApiService api, String epubUrl, String chapterId, WebView webView, TextView tvTitle) {
        // Save current scroll position before switching chapters
        if (this.currentChapterId != null && !this.currentChapterId.equals(chapterId)) {
            saveCurrentScrollPosition();
        }
        
        this.currentChapterId = chapterId;
        Integer knownIndex = getChapterIndex(chapterId);
        if (chapterId != null && knownIndex == null && !chapterItems.isEmpty()) {
            for (int i = 0; i < chapterItems.size(); i++) {
                com.example.myreadbookapplication.model.epub.EpubModels.ChapterItem item = chapterItems.get(i);
                if (item == null) continue;
                if (matchesChapterKey(item, chapterId)) {
                    indexChapter(item.id, i);
                    indexChapter(item.href, i);
                    indexChapter(chapterId, i);
                    knownIndex = i;
                    break;
                }
            }
        }
        updateChapterNavigationButtons();
        // Load saved scroll position for the new chapter
        loadSavedScrollPosition();
        
        api.getEpubChapterContent(new EpubChapterContentRequest(epubUrl, chapterId))
                .enqueue(new Callback<ApiResponse<EpubChapterContentData>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<EpubChapterContentData>> call, Response<ApiResponse<EpubChapterContentData>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                            EpubChapterContentData data = response.body().getData();
                            if (data.title != null && !data.title.isEmpty()) {
                                updateCurrentChapterLabel(data.title);
                            } else {
                                updateCurrentChapterLabel(chapterId);
                            }
                            // Render HTML string; no external URL loaded
                            String html = data.content != null ? data.content : "";
                            // Minimal readable defaults
                            String style = "<style> body{padding:16px; line-height:1.6; font-size:16px;} img{max-width:100%; height:auto;} </style>";
                            String doc = "<html><head>" + style + "</head><body>" + html + "</body></html>";
                            webView.setVisibility(View.VISIBLE);
                            // Use backend base URL so relative resources like /images resolve
                            webView.loadDataWithBaseURL(BuildConfig.BASE_URL, doc, "text/html", "utf-8", null);
                        } else {
                            Toast.makeText(ReadBookActivity.this, "Failed to load chapter", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<ApiResponse<EpubChapterContentData>> call, Throwable t) {
                        Toast.makeText(ReadBookActivity.this, "Failed to load chapter", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openAdjacentChapter(int direction) {
        if (direction == 0) return;
        if (apiRef == null || currentEpubUrl == null || webViewRef == null || chapterItems.isEmpty()) return;

        Integer currentIndex = getChapterIndex(currentChapterId);
        int referenceIndex = currentIndex != null ? currentIndex : (direction > 0 ? -1 : chapterItems.size());
        Integer targetIndex = findNavigableIndex(referenceIndex, direction);
        if (targetIndex == null) {
            return;
        }
        com.example.myreadbookapplication.model.epub.EpubModels.ChapterItem target = chapterItems.get(targetIndex);
        if (target == null || target.id == null || target.id.isEmpty()) {
            return;
        }
        TextView titleView = findViewById(R.id.tv_title);
        openChapter(apiRef, currentEpubUrl, target.id, webViewRef, titleView);
    }

    private Integer findNavigableIndex(int startIndex, int direction) {
        if (direction == 0 || chapterItems.isEmpty()) return null;
        int i = startIndex + direction;
        while (i >= 0 && i < chapterItems.size()) {
            com.example.myreadbookapplication.model.epub.EpubModels.ChapterItem candidate = chapterItems.get(i);
            if (isNavigableChapter(candidate)) {
                return i;
            }
            i += direction;
        }
        return null;
    }

    private boolean isNavigableChapter(com.example.myreadbookapplication.model.epub.EpubModels.ChapterItem chapter) {
        if (!shouldDisplayChapter(chapter)) return false;
        String idLower = chapter != null && chapter.id != null ? chapter.id.toLowerCase() : "";
        String hrefLower = chapter != null && chapter.href != null ? chapter.href.toLowerCase() : "";
        if (idLower.contains("cover") || hrefLower.contains("wrap0000")) return false;
        if (idLower.startsWith("toc") || hrefLower.contains("toc")) return false;
        if (idLower.contains("pg-header") || idLower.contains("pg-footer")) return false;
        return true;
    }

    private boolean shouldDisplayChapter(com.example.myreadbookapplication.model.epub.EpubModels.ChapterItem chapter) {
        if (chapter == null) return false;
        String normalizedId = normalizeChapterKey(chapter.id);
        String normalizedHref = normalizeChapterKey(chapter.href);
        String title = chapter.title != null ? chapter.title.trim() : "";

        if (TextUtils.isEmpty(normalizedId) && TextUtils.isEmpty(normalizedHref) && TextUtils.isEmpty(title)) {
            return false;
        }

        String lowerTitle = title.toLowerCase(Locale.getDefault());
        if (lowerTitle.startsWith("cover") || lowerTitle.contains("project gutenberg")) {
            return false;
        }
        if (lowerTitle.startsWith("table of contents") || lowerTitle.equals("toc")) {
            return false;
        }

        String hrefLower = chapter.href != null ? chapter.href.toLowerCase() : "";
        if (hrefLower.contains("pg-header") || hrefLower.contains("pg-footer")) return false;
        if (hrefLower.contains("coverpage") || hrefLower.contains("/images/") || hrefLower.endsWith(".css")) return false;
        if (hrefLower.contains("wrap0000")) return false;

        return true;
    }

    private String getChapterUniqueKey(com.example.myreadbookapplication.model.epub.EpubModels.ChapterItem chapter) {
        if (chapter == null) return null;
        String key = normalizeChapterKey(chapter.id);
        if (!TextUtils.isEmpty(key)) return key;
        key = normalizeChapterKey(chapter.href);
        if (!TextUtils.isEmpty(key)) return key;
        if (!TextUtils.isEmpty(chapter.title)) {
            return chapter.title.trim().toLowerCase(Locale.getDefault());
        }
        return null;
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

    private void updateChapterNavigationButtons() {
        if (btnPrevChapter == null || btnNextChapter == null) return;

        boolean hasPrev = false;
        boolean hasNext = false;

        Integer index = getChapterIndex(currentChapterId);
        if (index != null) {
            hasPrev = findNavigableIndex(index, -1) != null;
            hasNext = findNavigableIndex(index, 1) != null;
        } else if (!chapterItems.isEmpty()) {
            hasPrev = false;
            hasNext = findNavigableIndex(-1, 1) != null;
        }

        applyButtonState(btnPrevChapter, hasPrev);
        applyButtonState(btnNextChapter, hasNext);
    }

    private void applyButtonState(MaterialButton button, boolean enabled) {
        if (button == null) return;
        button.setEnabled(enabled);
        button.setAlpha(enabled ? 1f : 0.5f);
    }

    private void showChapterSheet() {
        if (chapterItems.isEmpty()) {
            Toast.makeText(this, getString(R.string.chapter_sheet_empty), Toast.LENGTH_SHORT).show();
            return;
        }
        ensureChapterSheet();
        updateChapterSheet();
        if (chapterSheetDialog != null && !chapterSheetDialog.isShowing()) {
            chapterSheetDialog.show();
        }
    }

    private void ensureChapterSheet() {
        if (chapterSheetDialog != null) return;
        chapterSheetDialog = new BottomSheetDialog(this);
        View sheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_chapter_list, null);
        chapterSheetDialog.setContentView(sheetView);

        chapterRecycler = sheetView.findViewById(R.id.recycler_chapters);
        sheetTitleView = sheetView.findViewById(R.id.tv_sheet_title);
        sheetCountView = sheetView.findViewById(R.id.tv_chapter_count);
        sheetEmptyView = sheetView.findViewById(R.id.tv_empty_chapters);
        View closeBtn = sheetView.findViewById(R.id.btn_close_sheet);
        if (closeBtn != null) {
            closeBtn.setOnClickListener(v -> chapterSheetDialog.dismiss());
        }

        chapterListAdapter = new ChapterListAdapter(this, (chapter, position) -> {
            if (chapter == null) return;
            String jumpId = !TextUtils.isEmpty(chapter.id) ? chapter.id : chapter.href;
            openChapterFromList(jumpId);
        });
        if (chapterRecycler != null) {
            chapterRecycler.setLayoutManager(new LinearLayoutManager(this));
            chapterRecycler.setAdapter(chapterListAdapter);
        }
    }

    private void updateChapterSheet() {
        ensureChapterSheet();
        if (chapterListAdapter != null) {
            chapterListAdapter.submitList(new ArrayList<>(chapterItems));
            chapterListAdapter.setCurrentChapterKey(currentChapterId);
        }
        if (sheetTitleView != null) {
            sheetTitleView.setText(tvBookTitle != null ? tvBookTitle.getText() : getString(R.string.chapter_sheet_title));
        }
        if (sheetCountView != null) {
            sheetCountView.setText(getString(R.string.chapter_sheet_count, chapterItems.size()));
        }
        if (sheetEmptyView != null) {
            sheetEmptyView.setVisibility(chapterItems.isEmpty() ? View.VISIBLE : View.GONE);
        }
        if (chapterRecycler != null && chapterListAdapter != null) {
            int currentPos = chapterListAdapter.getCurrentPosition();
            if (currentPos >= 0) {
                chapterRecycler.post(() -> chapterRecycler.smoothScrollToPosition(currentPos));
            }
        }
    }

    private void openChapterFromList(String chapterKey) {
        if (TextUtils.isEmpty(chapterKey)) {
            Toast.makeText(this, getString(R.string.chapter_current_unknown), Toast.LENGTH_SHORT).show();
            return;
        }
        pendingChapterId = chapterKey;
        if (apiRef != null && currentEpubUrl != null && webViewRef != null) {
            openChapter(apiRef, currentEpubUrl, chapterKey, webViewRef, (TextView) findViewById(R.id.tv_title));
            pendingChapterId = null;
        }
        if (chapterSheetDialog != null && chapterSheetDialog.isShowing()) {
            chapterSheetDialog.dismiss();
        }
    }

    private void updateCurrentChapterLabel(String source) {
        if (tvCurrentChapter == null) return;
        String display = resolveChapterTitle(source);
        if (TextUtils.isEmpty(display) && !TextUtils.isEmpty(currentChapterId)) {
            display = resolveChapterTitle(currentChapterId);
        }
        if (TextUtils.isEmpty(display)) {
            display = getString(R.string.chapter_current_unknown);
        }
        tvCurrentChapter.setText(getString(R.string.chapter_current_format, display));
    }

    private String resolveChapterTitle(String rawKeyOrTitle) {
        if (TextUtils.isEmpty(rawKeyOrTitle)) return null;
        String hint = fetchStoredTitleHint(rawKeyOrTitle);
        if (!TextUtils.isEmpty(hint)) return hint;
        return sanitizeTitle(rawKeyOrTitle);
    }

    private void indexChapter(String key, int index) {
        if (index < 0) return;
        String normalized = normalizeChapterKey(key);
        if (normalized == null || normalized.isEmpty()) return;
        chapterIndexMap.put(normalized, index);

        String fileName = extractFileName(normalized);
        if (fileName != null && !fileName.isEmpty()) {
            chapterIndexMap.putIfAbsent(fileName, index);
            String baseName = stripExtension(fileName);
            if (baseName != null && !baseName.isEmpty()) {
                chapterIndexMap.putIfAbsent(baseName, index);
            }
        }
    }

    private Integer getChapterIndex(String key) {
        if (TextUtils.isEmpty(key)) return null;
        String normalized = normalizeChapterKey(key);
        if (normalized == null) return null;

        Integer index = chapterIndexMap.get(normalized);
        if (index != null) return index;

        String fileName = extractFileName(normalized);
        if (fileName != null && !fileName.isEmpty()) {
            index = chapterIndexMap.get(fileName);
            if (index != null) return index;
            String baseName = stripExtension(fileName);
            if (baseName != null && !baseName.isEmpty()) {
                index = chapterIndexMap.get(baseName);
                if (index != null) return index;
            }
        }
        return null;
    }

    private boolean matchesChapterKey(com.example.myreadbookapplication.model.epub.EpubModels.ChapterItem item, String key) {
        if (item == null || TextUtils.isEmpty(key)) return false;
        String normalizedKey = normalizeChapterKey(key);
        if (normalizedKey == null) return false;
        if (normalizedKey.equals(normalizeChapterKey(item.id))) return true;
        if (normalizedKey.equals(normalizeChapterKey(item.href))) return true;
        String fileName = extractFileName(normalizedKey);
        if (fileName != null && fileName.equals(extractFileName(normalizeChapterKey(item.href)))) return true;
        String baseKey = stripExtension(fileName);
        if (baseKey != null) {
            String itemFile = extractFileName(normalizeChapterKey(item.href));
            if (baseKey.equals(stripExtension(itemFile))) return true;
            if (baseKey.equals(stripExtension(normalizeChapterKey(item.id)))) return true;
        }
        return false;
    }

    private String normalizeChapterKey(String key) {
        if (key == null) return null;
        String normalized = key.trim().toLowerCase().replace("\\", "/");
        int hashIndex = normalized.indexOf('#');
        if (hashIndex >= 0) {
            normalized = normalized.substring(0, hashIndex);
        }
        if (normalized.startsWith("./")) {
            normalized = normalized.substring(2);
        }
        if (normalized.startsWith("oebps/")) {
            normalized = normalized.substring("oebps/".length());
        }
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }

    private String extractFileName(String path) {
        if (path == null) return null;
        int slashIndex = path.lastIndexOf('/');
        if (slashIndex >= 0 && slashIndex < path.length() - 1) {
            return path.substring(slashIndex + 1);
        }
        return path;
    }

    private String stripExtension(String fileName) {
        if (fileName == null) return null;
        int dot = fileName.lastIndexOf('.');
        if (dot > 0) {
            return fileName.substring(0, dot);
        }
        return fileName;
    }

    private boolean handleWebLink(String url, WebView view, TextView tvTitle) {
        try {
            if (url == null) return true;
            // Ignore favicon
            if (url.endsWith("/favicon.ico")) return true;

            // Normalize path relative to backend base
            String path = url;
            try {
                URI uri = new URI(url);
                path = uri.getPath();
            } catch (URISyntaxException ignored) {}

            if (path == null) path = url;

            // Pattern 1: /links/{anchorOrId}/OEBPS/...  → prefer the OEBPS href if present
            if (path.startsWith("/links/")) {
                String remainder = path.substring("/links/".length());
                // If the remainder contains an OEBPS path, use it as href
                int oebpsIdx = remainder.indexOf("OEBPS/");
                if (oebpsIdx >= 0) {
                    String href = remainder.substring(oebpsIdx);
                    String mappedId = hrefToId.get(href);
                    String target = mappedId != null ? mappedId : href; // fall back to href directly
                    if (apiRef != null && currentEpubUrl != null) {
                        openChapter(apiRef, currentEpubUrl, target, view, tvTitle);
                        return true;
                    }
                } else {
                    // No OEBPS path; fall back to first segment as an id
                    int slash = remainder.indexOf('/');
                    String targetId = slash >= 0 ? remainder.substring(0, slash) : remainder;
                    if (apiRef != null && currentEpubUrl != null && targetId != null && !targetId.isEmpty()) {
                        openChapter(apiRef, currentEpubUrl, targetId, view, tvTitle);
                        return true;
                    }
                }
            }

            // Pattern 2: direct OEBPS html file links → look up by href
            if (path.contains("OEBPS/")) {
                // Extract trailing OEBPS/... file path
                int idx = path.indexOf("OEBPS/");
                String href = path.substring(idx);
                String targetId = hrefToId.get(href);
                if (targetId != null && apiRef != null && currentEpubUrl != null) {
                    openChapter(apiRef, currentEpubUrl, targetId, view, tvTitle);
                    return true;
                }
            }

            // Otherwise, let WebView handle normally within the same view
            view.loadUrl(url);
            return true;
        } catch (Exception e) {
            // Fallback to default navigation
            view.loadUrl(url);
            return true;
        }
    }

    private void fallbackDirectLoad(WebView webView, String bookUrl, String txtUrl, String epubUrl) {
        String loadUrl = null;
        if (bookUrl != null && !bookUrl.isEmpty()) {
            loadUrl = bookUrl;
        } else if (txtUrl != null && !txtUrl.isEmpty()) {
            loadUrl = txtUrl;
        } else if (epubUrl != null && !epubUrl.isEmpty()) {
            loadUrl = epubUrl;
        }
        if (loadUrl != null) {
            webView.setVisibility(View.VISIBLE);
            webView.loadUrl(loadUrl);
            updateCurrentChapterLabel(getString(R.string.chapter_current_full_book));
        } else {
            webView.setVisibility(View.GONE);
            updateCurrentChapterLabel(null);
        }
    }

    private void handleBack() {
        if (webViewRef != null && webViewRef.canGoBack()) {
            webViewRef.goBack();
        } else {
            saveBookmarkAndFinish();
        }
    }

    @Override
    public void onBackPressed() {
        handleBack();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop auto-save when pausing
        stopAutoSaveScrollPosition();
        // Persist bookmark also on pause to be robust
        if (isFinishing()) return;
        saveBookmarkAndFinish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cleanup handlers
        stopAutoSaveScrollPosition();
        if (scrollSaveHandler != null) {
            scrollSaveHandler.removeCallbacksAndMessages(null);
        }
    }

    private void resumeFromBookmarkIfAny() {
        try {
            if (currentBookId == null || currentBookId.isEmpty()) return;
            
            AuthManager authManager = AuthManager.getInstance(this);
            String userId = authManager.getUserId();
            String token = authManager.getAccessToken();
            
            if (userId == null || token == null || token.isEmpty()) return;
            if (apiRef == null) apiRef = RetrofitClient.getApiService();

            apiRef.getBookmark(userId, currentBookId, "Bearer " + token).enqueue(new Callback<ApiResponse<com.example.myreadbookapplication.model.HistoryItem>>() {
                @Override
                public void onResponse(Call<ApiResponse<com.example.myreadbookapplication.model.HistoryItem>> call, Response<ApiResponse<com.example.myreadbookapplication.model.HistoryItem>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                        com.example.myreadbookapplication.model.HistoryItem item = response.body().getData();
                        currentPage = item.getPage();
                        currentChapterId = item.getChapterId();
                        
                        pendingChapterId = currentChapterId;
                        if (!TextUtils.isEmpty(pendingChapterId) && !chapterItems.isEmpty() && getChapterIndex(pendingChapterId) != null
                                && apiRef != null && currentEpubUrl != null && webViewRef != null) {
                            openChapter(apiRef, currentEpubUrl, pendingChapterId, webViewRef, (TextView) findViewById(R.id.tv_title));
                            pendingChapterId = null;
                        }
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<com.example.myreadbookapplication.model.HistoryItem>> call, Throwable t) {
                }
            });
        } catch (Exception ignored) {}
    }

    private void saveBookmarkAndFinish() {
        try {
            if (currentBookId == null || currentBookId.isEmpty()) { finish(); return; }
            
            AuthManager authManager = AuthManager.getInstance(this);
            String userId = authManager.getUserId();
            String token = authManager.getAccessToken();
            
            if (userId == null || token == null || token.isEmpty()) { finish(); return; }
            if (apiRef == null) apiRef = RetrofitClient.getApiService();

            // Save current scroll position before saving bookmark
            saveCurrentScrollPosition();

            String chapterIdToSave = currentChapterId;
            if (chapterIdToSave == null || chapterIdToSave.isEmpty()) {
                chapterIdToSave = "chapter1"; // Default chapter
            }
            
            // Log để debug
            android.util.Log.d("ReadBookActivity", "Saving bookmark - userId: " + userId + ", bookId: " + currentBookId + ", chapterId: " + chapterIdToSave);
            
            apiRef.saveBookmark(userId, currentBookId, chapterIdToSave, "Bearer " + token)
                    .enqueue(new Callback<ApiResponse>() {
                        @Override
                        public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                            finish();
                        }

                        @Override
                        public void onFailure(Call<ApiResponse> call, Throwable t) {
                            finish();
                        }
                    });
        } catch (Exception e) {
            finish();
        }
    }

    private void saveCurrentScrollPosition() {
        try {
            if (webViewRef != null && currentBookId != null && currentChapterId != null) {
                webViewRef.evaluateJavascript("window.scrollY", value -> {
                    try {
                        int scrollY = (int) Double.parseDouble(value.replace("\"", ""));
                        android.content.SharedPreferences prefs = getSharedPreferences("reading_progress", MODE_PRIVATE);
                        String key = currentBookId + "_" + currentChapterId;
                        prefs.edit().putInt(key, scrollY).apply();
                    } catch (Exception ignored) {}
                });
            }
        } catch (Exception ignored) {}
    }

    private void loadSavedScrollPosition() {
        try {
            if (currentBookId != null && currentChapterId != null) {
                android.content.SharedPreferences prefs = getSharedPreferences("reading_progress", MODE_PRIVATE);
                String key = currentBookId + "_" + currentChapterId;
                currentScrollPosition = prefs.getInt(key, 0);
            }
        } catch (Exception ignored) {}
    }

    private void restoreScrollPosition() {
        try {
            if (webViewRef != null && currentScrollPosition > 0) {
                webViewRef.post(() -> {
                    webViewRef.scrollTo(0, currentScrollPosition);
                });
            }
        } catch (Exception ignored) {}
    }

    private void startAutoSaveScrollPosition() {
        // Stop previous auto-save if running
        stopAutoSaveScrollPosition();
        
        scrollSaveRunnable = new Runnable() {
            @Override
            public void run() {
                saveCurrentScrollPosition();
                // Schedule next save in 2 seconds
                scrollSaveHandler.postDelayed(this, 2000);
            }
        };
        scrollSaveHandler.postDelayed(scrollSaveRunnable, 2000);
    }

    private void stopAutoSaveScrollPosition() {
        if (scrollSaveRunnable != null) {
            scrollSaveHandler.removeCallbacks(scrollSaveRunnable);
            scrollSaveRunnable = null;
        }
    }
    
    // Menu dropdown methods
    private void loadSavedStates() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        isNightMode = prefs.getBoolean("night_mode_" + currentBookId, false);
        isFavorite = prefs.getBoolean("favorite_" + currentBookId, false);
        
        // Apply night mode if enabled
        if (isNightMode) {
            applyNightMode();
        }
    }
    
    private void setupMenuDropdown(ImageView menuButton) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View menuView = inflater.inflate(R.layout.menu_dropdown, null);
        
        menuPopup = new PopupWindow(menuView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        menuPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        menuPopup.setElevation(8);
        
        // Setup menu items
        LinearLayout menuAddFavorite = menuView.findViewById(R.id.menu_add_favorite);
        LinearLayout menuNightMode = menuView.findViewById(R.id.menu_night_mode);
        
        menuAddFavorite.setOnClickListener(v -> {
            toggleFavorite();
            menuPopup.dismiss();
        });
        
        menuNightMode.setOnClickListener(v -> {
            toggleNightMode();
            menuPopup.dismiss();
        });
    }
    
    private void showMenuDropdown(ImageView menuButton) {
        if (menuPopup != null) {
            // Calculate position to show menu aligned to the right edge of the button
            int[] location = new int[2];
            menuButton.getLocationOnScreen(location);
            int xOffset = menuButton.getWidth() - menuPopup.getWidth();
            menuPopup.showAsDropDown(menuButton, xOffset, 0);
        }
    }
    
    private void toggleFavorite() {
        isFavorite = !isFavorite;
        
        // Save to SharedPreferences
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        prefs.edit().putBoolean("favorite_" + currentBookId, isFavorite).apply();
        
        // Show feedback
        String message = isFavorite ? "Added to favorites" : "Removed from favorites";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        
        // Sync with backend if user is logged in
        syncFavoriteWithBackend();
    }
    
    private void toggleNightMode() {
        isNightMode = !isNightMode;
        
        // Save to SharedPreferences
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        prefs.edit().putBoolean("night_mode_" + currentBookId, isNightMode).apply();
        
        // Apply night mode
        if (isNightMode) {
            applyNightMode();
            Toast.makeText(this, "Night mode enabled", Toast.LENGTH_SHORT).show();
        } else {
            applyDayMode();
            Toast.makeText(this, "Day mode enabled", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void applyNightMode() {
        // Change background to dark
        findViewById(R.id.header_layout).setBackgroundColor(Color.parseColor("#1E1E1E"));
        findViewById(R.id.divider_line).setBackgroundColor(Color.parseColor("#333333"));
        
        // Change text color to light
        TextView tvTitle = findViewById(R.id.tv_title);
        tvTitle.setTextColor(Color.WHITE);
        
        // Apply dark theme to WebView
        if (webViewRef != null) {
            webViewRef.setBackgroundColor(Color.parseColor("#1E1E1E"));
            // Inject CSS for dark mode
            String darkModeCSS = "javascript:(function(){" +
                "var style = document.createElement('style');" +
                "style.innerHTML = 'body { background-color: #1E1E1E !important; color: #FFFFFF !important; }';" +
                "document.head.appendChild(style);" +
                "})()";
            webViewRef.evaluateJavascript(darkModeCSS, null);
        }
    }
    
    private void applyDayMode() {
        // Change background to light
        findViewById(R.id.header_layout).setBackgroundColor(Color.WHITE);
        findViewById(R.id.divider_line).setBackgroundColor(Color.BLACK);
        
        // Change text color to dark
        TextView tvTitle = findViewById(R.id.tv_title);
        tvTitle.setTextColor(Color.BLACK);
        
        // Apply light theme to WebView
        if (webViewRef != null) {
            webViewRef.setBackgroundColor(Color.WHITE);
            // Inject CSS for light mode
            String lightModeCSS = "javascript:(function(){" +
                "var style = document.createElement('style');" +
                "style.innerHTML = 'body { background-color: #FFFFFF !important; color: #000000 !important; }';" +
                "document.head.appendChild(style);" +
                "})()";
            webViewRef.evaluateJavascript(lightModeCSS, null);
        }
    }
    
    private void syncFavoriteWithBackend() {
        try {
            AuthManager authManager = AuthManager.getInstance(this);
            String userId = authManager.getUserId();
            String token = authManager.getAccessToken();
            
            if (userId != null && token != null && !token.isEmpty()) {
                ApiService api = RetrofitClient.getApiService();
                if (isFavorite) {
                    api.addFavorite(userId, currentBookId, "Bearer " + token).enqueue(new Callback<ApiResponse>() {
                        @Override
                        public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                            // Silent success
                        }
                        @Override
                        public void onFailure(Call<ApiResponse> call, Throwable t) {
                            // Silent failure
                        }
                    });
                } else {
                    api.removeFavorite(userId, currentBookId, "Bearer " + token).enqueue(new Callback<ApiResponse>() {
                        @Override
                        public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                            // Silent success
                        }
                        @Override
                        public void onFailure(Call<ApiResponse> call, Throwable t) {
                            // Silent failure
                        }
                    });
                }
            }
        } catch (Exception ignored) {}
    }

    /**
     * Setup title scrolling for long titles
     * @param titleView TextView to setup scrolling
     * @param title Title text
     */
    private void setupTitleScrolling(TextView titleView, String title) {
        if (title == null || title.isEmpty()) return;
        
        // Set the title
        titleView.setText(title);
        
        // Enable marquee scrolling for long titles
        titleView.post(() -> {
            // Check if text is longer than available space
            if (titleView.getLayout() != null && titleView.getText().length() > 0) {
                // Enable marquee scrolling
                titleView.setSelected(true);
            }
        });
    }

    /**
     * Setup book information display
     */
    private void setupBookInfo(String title, String author, String coverUrl) {
        // Set book title (card) if present, otherwise keep header title only
        if (tvBookTitle != null) {
            tvBookTitle.setText(title != null ? title : getString(R.string.chapter_current_unknown));
        }

        // Set author
        if (tvAuthor != null) {
            String authorDisplay = (author != null && !author.isEmpty())
                    ? author
                    : getString(R.string.author_unknown);
            tvAuthor.setText(getString(R.string.author_label, authorDisplay));
        }
        
        // Load cover image
        ImageView ivCover = findViewById(R.id.iv_cover);
        if (coverUrl != null && !coverUrl.isEmpty()) {
            Glide.with(this)
                .load(coverUrl)
                .placeholder(R.drawable.default_book_cover)
                .error(R.drawable.default_book_cover)
                .into(ivCover);
        }
    }

    /**
     * Update reading progress
     */
    /**
     * Setup font size controls
     */
    private void setupFontControls() {
        btnFontDecrease.setOnClickListener(v -> {
            if (currentFontSize > 20) {
                currentFontSize -= 5;
                updateWebViewFontSize();
            }
        });
        
        btnFontIncrease.setOnClickListener(v -> {
            if (currentFontSize < 50) {
                currentFontSize += 5;
                updateWebViewFontSize();
            }
        });
    }

    /**
     * Update WebView font size
     */
    private void updateWebViewFontSize() {
        if (webViewRef != null) {
            webViewRef.post(() -> {
                webViewRef.evaluateJavascript(
                    "document.body.style.fontSize='" + currentFontSize + "px'", null);
            });
        }
    }



}


