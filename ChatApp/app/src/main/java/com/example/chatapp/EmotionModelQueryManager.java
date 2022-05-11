package com.example.chatapp;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.util.IOUtils;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EmotionModelQueryManager extends AsyncTask<String, String, String> {
    private static final String TAG = "EmotionModelQueryManage";
    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    public String url = "https://mobile-group3.herokuapp.com/predict_emotion";

    public void setCallback(){

    }

    @Override
    protected String doInBackground(String... strings) {
        Log.i(TAG, "Start");
        String json = "{ \"msgs\" : [\"I love you\", \"I love you\", \"I love you\"]}";
        Log.i(TAG, "Preparing Request");
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Log.i(TAG, "Request Prepared");
        try (Response response = client.newCall(request).execute()) {
            String result = response.body().string();
            Log.i(TAG, "[HTTP request result]" + result);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Neutral";
    }
}
