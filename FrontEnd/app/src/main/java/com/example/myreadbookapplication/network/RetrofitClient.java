package com.example.myreadbookapplication.network;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.myreadbookapplication.BuildConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofit; //singleton instance
    private static Context applicationContext;

    public static void init(Context context) {
        applicationContext = context.getApplicationContext();
    }

    public static ApiService getApiService() {
        if (retrofit == null) {
            // Tạo OkHttpClient với logging và timeout
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            
            // Cấu hình timeout để tránh request quá lâu
            httpClient.connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS);
            httpClient.readTimeout(30, java.util.concurrent.TimeUnit.SECONDS);
            httpClient.writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS);

            // Thêm logging interceptor cho debug (chỉ log headers, không log body để tránh log sensitive data)
            if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                logging.setLevel(HttpLoggingInterceptor.Level.HEADERS); // Chỉ log headers, không log body
                httpClient.addInterceptor(logging);
            }

            // Interceptor thêm Authorization tự động nếu có token
            httpClient.addInterceptor(chain -> {
                okhttp3.Request original = chain.request();
                
                // Lấy token từ SharedPreferences
                String token = null;
                if (applicationContext != null) {
                    SharedPreferences prefs = applicationContext.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
                    token = prefs.getString("access_token", null);
                }
                
                // Nếu có token và request chưa có Authorization header
                if (token != null && !token.isEmpty() && original.header("Authorization") == null) {
                    okhttp3.Request.Builder requestBuilder = original.newBuilder()
                            .header("Authorization", "Bearer " + token);
                    return chain.proceed(requestBuilder.build());
                }
                
                return chain.proceed(original);
            });

            // Tạo Gson với cấu hình
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.BASE_URL)
                    .client(httpClient.build())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}
