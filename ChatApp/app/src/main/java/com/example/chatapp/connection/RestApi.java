package com.example.chatapp.connection;

import com.example.chatapp.util.CustomAsyncTask;
import com.example.chatapp.util.UICallback;

import java.util.List;

import okhttp3.Request;

public class RestApi {

    String responseString = null;
    UICallback callback = null;

    public void makeRequests(List<Request> requestList){
        CustomAsyncTask asyncTask = new CustomAsyncTask(requestList);
        asyncTask.setResponseCallbacks(callback);
        asyncTask.execute();
    }

    public void setUICallback(UICallback callback){
        this.callback = callback;
    }

    public String getResponseString() {
        return responseString;
    }
}