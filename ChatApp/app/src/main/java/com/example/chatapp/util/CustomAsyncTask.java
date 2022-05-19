package com.example.chatapp.util;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CustomAsyncTask extends AsyncTask {
    private static final String TAG = "ChatActivity";
    private UICallback responseCallbacks;
    private List<Request> requestList;
    private List<String> results;

    public CustomAsyncTask(List<Request> requestList) {
        this.requestList = requestList;
        this.results = new ArrayList<>();
    }

    public void setResponseCallbacks(UICallback callbacks) {
        this.responseCallbacks = callbacks;
    }

    @Override
    protected List<String> doInBackground(Object... params) {
        OkHttpClient client = new OkHttpClient();
        String result = null;
        int counter = 0;

        try {
            for(Request request: requestList){
                Response response = client.newCall(request).execute();
                result = response.body().string();
                Log.i(TAG, "[HTTP request result #" +counter + "]: " + result);
                results.add(result);
                counter++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return results;
    }

    protected void onPostExecute(Object results) {
        List<String> results_string = (List<String>) results;

        Log.i(TAG, "[HTTP request onPostExecute]");
        try {
            if(results_string.isEmpty())
                responseCallbacks.onFailure(null);
            else
                responseCallbacks.onSuccess(results_string);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
