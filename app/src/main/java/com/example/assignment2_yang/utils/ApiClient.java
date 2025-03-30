package com.example.assignment2_yang.utils;


import android.util.Log;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class ApiClient {
    // Singleton OkHttpClient instance for making HTTP requests
    private static final OkHttpClient client = new OkHttpClient();

    // JSON media type for request body
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");


    /**
     * Sends a GET request to the specified URL.
     * @param url The endpoint URL
     * @param callback Callback to handle the response asynchronously
     */

    public static void get(String url, Callback callback) {
        Log.d("ApiClient", "Request URL: " + url);  // Log the request URL for debugging //
        Request request = new Request.Builder().url(url).build();

        // Execute the request asynchronously
        client.newCall(request).enqueue(callback);
    }
}
