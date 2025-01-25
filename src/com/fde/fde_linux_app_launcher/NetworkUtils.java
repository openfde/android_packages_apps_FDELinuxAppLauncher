package com.fde.fde_linux_app_launcher;

import android.util.Log;
import okhttp3.*;
import java.io.IOException;

public class NetworkUtils {
      private static final String TAG = "NetworkUtils";
    private final OkHttpClient client = new OkHttpClient();

    public void postRequest(String url, FormBody requestBody) {
        // Build the request
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        // Execute the request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Handle the error
                e.printStackTrace();
                Log.e(TAG, "Request failed", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Handle the response
                    String responseData = response.body().string();
                    Log.i(TAG, "Response: " + responseData);
                } else {
                    // Handle the error
                    Log.e(TAG, "Request failed: " + response.message());
                }
            }
        });
    }
}
