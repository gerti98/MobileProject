package com.example.chatapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
    private EditText editTextMsg;
    private Toolbar chatToolbar;
    private RecyclerView MessageRecycler;

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
        editTextMsg = findViewById(R.id.edit_text_message);

        //Recycler
        MessageRecycler = (RecyclerView) findViewById(R.id.recycler_gchat);
        MessageRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

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


}