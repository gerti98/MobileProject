package com.example.chatapp.util;

import java.io.IOException;

public interface UICallback {
    void onFailure(String response);
    void onSuccess(String response) throws IOException;
}
