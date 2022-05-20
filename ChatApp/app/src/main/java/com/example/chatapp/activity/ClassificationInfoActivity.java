package com.example.chatapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.R;
import com.example.chatapp.dto.Message;
import com.example.chatapp.util.EmotionClassificationLogic;
import com.example.chatapp.util.EmotionProcessing;
import com.example.chatapp.util.UICallback;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClassificationInfoActivity  extends AppCompatActivity implements UICallback {
    private static final String TAG = "ChatActivity";
    List<Message> chatMessages;
    ImageView peerImageView;
    ImageView yourImageView;
    String displayName;
    String chatUserId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classification_info);
        Intent i = getIntent();
        chatMessages = (ArrayList<Message>) i.getSerializableExtra("messages");
        displayName = i.getStringExtra("displayName");
//        chatUserId = i.getStringExtra("userid");

        peerImageView = findViewById(R.id.peer_emotion_imageview);
        yourImageView = findViewById(R.id.your_emotion_imageview);

        EmotionClassificationLogic emotionClassificationLogic = new EmotionClassificationLogic(displayName);
        List<Message> peerMessages = emotionClassificationLogic.getCommonMessagesToClassify(chatMessages, peerImageView,2);
        List<Message> yourMessages = emotionClassificationLogic.getCommonMessagesToClassify(chatMessages, yourImageView,1);
        Log.i(TAG, "PeerMessages to classify: " + peerMessages.size());
        Log.i(TAG, "PeerMessages to classify: " + peerMessages.size());
        if(peerMessages.size() > 0){
            emotionClassificationLogic.performMessageClassification(getApplicationContext(), peerMessages, (UICallback) this, 2);
        }

        if(yourMessages.size() > 0){
            emotionClassificationLogic.performMessageClassification(getApplicationContext(), yourMessages, (UICallback) this, 1);
        }
    }

    @Override
    public void onFailure(String response) {

    }

    /**
     * @param type: 0 if chat emoji, 1 if your emoji, 2 if peer emoji
     */
    @Override
    public void onSuccess(List<String> responses, int type) throws IOException {
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

        ImageView emotionImageView;
        if(type == 1){
            emotionImageView = yourImageView;
        } else if(type == 2){
            emotionImageView = peerImageView;
        } else {
            return;
        }

        Log.i(TAG, "Response len: " + mergedResult.size());
        String winner = EmotionProcessing.getEmotionClassMajority(mergedResult);
        if (winner.equals("joy")) {
            Log.i(TAG, "Joy change");
            emotionImageView.setImageResource(R.drawable.ic_joy_emoji);
            emotionImageView.setTag("joy");
        } else if(winner.equals("neutral")) {
            emotionImageView.setImageResource(R.drawable.ic_neutral_emoji);
            emotionImageView.setTag("neutral");
        } else if(winner.equals("sadness")) {
            emotionImageView.setImageResource(R.drawable.ic_sad_emoji);
            emotionImageView.setTag("sadness");
        } else if(winner.equals("fear")) {
            emotionImageView.setImageResource(R.drawable.ic_fear_emoji);
            emotionImageView.setTag("fear");
        } else if(winner.equals("anger")) {
            emotionImageView.setImageResource(R.drawable.ic_angry_emoji);
            emotionImageView.setTag("fear");
        }
    }
}
