package com.example.chatapp.connection;

import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;
import com.example.chatapp.adapter.MessageAdapter;
import com.example.chatapp.dto.Message;
import com.example.chatapp.dto.User;
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

    public void getSearchResult(String text, ArrayList<User> contacts, AppCompatActivity contactsActivity){
        final DatabaseReference dbRef= dbInstance.getReference("users");

        //this reference match only email that starts with text
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
                lv.setAdapter(new ArrayAdapter<User>(contactsActivity, android.R.layout.simple_list_item_1, contacts));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // initialize the listener to update the current chat UI
    public void initializeChatsListener(AppCompatActivity usersActivity, List<Message> messageList, String key_chat){
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

                    rv.scrollToPosition(messageList.size()-1);

                    // Download the audio message if it is an audio message
                    if (!result.getIsAudio())
                        return;

                    String receivedRecFilePath = usersActivity.getExternalCacheDir().getAbsolutePath();
                    receivedRecFilePath += result.getFilename();

                    File newFile = new File(receivedRecFilePath);
                    if (newFile.exists()) {
                        //Toast.makeText(usersActivity, "Rec already exists", Toast.LENGTH_SHORT).show();
                    } else {
                        //Toast.makeText(usersActivity, "Audio downloading", Toast.LENGTH_SHORT).show();
                        new FirebaseDbManager().downloadAudio(result.getFilename(), receivedRecFilePath, usersActivity);
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
                    Toast.makeText(chatActivity, "Error during registration download " + fileName + " " + whereToSave, Toast.LENGTH_LONG).show();
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
