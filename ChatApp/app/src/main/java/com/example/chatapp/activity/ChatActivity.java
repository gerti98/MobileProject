package com.example.chatapp.activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.chatapp.connection.FirebaseDbManager;
import com.example.chatapp.dto.Message;
import com.example.chatapp.NotificationHandlerService;
import com.example.chatapp.R;
import com.example.chatapp.RecyclerItemClickListener;
import com.example.chatapp.connection.RestApi;
import com.example.chatapp.util.EmotionProcessing;
import com.example.chatapp.util.UICallback;
import com.example.chatapp.fragment.AlertDialogueFragment;
import com.example.chatapp.util.Constants;
import com.example.chatapp.util.JSONBuilder;
import com.example.chatapp.util.WavRecorder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Request;
import okhttp3.RequestBody;


/*ideas:
https://levelup.gitconnected.com/structure-firestore-firebase-for-scalable-chat-app-939c7a6cd0f5
https://sendbird.com/developer/tutorials/android-chat-tutorial-building-a-messaging-ui
https://www.youtube.com/watch?v=1mJv4XxWlu8&list=PLzLFqCABnRQftQQETzoVMuteXzNiXmnj8&index=8
*/
public class ChatActivity extends AppCompatActivity implements UICallback {
    private FirebaseUser currentUser;
    private List<Message> chatMessages;
    private String chatUserName;
    private String chatUserUid;
    private String key_chat;
    private ImageButton sendMsgBtn;
    private ImageButton sendRecBtn;
    private EditText editTextMsg;
    private Toolbar chatToolbar;
    private FirebaseDbManager fdm_chat;
    private LinearLayoutManager layoutManager;
    private RecyclerView MessageRecycler;
    private int howManyMsgToShow;
    private final int MSG_TO_SHOW_INCREMENT = 5;
    private ImageView emotionImageView;
    private WavRecorder rec;
    private String audioFilename;
    private String recFilePath;
    final private String TAG = "ChatApp/ChatActivity";
    private AppCompatActivity thisActivity = this;
    private boolean isRecording = false;
    private boolean stop;

    //TODO: move constants into a better place

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
        emotionImageView = findViewById(R.id.emotion_imageview);

        //Recycler
        howManyMsgToShow = 15;
        fdm_chat = new FirebaseDbManager("chats");

