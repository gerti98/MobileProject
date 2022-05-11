package com.example.chatapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;


/*ideas:
https://levelup.gitconnected.com/structure-firestore-firebase-for-scalable-chat-app-939c7a6cd0f5
https://sendbird.com/developer/tutorials/android-chat-tutorial-building-a-messaging-ui
https://www.youtube.com/watch?v=1mJv4XxWlu8&list=PLzLFqCABnRQftQQETzoVMuteXzNiXmnj8&index=8
*/
public class ChatActivity extends AppCompatActivity {
    private FirebaseUser currentUser;
    private List<Message> chatMessages;
    private String chatUserName;
    private String chatUserUid;
    private String key_chat;
    private ImageButton sendMsgBtn;
    private ImageButton sendRecBtn;
    private EditText editTextMsg;
    private Toolbar chatToolbar;
    private RecyclerView MessageRecycler;
    private MediaRecorder rec;
    private String audioFilename;
    private String recFilePath;
    final private String TAG = "ChatApp/ChatActivity";
    private AppCompatActivity thisActivity = this;
    private boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //chat info
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Intent i = getIntent();
        chatUserName = i.getStringExtra("chat_user_name");
        chatUserUid = i.getStringExtra("chat_user_uid");
        key_chat = establishKeychat(currentUser.getUid(), chatUserUid);
        chatMessages = new ArrayList<>();

        //UI init
        chatToolbar = findViewById(R.id.chat_toolbar);
        chatToolbar.setTitle(chatUserName);
        sendMsgBtn = findViewById(R.id.send_msg_btn);
        sendRecBtn = findViewById(R.id.send_rec_btn);
        editTextMsg = findViewById(R.id.edit_text_message);

        //Recycler
        MessageRecycler = (RecyclerView) findViewById(R.id.recycler_gchat);
        MessageRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        MessageRecycler.addOnItemTouchListener(
                new RecyclerItemClickListener(this, MessageRecycler ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                       /* Message msg = chatMessages.get(position);
                        if(!msg.getIsAudio())
                            return;
                        Toast.makeText(thisActivity, "Audio downloading", Toast.LENGTH_SHORT).show();
                        String receivedRecFilePath = getExternalCacheDir().getAbsolutePath();
                        receivedRecFilePath += msg.text;

                        new FirebaseDbManager().downloadAudio(msg.text, receivedRecFilePath, thisActivity);*/

                        Message msg = chatMessages.get(position);
                        if(!msg.getIsAudio())
                            return;
                        String receivedRecFilePath = getExternalCacheDir().getAbsolutePath();
                        receivedRecFilePath += msg.text;
                        MediaPlayer mediaPlayer = MediaPlayer.create(thisActivity, Uri.parse(receivedRecFilePath));
                        mediaPlayer.start(); // no need to call prepare(); create() does that for you
                    }

                    @Override public void onLongItemClick(View view, int position) {
                        // do whatever
                    }
                })
        );

        //initialize the listener for the messages
        new FirebaseDbManager("chats").initializeChatsListener(this, chatMessages, key_chat);

        //a message is added to the database
        sendMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = editTextMsg.getText().toString();
                if (!msg.equals("")){
                    editTextMsg.setText("");
                    new FirebaseDbManager().addMessageToChat(key_chat, currentUser.getDisplayName(), chatUserName, msg);
                }
            }
        });

        // Registration button utils and listener
        Log.w(TAG, "File path:" + recFilePath);

        sendRecBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isRecording){
                    isRecording = false;
                    int idOnlineMic = getResources().getIdentifier("@android:drawable/presence_audio_online", null, getPackageName());
                    Drawable onlineMic = getResources().getDrawable(idOnlineMic);
                    sendRecBtn.setBackground(onlineMic);
                    stopRecording();
                }
                else {
                    // Path to save rec
                    recFilePath = getExternalCacheDir().getAbsolutePath();
                    Long tsLong = System.currentTimeMillis()/1000;
                    String ts = tsLong.toString();
                    audioFilename = "/audio" + ts + ".aac";
                    recFilePath += audioFilename;

                    isRecording = true;
                    int idBusyMic = getResources().getIdentifier("@android:drawable/presence_audio_busy", null, getPackageName());
                    Drawable busyMic = getResources().getDrawable(idBusyMic);
                    sendRecBtn.setBackground(busyMic);
                    startRecording();
                }
            }
        });
    }

    // function to establish the unique key_chat identifier based on the user's uid of the chat
    public String establishKeychat(String uid1, String uid2){
        if (uid1.compareTo(uid2)>0){
            return uid1 + uid2;
        }
        else{
            return uid2 + uid1;
        }
    }

    private void startRecording() {
        Toast.makeText(this, "Recording Started", Toast.LENGTH_SHORT).show();
        rec = new MediaRecorder();
        rec.setAudioSource(MediaRecorder.AudioSource.MIC);
        rec.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
        rec.setOutputFile(recFilePath);
        rec.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        try {
            rec.prepare();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }
        rec.start();
    }

    private void stopRecording() {
        Toast.makeText(this, "Recording Stopped", Toast.LENGTH_SHORT).show();
        rec.stop();
        rec.reset();
        rec.release();
        rec = null;

        sendAudio();
    }

    private void sendAudio() {
        // Temporary: only for testing purposes
        MediaPlayer mediaPlayer = MediaPlayer.create(this, Uri.parse(recFilePath));
        mediaPlayer.start(); // no need to call prepare(); create() does that for you

        new FirebaseDbManager().addAudioToChat(recFilePath, audioFilename, key_chat, currentUser.getDisplayName(), chatUserName, this);
    }


}