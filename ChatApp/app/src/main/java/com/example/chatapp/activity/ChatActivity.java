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
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.connection.FirebaseDbManager;
import com.example.chatapp.dto.Message;
import com.example.chatapp.notification.NotificationHandlerService;
import com.example.chatapp.R;
import com.example.chatapp.listener.RecyclerItemClickListener;
import com.example.chatapp.util.EmotionProcessing;
import com.example.chatapp.util.EmotionClassificationLogic;
import com.example.chatapp.util.UICallback;
import com.example.chatapp.fragment.LabelingRequiredAlertDialogueFragment;
import com.example.chatapp.util.Constants;
import com.example.chatapp.util.WavRecorder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class ChatActivity extends AppCompatActivity implements UICallback {
    private FirebaseUser currentUser;
    private List<Message> chatMessages;
    private String chatUserName;
    private String chatUserUid;
    private String key_chat;
    private ImageButton sendMsgBtn;
    private ImageButton sendRecBtn;
    private ImageButton loadMessagesBtn;
    private ImageButton dropChatDownBtn;
    private EditText editTextMsg;
    private TextView newMsgNumberText;
    private Toolbar chatToolbar;
    private FirebaseDbManager fdm_chat;
    private LinearLayoutManager layoutManager;
    private RecyclerView MessageRecycler;
    private ImageView emotionImageView;
    private WavRecorder rec;
    private String audioFilename;
    private String recFilePath;
    final private String TAG = "ChatApp/ChatActivity";
    private AppCompatActivity thisActivity = this;
    private boolean isRecording = false;
    private boolean stop;
    private boolean askLabelling;
    public static int numberOfLastLoadedMsgs = -1; //-1 to distinguish between opened chat and chat with no loaded messages
    public static int numberOfNewMessages = 0;
    private DatabaseReference dbRefMessages;
    private ChildEventListener childEventListener;
    private long openChatTimestamp;

    //TODO: move constants into a better place

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        openChatTimestamp = System.currentTimeMillis();

        //chat info
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Intent i = getIntent();
        chatUserName = i.getStringExtra("chat_user_name");
        chatUserUid = i.getStringExtra("chat_user_uid");
        askLabelling = i.getBooleanExtra("askLabelling", true);
        key_chat = establishKeychat(currentUser.getUid(), chatUserUid);
        chatMessages = new ArrayList<>();

        //UI init
        chatToolbar = findViewById(R.id.chat_toolbar);
        chatToolbar.setTitle(chatUserName);
        sendMsgBtn = findViewById(R.id.send_msg_btn);
        sendRecBtn = findViewById(R.id.send_rec_btn);
        loadMessagesBtn = findViewById(R.id.load_messages_button);
        dropChatDownBtn = findViewById(R.id.drop_chat_down_btn);

        editTextMsg = findViewById(R.id.edit_text_message);
        emotionImageView = findViewById(R.id.emotion_imageview);
        newMsgNumberText = findViewById(R.id.new_msg_number_text);

        addEmotionImageViewListener();
        setMessageRecycler();

        //tell the notification handler to not show notification of the active user when app is in
        //foreground
        Intent intent = new Intent (getApplicationContext(), NotificationHandlerService.class);
        intent.putExtra("user_active_chat", chatUserUid);
        startService(intent);

        /*System.out.println("ehi" + chatUserName + chatUserUid + askLabelling);*/

        //initialize the listener for the messages
        fdm_chat = new FirebaseDbManager("chats");
        fdm_chat.initializeChatsListener(this, chatMessages, key_chat, openChatTimestamp);
        setChatButtons();
    }

    //If Emoji is pressed new info about Emotion per sender are shown
    private void addEmotionImageViewListener(){
        emotionImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ClassificationInfoActivity.class);
                intent.putExtra("messages", (Serializable) chatMessages);
                intent.putExtra("displayName", currentUser.getDisplayName());
                startActivity(intent);
            }
        });
    }

    //set message recycler parameters and initialize listeners
    private void setMessageRecycler(){
        MessageRecycler = (RecyclerView) findViewById(R.id.recycler_gchat);
        layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setStackFromEnd(true);
        MessageRecycler.setLayoutManager(layoutManager);

        MessageRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if(layoutManager.findLastCompletelyVisibleItemPosition()==chatMessages.size()-1) {
                    newMsgNumberText.setText(String.valueOf(0));
                    newMsgNumberText.setTextColor(Color.BLACK);
                    /*newMsgNumberText.setTextColor(getResources().getColor(R.color.black))*/;
                }
            }
        });

        setMessageRecyclerTouchListener();
        setMessageRecyclerLayoutChangeListener();
    }

    //Add possibility to listen to an audio by tapping the related message
    private void setMessageRecyclerTouchListener(){
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
    }


    //Handles the logic for sending messages to the model for emotion detection
    private void setMessageRecyclerLayoutChangeListener(){
        MessageRecycler.addOnLayoutChangeListener((view, i, i1, i2, i3, i4, i5, i6, i7) -> {
            int message_size = chatMessages.size();
            int fromIndex, lastIndex;
            EmotionClassificationLogic classificationLogic = new EmotionClassificationLogic(chatUserName);
            List<Message> messagesToClassify = classificationLogic.getCommonMessagesToClassify
            (chatMessages, (String) emotionImageView.getTag());

            if(messagesToClassify.size() > 0){
                classificationLogic.performMessageClassification(getApplicationContext(),
                messagesToClassify, (UICallback) thisActivity, 0);
            }

            //Check need of manual labelling
            Log.w(TAG, "ask labelling is " + String.valueOf(askLabelling));
            Log.w(TAG, "Message size " + message_size);

            //check if labelling has been already done or not, if message_size is more than 0 and if the new messages have
            //reached the threshold for the labelling
            /*System.out.println(numberOfNewMessages);
            System.out.println(askLabelling);*/

            //resetting the possibility to ask for labelling
            if(numberOfNewMessages % Constants.LABELLING_API_MESSAGE_SIZE != 0)
                askLabelling = true;

            if(askLabelling && message_size >= Constants.LABELLING_API_MESSAGE_SIZE
            && numberOfNewMessages>0 && numberOfNewMessages % Constants.LABELLING_API_MESSAGE_SIZE == 0
            && Constants.LABELLING_REQUIRED){
                Log.i(TAG, "Labelling request");
                askLabelling = false;
                fromIndex = message_size - Constants.LABELLING_API_MESSAGE_SIZE;
                lastIndex = message_size;
                Log.i(TAG, "Created Labeling request: [from: " + fromIndex + ", to (exclusive): " + lastIndex + "]");
                List<Message> sublist = chatMessages.subList(fromIndex, lastIndex);
                LabelingRequiredAlertDialogueFragment dialog = new LabelingRequiredAlertDialogueFragment(
                getApplicationContext(), sublist, chatUserUid, chatUserName);
                dialog.show(getSupportFragmentManager(), "MyDialogFragmentTag");
            }
        });
    }


    private void setChatButtons(){
        //a message is added to the database
        sendMsgBtn.setOnClickListener(v -> {
            String msg = editTextMsg.getText().toString();
            if (!msg.equals("")){
                editTextMsg.setText("");
                new FirebaseDbManager().addMessageToChat(key_chat, currentUser.getDisplayName(),
                chatUserName, chatUserUid, msg);
            }
        });

        // Registration button utils and listener
        Log.w(TAG, "File path:" + recFilePath);

        sendRecBtn.setOnClickListener(v -> {
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
        });

        loadMessagesBtn.setOnClickListener(v -> {
            //load more messages from the firebase server if before the loading was not 0 elements or
            //les than Constants.MSG_TO_SHOW_INCREMENT
            if (numberOfLastLoadedMsgs !=0 && numberOfLastLoadedMsgs % Constants.MSG_TO_SHOW_INCREMENT == 0
                && chatMessages.size()>0) {
                int howManyMsgToShow = Constants.MSG_TO_SHOW_INCREMENT+chatMessages.size();
                fdm_chat.loadMoreMessages(thisActivity, key_chat, howManyMsgToShow, chatMessages);
            }
            //distinguish the case of new opened chat and a chat that has no more messages to load
            else if(numberOfLastLoadedMsgs == -1 && chatMessages.size()>0){
                numberOfLastLoadedMsgs = 0;
                int howManyMsgToShow = Constants.MSG_TO_SHOW_INCREMENT+chatMessages.size();
                fdm_chat.loadMoreMessages(thisActivity, key_chat, howManyMsgToShow, chatMessages);
            }
            else{
                numberOfLastLoadedMsgs = 0;
            }
        });

        dropChatDownBtn.setOnClickListener(v -> {
            MessageRecycler.scrollToPosition(chatMessages.size()-1);
        });
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //detach the event onChildAdded
        fdm_chat.detach();
        //resetting the parameters for loading new messages
        numberOfLastLoadedMsgs = -1;
        // Return to contacts and not to the labelling form
        Intent intent = new Intent(getApplicationContext(), ContactsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onStop(){
        super.onStop();
        numberOfNewMessages = 0;
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
        // Uncomment only for debuggin purposes
        // MediaPlayer mediaPlayer = MediaPlayer.create(this, Uri.parse(recFilePath));
        // mediaPlayer.start(); // no need to call prepare(); create() does that for you
        new FirebaseDbManager().addAudioToChat(recFilePath, audioFilename, key_chat, currentUser.getDisplayName(),
        chatUserName, chatUserUid, this);
    }


    @Override
    public void onClassificationFailure(String response) {
        Log.i(TAG, "Response: " + response);
    }

    /**
     * Obtain results from Rest API and process it
     * @param type: 0 if chat emoji, 1 if your emoji, 2 if peer emoji
     */
    @Override
    public void onClassificationSuccess(List<String> responses, int type) {
        List<String> mergedResult = EmotionClassificationLogic.mergeResultsFromDifferentSources(responses);
        Log.i(TAG, "Response len: " + mergedResult.size());
        String winner = EmotionProcessing.getEmotionClassMajority(mergedResult);
        EmotionClassificationLogic.setImageViewEmoji(winner, emotionImageView);
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