        MessageRecycler = (RecyclerView) findViewById(R.id.recycler_gchat);
        layoutManager = new LinearLayoutManager(getApplicationContext());
        MessageRecycler.setLayoutManager(layoutManager);
        MessageRecycler.addOnItemTouchListener(
                new RecyclerItemClickListener(this, MessageRecycler ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        Message msg = chatMessages.get(position);
                        if(!msg.getIsAudio())
                            return;
                        String receivedRecFilePath = getExternalCacheDir().getAbsolutePath();
                        receivedRecFilePath += msg.getFilename();
                        MediaPlayer mediaPlayer = MediaPlayer.create(thisActivity, Uri.parse(receivedRecFilePath));
                        mediaPlayer.start(); // no need to call prepare(); create() does that for you
                    }

                    @Override public void onLongItemClick(View view, int position) {
                        // do whatever
                    }
                })
        );
        stop = false;
        MessageRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int id = layoutManager.findFirstCompletelyVisibleItemPosition();
                Log.w(TAG, " Scrolling: " + String.valueOf(id) + " - text: " + chatMessages.get(id).getText());
                Log.w(TAG, " Last completely visible: " + String.valueOf(layoutManager.findLastCompletelyVisibleItemPosition()) + " - text: " + chatMessages.get(layoutManager.findLastCompletelyVisibleItemPosition()).getText());

                // After the loading of the old messages if the user goes to the last message then focusOnLast is activated
                if(layoutManager.findLastCompletelyVisibleItemPosition()==chatMessages.size()-1)
                    fdm_chat.setFocusOnLast(true);

                // To load more users after one loading the user need to scroll down and then up
                if(id == 1 && stop)
                    stop = false;

                if(id==0 && !stop){
                    Log.w(TAG, " I need to have more messages, the last one seen is: " + String.valueOf(id));
                    chatMessages.clear();
                    howManyMsgToShow += MSG_TO_SHOW_INCREMENT;
                    fdm_chat.setFocusOnLast(false);
                    fdm_chat.initializeChatsListener(thisActivity, chatMessages, key_chat, howManyMsgToShow, MSG_TO_SHOW_INCREMENT);
                    //if(howManyMsgToShow>chatMessages.size())
                    stop = true;
                }
            }
        });


        //tell the notification handler to not show notification of the active user on the chat
        Intent intent = new Intent (getApplicationContext(), NotificationHandlerService.class);
        intent.putExtra("user_active_chat", chatUserUid);
        startService(intent);

        //Handles the logic for sending messages to the model for emotion detection
        MessageRecycler.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
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
                    List<Message> sublist = chatMessages.subList(fromIndex, lastIndex);
                    performMessageClassification(sublist);

                } else if (message_size >= Constants.REST_API_MESSAGE_SIZE && remainder == 0){
                    Log.i(TAG, "Classificating chunk of messages");
                    fromIndex = message_size - Constants.REST_API_MESSAGE_SIZE;
                    lastIndex = message_size;
                    Log.i(TAG, "Created Api request: [from: " + fromIndex + ", to (exclusive): " + lastIndex + "]");
                    List<Message> sublist = chatMessages.subList(fromIndex, lastIndex);
                    performMessageClassification(sublist);
                }

                if(message_size >= Constants.LABELLING_API_MESSAGE_SIZE && message_size % Constants.LABELLING_API_MESSAGE_SIZE == 0){
                    Log.i(TAG, "Labelling request");
                    fromIndex = message_size - Constants.LABELLING_API_MESSAGE_SIZE;
                    lastIndex = message_size;
                    Log.i(TAG, "Created Labeling request: [from: " + fromIndex + ", to (exclusive): " + lastIndex + "]");
                    List<Message> sublist = chatMessages.subList(fromIndex, lastIndex);
                    AlertDialogueFragment dialog = new AlertDialogueFragment(getApplicationContext(), sublist);
                    dialog.show(getSupportFragmentManager(), "MyDialogFragmentTag");
                }
            }
        });


        //initialize the listener for the messages
        fdm_chat.initializeChatsListener(this, chatMessages, key_chat, howManyMsgToShow, MSG_TO_SHOW_INCREMENT);

        //a message is added to the database
        sendMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = editTextMsg.getText().toString();
                if (!msg.equals("")){
                    editTextMsg.setText("");
                    new FirebaseDbManager().addMessageToChat(key_chat, currentUser.getDisplayName(), chatUserName, chatUserUid, msg);
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
                    Drawable onlineMic = getResources().getDrawable(R.drawable.mic_avail2);
                    sendRecBtn.setBackground(onlineMic);
                    stopRecording();
                }
                else {
                    // Path to save rec
                    recFilePath = getExternalCacheDir().getAbsolutePath();
                    Long tsLong = System.currentTimeMillis()/1000;
                    String ts = tsLong.toString();
                    audioFilename = "/audio" + ts + ".wav";
                    recFilePath += audioFilename;

                    isRecording = true;
                    Drawable busyMic = getResources().getDrawable(R.drawable.mic_busy);
                    sendRecBtn.setBackground(busyMic);
                    startRecording();
                }
            }
        });
    }

    /*void getMoreMessages(String id){
        Query ref = FirebaseDatabase.getInstance().getReference()
                .child("database")
                .child("post")
                .orderByChild("date")
                .startAt(id)
                .limitToFirst(1);
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    Post post = dataSnapshot.getValue(Post.class);
                    if(!posts.contains(post)) {
                        posts.add(post);
                        postAdapter.setData(posts);
                        postAdapter.notifyDataSetChanged();
                    }
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }*/

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
        rec = new WavRecorder(getExternalCacheDir().getAbsolutePath(), "/tempraw.raw", audioFilename);
        // Check Permission
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
            rec.startRecording();
        } else {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            requestPermissionLauncher.launch(
                    Manifest.permission.RECORD_AUDIO);
        }
    }

    private void stopRecording() {
        Toast.makeText(this, "Recording Stopped", Toast.LENGTH_SHORT).show();
        rec.stopRecording();
        rec = null;
        sendAudio();
    }

    private void sendAudio() {
        // Temporary: only for testing purposes
        MediaPlayer mediaPlayer = MediaPlayer.create(this, Uri.parse(recFilePath));
        mediaPlayer.start(); // no need to call prepare(); create() does that for you

        new FirebaseDbManager().addAudioToChat(recFilePath, audioFilename, key_chat, currentUser.getDisplayName(),
        chatUserName, chatUserUid, this);
    }


    @Override
    public void onFailure(String response) {
        Log.i(TAG, "Response: " + response);
    }

    @Override
    public void onSuccess(String response) {
        List<String> result = new Gson().fromJson(response, List.class);
//        String cleanResponse = response.replaceAll("^\"|\"$", "").replace("\n", "").replace("\r", "");
        Log.i(TAG, "Response: " + result);
        Log.i(TAG, "Response len: " + result.size());

        String winner = EmotionProcessing.getEmotionClassMajority(result);
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

    public void performMessageClassification(List<Message> subList){
        String json = JSONBuilder.buildMessageJSON(subList);
        Log.i(TAG, "Sent JSON:" + json);

        RestApi api = new RestApi();
        api.setUICallback((UICallback) thisActivity);
        api.makeRequest(new Request.Builder()
                .url(Constants.URL_TEXT_MESSAGES_REST_API)
                .post(RequestBody.create(json, Constants.JSON_MEDIATYPE))
                .build());

    }

    private ActivityResultLauncher<String> requestPermissionLauncher =
            this.registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                    rec.startRecording();
                } else {
                    Toast.makeText(this, "Since you do not give the permission you will not be able to send audio recording", Toast.LENGTH_SHORT).show();
                    rec = null;
                }
            });
}