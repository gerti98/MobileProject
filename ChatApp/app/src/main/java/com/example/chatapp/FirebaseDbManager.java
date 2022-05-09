package com.example.chatapp;

import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class FirebaseDbManager {
    private DatabaseReference db;
    final private String dbName = "https://chatapp-8aa46-default-rtdb.europe-west1.firebasedatabase.app/";
    final private String TAG = "ChatApp/DbManager";
    static boolean keychat_search; //used to perform keychat search

    public FirebaseDbManager() {
        this.db = FirebaseDatabase.getInstance(dbName).getReference();
    }

    public FirebaseDbManager(String reference) {
        this.db = FirebaseDatabase.getInstance(dbName).getReference(reference);
    }

    // initialize the listener to update contacts UI
    void initializeUsersListener(AppCompatActivity contactsActivity, ArrayList<User> contacts) {
        ValueEventListener usersListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get the object and use the values to update the UI
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    User result = child.getValue(User.class);
                    contacts.add(result);
                }
                ListView lv = (ListView) contactsActivity.findViewById(R.id.contacts_list_view);
                lv.setAdapter(new ArrayAdapter<User>(contactsActivity, android.R.layout.simple_list_item_1, contacts));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting info failed
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };

        //adding the listener to users collection
        db.addValueEventListener(usersListener);
    }

    // initialize the listener to update the current chat UI
    void initializeChatsListener(AppCompatActivity usersActivity, List<Message> messageList, String key_chat){
        ChildEventListener chatsListener = new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                Message result = dataSnapshot.getValue(Message.class);
                messageList.add(result);
                RecyclerView rv= (RecyclerView) usersActivity.findViewById(R.id.recycler_gchat);
                rv.setAdapter(new MessageAdapter(usersActivity, messageList));
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(this.getClass().toString(), "Database error: "+error.getDetails());
            }

        };
        //adding the listener to the chat
        db.child(key_chat+"/messages").addChildEventListener(chatsListener);
    }


    void addUserToDB(FirebaseUser user) {
        Log.w(TAG, "Adding new user");
        Log.w(TAG, user.getUid());
        Log.w(TAG, user.getEmail());
        Log.w(TAG, user.getDisplayName());

        DatabaseReference usersRef = db.child("users");

        /* Check if the user is already in the database. This is done using the firebase
         * uid as key, so we are sure that the user will be registered once*/
        User toAdd = new User(user.getUid(), user.getDisplayName(), user.getEmail());
        DatabaseReference newUserRef = usersRef.child(user.getUid());
        newUserRef.setValue(toAdd); /*questo risetta tutte le volte i valori e fa partire i trigger su tutti i client*/
    }

    // adding a new message to the chat, if chat does not exists it creates one
    void addMessageToChat(String key_chat, String sender, String receiver, String msg){
        HashMap<String, Object> message = new HashMap<>();
        message.put("text", msg);
        message.put("sender_name", sender);
        message.put("receiver_name", receiver);
        message.put("timestamp", ServerValue.TIMESTAMP);
        DatabaseReference chatsRef = db.child("chats");

        chatsRef.child(key_chat).child("messages").push().setValue(message);
    }
}
