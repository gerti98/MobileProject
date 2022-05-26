package com.example.chatapp.connection;

import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;
import com.example.chatapp.activity.ChatActivity;
import com.example.chatapp.adapter.ContactsAdapter;
import com.example.chatapp.adapter.MessageAdapter;
import com.example.chatapp.dto.Message;
import com.example.chatapp.dto.User;
import com.example.chatapp.firebasevent.FirebaseEventHandler;
import com.example.chatapp.util.Constants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class FirebaseDbManager {
    private DatabaseReference db;
    private FirebaseDatabase dbInstance;
    private StorageReference storage;
    private DatabaseReference privateDbReference;
    private ChildEventListener privateChildEventListener;

    final private String TAG = "ChatApp/DbManager";

    public void detach(){
        privateDbReference.removeEventListener(privateChildEventListener);
    }

    public FirebaseDbManager() {
        this.dbInstance = FirebaseDatabase.getInstance(Constants.dbName);
        this.db = FirebaseDatabase.getInstance(Constants.dbName).getReference();
        this.storage = FirebaseStorage.getInstance().getReference();
    }

    public FirebaseDbManager(String reference) {
        this.dbInstance = FirebaseDatabase.getInstance(Constants.dbName);
        this.db = FirebaseDatabase.getInstance(Constants.dbName).getReference(reference);
    }

    public FirebaseDatabase getFirebaseDbInstance(){
        return dbInstance;
    }

    public void getSearchResult(String text, ArrayList<User> contacts, AppCompatActivity contactsActivity){
        final DatabaseReference dbRef= dbInstance.getReference("users");

        //this reference match only email that starts with text, the event is listened one time
        dbRef.orderByChild("email").startAt(text).endAt(text+"\uf8ff").addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //clear the contacts list array
                contacts.clear();
                //Get the object and use the values to update the UI
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    User result = child.getValue(User.class);
                    //not showing the current logged user in the search result
                    if (result.getUid().equals(FirebaseAuth.getInstance().getUid())){
                        continue;
                    }
                    contacts.add(result);
                }
                //update the UI with the result of the search
                ListView lv = (ListView) contactsActivity.findViewById(R.id.contacts_list_view);
                /*lv.setAdapter(new ArrayAdapter<User>(contactsActivity, android.R.layout.simple_list_item_1, contacts));*/
                lv.setAdapter(new ContactsAdapter(contactsActivity, contacts));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // initialize the listener to update the current chat UI
    public void initializeChatsListener(AppCompatActivity chatActivity, List<Message> messageList,
    String key_chat, long openTimestamp){
        privateChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                // If no users are logged in I remove the listener
                if (FirebaseAuth.getInstance().getCurrentUser() == null){
                    db.child(key_chat+"/messages").removeEventListener(this);
                }
                else {
                    Message message = dataSnapshot.getValue(Message.class);

                    messageList.add(message);

                    RecyclerView rv = (RecyclerView) chatActivity.findViewById(R.id.recycler_gchat);
                    rv.swapAdapter(new MessageAdapter(chatActivity, messageList), false);

                    //if the messages are sent after the openTimestamp of the chat they are considered
                    //as new, so they will be taken into account for the labelling
                    if (message.getTimestamp() > openTimestamp){
                        ChatActivity.numberOfNewMessages++;
                        LinearLayoutManager layoutManager = (LinearLayoutManager) rv.getLayoutManager();
                        if (layoutManager.findLastCompletelyVisibleItemPosition() != messageList.size()-1){
                            TextView new_msg_number_text = chatActivity.findViewById(R.id.new_msg_number_text);
                            int new_msgs = Integer.parseInt(String.valueOf(new_msg_number_text.getText()));
                            new_msgs++;
                            new_msg_number_text.setText(String.valueOf(new_msgs));
                            new_msg_number_text.setTextColor(Color.parseColor("#1aab51"));
                        }
                    }

                    // if the message is of the sender the focus is at the end of the RecyclerView
                    if (message.getSender_uid().equals(FirebaseAuth.getInstance().getUid())){
                        rv.scrollToPosition(messageList.size()-1);
                    }

                    // Download the audio message if it is an audio message
                    if (!message.getIsAudio())
                        return;
                    String receivedRecFilePath = chatActivity.getExternalCacheDir().getAbsolutePath();
                    receivedRecFilePath += message.getFilename();
                    File newFile = new File(receivedRecFilePath);
                    if (!newFile.exists() || Constants.CACHE_AUDIO_MESSAGES_DISABLED){
                        new FirebaseDbManager().downloadAudio(message.getFilename(), receivedRecFilePath, chatActivity);
                    }
                }
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
        // Adding the listener to the chat
        privateDbReference = db.child(key_chat+"/messages");
        privateDbReference.limitToLast(Constants.DEFAULT_MSG_SHOWN).addChildEventListener(privateChildEventListener);
    }

    public void loadMoreMessages(AppCompatActivity chatActivity, String key_chat, int howMany, List<Message> messageList){
        ValueEventListener singleValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //first timestamp of already shown messages
                Long FirstTimestamp = messageList.get(0).getTimestamp();
                ChatActivity.numberOfLastLoadedMsgs = 0;
                List <Message> loaded_messages = new ArrayList<>();

                for (DataSnapshot result : dataSnapshot.getChildren()) {
                    Message message = result.getValue(Message.class);

                    if (message.getTimestamp() < FirstTimestamp) {
                        loaded_messages.add(message);
                        ChatActivity.numberOfLastLoadedMsgs++;
                    }
                }
                //adding the loaded of messages on the top of the chat
                Collections.reverse(loaded_messages);
                Collections.reverse(messageList);
                messageList.addAll(loaded_messages);
                Collections.reverse(messageList);

                //update the view
                RecyclerView rv = (RecyclerView) chatActivity.findViewById(R.id.recycler_gchat);
                rv.swapAdapter(new MessageAdapter(chatActivity, messageList), false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        db.child(key_chat+"/messages").limitToLast(howMany).addListenerForSingleValueEvent(singleValueListener);
    }

    public void addUserToDB(FirebaseUser user) {
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

        //notifications creation if it does not already exists
        DatabaseReference notificationIstanceRef = dbInstance.getReference().child("notifications").child(user.getUid());
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()) {
                    notificationIstanceRef.setValue("");
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, databaseError.getMessage());
            }
        };
        notificationIstanceRef.addListenerForSingleValueEvent(eventListener);
    }

    // adding a new message to the chat, if chat does not exists it creates one
    public void addMessageToChat(String key_chat, String sender, String receiver, String receiver_uid, String msg){
        HashMap<String, Object> message = new HashMap<>();
        message.put("text", msg);
        message.put("sender_name", sender);
        message.put("sender_uid", FirebaseAuth.getInstance().getUid());
        message.put("receiver_name", receiver);
        message.put("timestamp", ServerValue.TIMESTAMP);
        message.put("isAudio", false);
        DatabaseReference chatsRef = db.child("chats");

        chatsRef.child(key_chat).child("messages").push().setValue(message);

        // send notification to receiver
        updateMessageNotificationEntity(receiver_uid, sender, FirebaseAuth.getInstance().getUid(), false);
    }

    public void addAudioToChat(String filePath, String filename, String key_chat, String sender, String receiver, String receiver_uid,
                               AppCompatActivity chatActivity){
        StorageReference audioPath = storage.child("audio").child(filename);
        Uri localUri = Uri.fromFile(new File(filePath));

        audioPath.putFile(localUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Find recording duration
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(filePath);
                String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                double millSecond = Integer.parseInt(durationStr);

                String textAudioMsg = "Audio Message - " + String.valueOf(millSecond/1000) + " s";
                Toast.makeText(chatActivity, "Registration uploaded succesfully", Toast.LENGTH_LONG).show();
                HashMap<String, Object> message = new HashMap<>();
                message.put("text", textAudioMsg);
                message.put("filename", filename);
                message.put("sender_name", sender);
                message.put("sender_uid", FirebaseAuth.getInstance().getUid());
                message.put("receiver_name", receiver);
                message.put("timestamp", ServerValue.TIMESTAMP);
                message.put("isAudio", true);
                DatabaseReference chatsRef = db.child("chats");

                chatsRef.child(key_chat).child("messages").push().setValue(message);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(chatActivity, "Error during registration upload", Toast.LENGTH_LONG).show();
            }
        });

        // send notification to receiver
        updateMessageNotificationEntity(receiver_uid, sender, FirebaseAuth.getInstance().getUid(), false);
    }

    // add a notification to the notification collection of the receiver
    public void updateMessageNotificationEntity(String receiver_uid, String sender, String sender_uid, boolean checked){
        HashMap<String, Object> notification = new HashMap<>();
        notification.put("sender", sender);
        notification.put("type", "messages");
        notification.put("checked", checked);

        DatabaseReference notificationInstanceRef = dbInstance.getReference("notifications/"+receiver_uid+"/"+sender_uid);
        notificationInstanceRef.setValue(notification);
    }

    public void downloadAudio(String fileName, String whereToSave, AppCompatActivity chatActivity) {
        String filePath = "audio/" + fileName;
        StorageReference audioFileReference = storage.child(filePath);

        final long ONE_MEGABYTE = 1024 * 1024 * 10;
        audioFileReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                try (FileOutputStream fos = new FileOutputStream(whereToSave)) {
                    fos.write(bytes);
                    Toast.makeText(chatActivity, "Rec downloaded", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(chatActivity, "Error during registration download - Maybe too long recording " + fileName + " " + whereToSave, Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(chatActivity, "Error during registration download " + fileName + " " + whereToSave, Toast.LENGTH_LONG).show();
            }
        });
    }


    public void uploadJSONLabel(Uri filePath, String filenameInFirebase){
        if (filePath != null) {
            //displaying a progress dialog while upload is going on

            StorageReference jsonRef = storage.child("labels").child(filenameInFirebase);
            jsonRef.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Log.i(TAG, "File Uploaded");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Log.i(TAG, exception.getMessage());
                        }
                    });
        }
        //if there is not any file
        else {
            Log.i(TAG, "Error");
        }
    }

}
