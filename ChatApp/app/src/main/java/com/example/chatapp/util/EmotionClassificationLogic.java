package com.example.chatapp.util;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;

import com.example.chatapp.R;
import com.example.chatapp.dto.Message;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

public class EmotionClassificationLogic {
    public static String TAG = "ChatActivity";
    public String chatUsername;

    public EmotionClassificationLogic(String chatUsername){
        this.chatUsername = chatUsername;
    }

    public List<Message> getCommonMessagesToClassify(List<Message> chatMessages, String imageTag){
        return this.getCommonMessagesToClassify(chatMessages, imageTag, 0);
    }

    /**
    * modality: integer which represent which messages need to be taken into account for classification purposes
     * if 0: Both Sender and Receiver
     *    1: Only Sender
     *    2: Only Receiver
     */
    public List<Message> getCommonMessagesToClassify(List<Message> chatMessages, String imageTag, int modality){
        List<Message> messagesToClassify = new ArrayList<>();
        List<Message> filteredMessages = this.filterMessagesByModality(chatMessages, modality);

        //Rest Api Call
        int message_size = filteredMessages.size();
        int fromIndex, lastIndex;
        int remainder = message_size % Constants.REST_API_MESSAGE_SIZE;

        //If chat is loaded, then it needs to be classified in the last classification result
        if (message_size >= Constants.REST_API_MESSAGE_SIZE &&
                imageTag.equals("wait")) {
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

    public void performMessageClassification(Context context, List<Message> messagesToClassify, UICallback callback, int type){

        List<Message> audioList = new ArrayList<Message>();
        List<Message> textList = new ArrayList<Message>();

        for(Message m: messagesToClassify){
            if(m.getIsAudio())
                audioList.add(m);
            else
                textList.add(m);
        }

        List<Request> requests = this.buildClassificationRequestList(audioList, textList, context);

        CustomAsyncTask asyncTask = new CustomAsyncTask(requests, type);
        asyncTask.setResponseCallbacks(callback);
        asyncTask.execute();
    }

    public static List<String> mergeResultsFromDifferentSources(List<String> responses){
        List<String> mergedResult = new ArrayList<>();
        List<String> result;
        for(String response: responses) {
            if(!response.contains("[")){
                String cleanResponse = response.replaceAll("^\"|\"$", "").replace("\n", "").replace("\r", "");
                mergedResult.add(cleanResponse);
            } else {
                result = new Gson().fromJson(response, List.class);
                mergedResult.addAll(result);
            }
        }

        return mergedResult;

    }
    public static void setImageViewEmoji(String winnerEmotion, ImageView emotionImageView){
        if(winnerEmotion == null)
            return;

        if (winnerEmotion.equals("joy")) {
            Log.i(TAG, "Joy change");
            emotionImageView.setImageResource(R.drawable.ic_joy_emoji);
            emotionImageView.setTag("joy");
        } else if(winnerEmotion.equals("neutral")) {
            emotionImageView.setImageResource(R.drawable.ic_neutral_emoji);
            emotionImageView.setTag("neutral");
        } else if(winnerEmotion.equals("sadness")) {
            emotionImageView.setImageResource(R.drawable.ic_sad_emoji);
            emotionImageView.setTag("sadness");
        } else if(winnerEmotion.equals("fear")) {
            emotionImageView.setImageResource(R.drawable.ic_fear_emoji);
            emotionImageView.setTag("fear");
        } else if(winnerEmotion.equals("anger")) {
            emotionImageView.setImageResource(R.drawable.ic_angry_emoji);
            emotionImageView.setTag("anger");
        }
    }

    private List<Message> filterMessagesByModality(List<Message> chatMessages, int modality){
        List<Message> filteredMessages = new ArrayList<>();
        Log.i(TAG, "New message, total: " + chatMessages.size());

        //Filtering
        if(modality != 0){
            for(Message m: chatMessages) {
                if ((m.getSender_name().equals(chatUsername) && modality == 1) ||
                        (!m.getSender_name().equals(chatUsername)) && modality == 2) {
                    filteredMessages.add(m);
                    Log.i(TAG, "Added " + m.getText());
                }
            }
        } else {
            filteredMessages = new ArrayList<>(chatMessages);
        }

        return filteredMessages;
    }

    private List<Request> buildClassificationRequestList(List<Message> audioList, List<Message> textList, Context context){
        List<Request> requests = new ArrayList<>();
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

        return requests;
    }
}
