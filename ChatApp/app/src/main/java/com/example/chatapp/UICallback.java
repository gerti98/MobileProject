package com.example.chatapp;

import java.io.IOException;

public interface UICallback {
    void onFailure(String response);
    void onSuccess(String response) throws IOException;
}
