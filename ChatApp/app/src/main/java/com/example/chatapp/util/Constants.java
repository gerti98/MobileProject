package com.example.chatapp.util;

import okhttp3.MediaType;

public class Constants {
    // Number of messages for which a rest api call will be triggered to classify an emotion
    public final static int REST_API_MESSAGE_SIZE = 5;

    public final static String URL_MESSAGES_REST_API = "https://mobile-group3.herokuapp.com/predict_emotion";
    public static final MediaType JSON_MEDIATYPE = MediaType.get("application/json; charset=utf-8");

}
