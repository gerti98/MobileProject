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
    private static final String TAG = "ChatApp/ChatActivity";
    List<Message> chatMessages;
    ImageView peerImageView;
    ImageView yourImageView;
    String displayName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classification_info);
        Intent i = getIntent();
        chatMessages = (ArrayList<Message>) i.getSerializableExtra("messages");
        displayName = i.getStringExtra("displayName");
        peerImageView = findViewById(R.id.peer_emotion_imageview);
        yourImageView = findViewById(R.id.your_emotion_imageview);

        EmotionClassificationLogic emotionClassificationLogic = new EmotionClassificationLogic(displayName);
        List<Message> peerMessages = emotionClassificationLogic.getCommonMessagesToClassify(chatMessages, (String) peerImageView.getTag(),2);
        List<Message> yourMessages = emotionClassificationLogic.getCommonMessagesToClassify(chatMessages, (String) yourImageView.getTag(),1);
        Log.i(TAG, "YourMessages to classify: " + yourMessages.size());
        Log.i(TAG, "PeerMessages to classify: " + peerMessages.size());
        if(peerMessages.size() > 0){
            StringBuilder messages = new StringBuilder();
            for(Message m: peerMessages){
                messages.append(m.getText()).append(", ");
            }
            Log.i(TAG, "Classifying (peer): [" + messages + "]");
            emotionClassificationLogic.performMessageClassification(getApplicationContext(), peerMessages, (UICallback) this, 2);
        }

        if(yourMessages.size() > 0){
            StringBuilder messages = new StringBuilder();
            for(Message m: yourMessages){
                 messages.append(m.getText()).append(", ");
            }
            Log.i(TAG, "Classifying (your): [" + messages + "]");
            emotionClassificationLogic.performMessageClassification(getApplicationContext(), yourMessages, (UICallback) this, 1);
        }
    }

    @Override
    public void onClassificationFailure(String response) {

    }

    /**
     * @param type: 0 if chat emoji, 1 if your emoji, 2 if peer emoji
     */
    @Override
    public void onClassificationSuccess(List<String> responses, int type) throws IOException {
        List<String> mergedResult = EmotionClassificationLogic.mergeResultsFromDifferentSources(responses);

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
        EmotionClassificationLogic.setImageViewEmoji(winner, emotionImageView);
    }
}
