package com.example.chatapp.util;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;

import com.example.chatapp.dto.Message;
import com.example.chatapp.fragment.AlertDialogueFragment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

public class EmotionClassificationLogic {
    public String TAG = "ChatActivity";

    public List<Message> getCommonMessagesToClassify(List<Message> chatMessages, ImageView emotionImageView){
        List<Message> messagesToClassify = new ArrayList<>();
        Log.i(TAG, "New message, total: " + chatMessages.size());
        //Rest Api Call
        int message_size = chatMessages.size();
        int fromIndex, lastIndex;
        int remainder = message_size % Constants.REST_API_MESSAGE_SIZE;

        //If chat is loaded, then it needs to be classified in the last classification result
        if (message_size >= Constants.REST_API_MESSAGE_SIZE &&
                emotionImageView.getTag().equals("wait")) {
            Log.i(TAG, "Need of reclassification for page refresh");
            fromIndex = message_size - remainder - Constants.REST_API_MESSAGE_SIZE;
            lastIndex = message_size - remainder;
            Log.i(TAG, "Created Api request: [from: " + fromIndex + ", to (exclusive): " + lastIndex + "]");
            messagesToClassify = chatMessages.subList(fromIndex, lastIndex);

        } else if (message_size >= Constants.REST_API_MESSAGE_SIZE && remainder == 0){
            Log.i(TAG, "Classificating chunk of messages");
            fromIndex = message_size - Constants.REST_API_MESSAGE_SIZE;
            lastIndex = message_size;
            Log.i(TAG, "Created Api request: [from: " + fromIndex + ", to (exclusive): " + lastIndex + "]");
            messagesToClassify = chatMessages.subList(fromIndex, lastIndex);
        }
        return messagesToClassify;
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void performMessageClassification(Context context, List<Message> messagesToClassify, UICallback callback){

        List<Message> audioList = new ArrayList<Message>();
        List<Message> textList = new ArrayList<Message>();
        List<Request> requests = new ArrayList<>();

        for(Message m: messagesToClassify){
            if(m.getIsAudio())
                audioList.add(m);
            else
                textList.add(m);
        }

        String json_text = JSONBuilder.buildMessageTextJSON(textList);
        Log.i(TAG, "JSON to send:" + json_text);

        if(!textList.isEmpty()){
            requests.add(new Request.Builder()
                    .url(Constants.URL_TEXT_MESSAGES_REST_API)
                    .post(RequestBody.create(json_text, Constants.JSON_MEDIATYPE))
                    .build());
        }

        for(Message m: audioList){
            String receivedRecFilePath = context.getExternalCacheDir().getAbsolutePath();
            receivedRecFilePath += m.getFilename();
            File file = new File(receivedRecFilePath);

            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("file", "file",
                            RequestBody.create(file, MediaType.parse("audio/vnd.wave")))
                    .build();
            requests.add(new Request.Builder()
                    .url(Constants.URL_VOICE_MESSAGES_REST_API)
                    .post(requestBody)
                    .build());
        }

        CustomAsyncTask asyncTask = new CustomAsyncTask(requests);
        asyncTask.setResponseCallbacks(callback);
        asyncTask.execute();
    }
}
