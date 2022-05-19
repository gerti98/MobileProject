package com.example.chatapp.util;

import java.io.IOException;
import java.util.List;

public interface UICallback {
    void onFailure(String response);
    void onSuccess(List<String> response) throws IOException;
}
