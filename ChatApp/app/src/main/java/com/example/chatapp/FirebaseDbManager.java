package com.example.chatapp;

import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FirebaseDbManager {
    private DatabaseReference db;
    final private String dbName = "https://chatapp-8aa46-default-rtdb.europe-west1.firebasedatabase.app/";
    final private String TAG = "ChatApp/DbManager";

    public FirebaseDbManager() {
        this.db = FirebaseDatabase.getInstance(dbName).getReference();
    }

    void initializeListener(AppCompatActivity contactsActivity, ArrayList<User> contacts) {
        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get the object and use the values to update the UI
                System.out.println(dataSnapshot);

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    System.out.println(child);
                    for (DataSnapshot child2 : child.getChildren()) {
                        User result = child2.getValue(User.class);
                        Log.w(TAG, result.toString());
                        contacts.add(result);
                    }
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
        db.addValueEventListener(userListener);
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
        newUserRef.setValue(toAdd);
    }
}
