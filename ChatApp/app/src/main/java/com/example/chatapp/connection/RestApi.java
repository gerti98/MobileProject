package com.example.chatapp.connection;

import com.example.chatapp.util.CustomAsyncTask;
import com.example.chatapp.util.UICallback;

import okhttp3.Request;

public class RestApi {

    String responseString = null;
    UICallback callback = null;

    public void makeRequest(Request request) {
        CustomAsyncTask asyncTask = new CustomAsyncTask();
        asyncTask.setResponseCallbacks(callback);
        asyncTask.execute(request);
    }

    public void setUICallback(UICallback callback){
        this.callback = callback;
    }

    public String getResponseString() {
        return responseString;
    }
}