package com.example.chatapp;

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