package com.example.chatapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FirebaseDbManager {
    private DatabaseReference db;
    private FirebaseDatabase dbInstance;
    private StorageReference storage;
    final private String dbName = "https://chatapp-8aa46-default-rtdb.europe-west1.firebasedatabase.app/";
    final private String storageName = "gs://chatapp-8aa46.appspot.com";
    final private String TAG = "ChatApp/DbManager";

    public FirebaseDbManager() {
        this.dbInstance = FirebaseDatabase.getInstance(dbName);
        this.db = FirebaseDatabase.getInstance(dbName).getReference();
        this.storage = FirebaseStorage.getInstance().getReference();
    }

    public FirebaseDbManager(String reference) {
        this.dbInstance = FirebaseDatabase.getInstance(dbName);
        this.db = FirebaseDatabase.getInstance(dbName).getReference(reference);
    }

    public FirebaseDatabase getFirebaseDbInstance(){
        return dbInstance;
    }

    public void prova(String text){
        final DatabaseReference dbRef= dbInstance.getReference("users");

        //this reference match only email that starts with text
        dbRef.orderByChild("email").startAt(text).endAt(text+"\uf8ff").addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                System.out.println(snapshot.getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
                //if no users are logged in I remove the listener
                if (FirebaseAuth.getInstance().getCurrentUser() == null){
                    db.child(key_chat+"/messages").removeEventListener(this);
                }
                else {
                    Message result = dataSnapshot.getValue(Message.class);
                    messageList.add(result);
                    RecyclerView rv = (RecyclerView) usersActivity.findViewById(R.id.recycler_gchat);
                    rv.setAdapter(new MessageAdapter(usersActivity, messageList));

                    // Download the audio message if it is an audio message
                    if (!result.getIsAudio())
                        return;

                    String receivedRecFilePath = usersActivity.getExternalCacheDir().getAbsolutePath();
                    receivedRecFilePath += result.text;

                    File newFile = new File(receivedRecFilePath);
                    if (newFile.exists()) {
                        //Toast.makeText(usersActivity, "Rec already exists", Toast.LENGTH_SHORT).show();
                    } else {
                        //Toast.makeText(usersActivity, "Audio downloading", Toast.LENGTH_SHORT).show();
                        new FirebaseDbManager().downloadAudio(result.text, receivedRecFilePath, usersActivity);
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
    void addMessageToChat(String key_chat, String sender, String receiver, String receiver_uid, String msg){
        HashMap<String, Object> message = new HashMap<>();
        message.put("text", msg);
        message.put("sender_name", sender);
        message.put("receiver_name", receiver);
        message.put("timestamp", ServerValue.TIMESTAMP);
        message.put("isAudio", false);
        DatabaseReference chatsRef = db.child("chats");

        chatsRef.child(key_chat).child("messages").push().setValue(message);

        // send notification to receiver
        updateMessageNotificationEntity(receiver_uid, sender, FirebaseAuth.getInstance().getUid(), false);
    }

    void addAudioToChat(String filePath, String filename, String key_chat, String sender, String receiver, String receiver_uid,
    AppCompatActivity chatActivity){
        StorageReference audioPath = storage.child("audio").child(filename);
        Uri localUri = Uri.fromFile(new File(filePath));

        audioPath.putFile(localUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(chatActivity, "Registration uploaded succesfully", Toast.LENGTH_LONG).show();
                HashMap<String, Object> message = new HashMap<>();
                message.put("text", filename);
                message.put("sender_name", sender);
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

    void downloadAudio(String fileName, String whereToSave, AppCompatActivity chatActivity) {
        String filePath = "audio/" + fileName;
        StorageReference audioFileReference = storage.child(filePath);

        final long ONE_MEGABYTE = 1024 * 1024;
        audioFileReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                try (FileOutputStream fos = new FileOutputStream(whereToSave)) {
                    fos.write(bytes);
                    Toast.makeText(chatActivity, "Rec downloaded", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(chatActivity, "Error during registration download", Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(chatActivity, "Error during registration download", Toast.LENGTH_LONG).show();
            }
        });
    }

}
