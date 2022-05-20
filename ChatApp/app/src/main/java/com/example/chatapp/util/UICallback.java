package com.example.chatapp.util;

import android.widget.ImageView;

import java.io.IOException;
import java.util.List;

public interface UICallback {
    void onFailure(String response);
    void onSuccess(List<String> response, int type) throws IOException;
}
