package com.example.chatapp.util;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CustomAsyncTask extends AsyncTask {
    private static final String TAG = "ChatActivity";
    private UICallback responseCallbacks;

    public CustomAsyncTask() {}

    public void setResponseCallbacks(UICallback callbacks) {
        this.responseCallbacks = callbacks;
    }

    @Override
    protected String doInBackground(Object... params) {
        Request request = (Request) params[0];
        OkHttpClient client = new OkHttpClient();
        String result = null;
        try {
            Response response = client.newCall(request).execute();
            result = response.body().string();
            Log.i(TAG, "[HTTP request result #1]" + result);
//            response = client.newCall(request).execute();
//            result = response.body().string();
//            Log.i(TAG, "[HTTP request result #"+counter+"]" + result);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    protected void onPostExecute(Object result) {
        Log.i(TAG, "[HTTP request onPostExecute]" + result);
        try {
            if(result == null)
                responseCallbacks.onFailure(null);
            else
                responseCallbacks.onSuccess((String) result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
