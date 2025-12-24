package com.example.myreadbookapplication.fragment.admin;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myreadbookapplication.R;
import com.example.myreadbookapplication.adapter.AdminFeedbackAdapter;
import com.example.myreadbookapplication.model.ApiResponse;
import com.example.myreadbookapplication.model.Feedback;
import com.example.myreadbookapplication.network.ApiService;
import com.example.myreadbookapplication.network.RetrofitClient;
import com.example.myreadbookapplication.utils.AuthManager;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeedbackFragment extends Fragment {

    private static final String TAG = "FeedbackFragment";
    
    private RecyclerView rvFeedback;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    
    private AdminFeedbackAdapter adapter;
    private ApiService apiService;
    private AuthManager authManager;
    private List<Feedback> feedbackList = new ArrayList<>();
    
    private boolean isDataLoaded = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiService = RetrofitClient.getApiService();
        authManager = AuthManager.getInstance(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feedback, container, false);
        
        initViews(view);
        setupRecyclerView();
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!isDataLoaded) {
            loadFeedbacks();
        }
    }

    private void initViews(View view) {
        rvFeedback = view.findViewById(R.id.rv_feedback);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        progressBar = view.findViewById(R.id.progress_bar);
    }

    private void setupRecyclerView() {
        adapter = new AdminFeedbackAdapter();
        adapter.setOnFeedbackClickListener(feedback -> {
            Toast.makeText(requireContext(), "Feedback from: " + feedback.getEmail(), Toast.LENGTH_SHORT).show();
        });

        rvFeedback.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvFeedback.setAdapter(adapter);
    }

    /**
     * Public method để reload data từ Activity
     */
    public void reloadData() {
        Log.d(TAG, "Reloading feedbacks data...");
        loadFeedbacks();
    }

    private void loadFeedbacks() {
        String accessToken = authManager.getAccessToken();
        
        if (accessToken == null || accessToken.isEmpty()) {
            Log.e(TAG, "No access token");
            Toast.makeText(requireContext(), "Please login as admin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (layoutEmpty != null) {
            layoutEmpty.setVisibility(View.GONE);
        }

        String authHeader = "Bearer " + accessToken;
        int page = 1;
        int limit = 100;
        
        Call<ApiResponse> call = apiService.getAllFeedbacks(authHeader, page, limit);
        
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    
                    if (apiResponse.isSuccess()) {
                        Object dataObj = apiResponse.getData();
                        
                        if (dataObj != null) {
                            try {
                                Gson gson = new Gson();
                                String json = gson.toJson(dataObj);
                                JsonElement jsonElement = JsonParser.parseString(json);
                                
                                if (jsonElement.isJsonArray()) {
                                    JsonArray jsonArray = jsonElement.getAsJsonArray();
                                    TypeToken<List<Feedback>> token = new TypeToken<List<Feedback>>() {};
                                    List<Feedback> feedbacksList = gson.fromJson(jsonArray, token.getType());

                                    if (feedbacksList != null && !feedbacksList.isEmpty()) {
                                        feedbackList = feedbacksList;
                                        adapter.setFeedbackList(feedbackList);
                                        hideEmptyState();
                                        isDataLoaded = true;
                                        Log.d(TAG, "Loaded " + feedbackList.size() + " feedbacks");
                                    } else {
                                        showEmptyState();
                                    }
                                } else {
                                    showEmptyState();
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing feedbacks: " + e.getMessage(), e);
                                showEmptyState();
                            }
                        } else {
                            showEmptyState();
                        }
                    } else {
                        showEmptyState();
                    }
                } else {
                    Log.e(TAG, "Load feedbacks failed - HTTP " + response.code());
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                Log.e(TAG, "Load feedbacks network error: " + t.getMessage(), t);
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }

    private void showEmptyState() {
        if (layoutEmpty != null) {
            layoutEmpty.setVisibility(View.VISIBLE);
        }
        if (rvFeedback != null) {
            rvFeedback.setVisibility(View.GONE);
        }
    }

    private void hideEmptyState() {
        if (layoutEmpty != null) {
            layoutEmpty.setVisibility(View.GONE);
        }
        if (rvFeedback != null) {
            rvFeedback.setVisibility(View.VISIBLE);
        }
    }
}

