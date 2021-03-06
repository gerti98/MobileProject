package com.example.chatapp.util;

import okhttp3.MediaType;

public class Constants {
    // Number of messages for which a rest api call will be triggered to classify an emotion
    public final static int REST_API_MESSAGE_SIZE = 5;
    public final static int LABELLING_API_MESSAGE_SIZE = 10;
    public final static boolean LABELLING_REQUIRED = true;

    public final static String URL_TEXT_MESSAGES_REST_API = "https://mobile-group3.herokuapp.com/predict_text_emotion";
    public final static String URL_VOICE_MESSAGES_REST_API = "https://mobile-group3.herokuapp.com/predict_voice_emotion";

    public static final MediaType JSON_MEDIATYPE = MediaType.get("application/json; charset=utf-8");

    public final static int MAX_FAVORITES_LIST_SIZE = 50;

    public final static int MSG_TO_SHOW_INCREMENT = 5;
    public final static int DEFAULT_MSG_SHOWN = 15;

    public final static String dbName = "https://chatapp-8aa46-default-rtdb.europe-west1.firebasedatabase.app/";
    public final static String storageName = "gs://chatapp-8aa46.appspot.com";
}
