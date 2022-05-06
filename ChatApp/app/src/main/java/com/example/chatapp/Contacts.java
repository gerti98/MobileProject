package com.example.chatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class Contacts extends AppCompatActivity {
    private DatabaseReference db;
    final private String dbName = "https://chatapp-8aa46-default-rtdb.europe-west1.firebasedatabase.app/";
    private ArrayList<User> contacts;
    final private AppCompatActivity thisActivity = this;
    final private String TAG = "ChatApp/Contacts";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        findViewById(R.id.logout_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
            }
        });

        db = FirebaseDatabase.getInstance(dbName).getReference();
        contacts = new ArrayList<>();
        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get the object and use the values to update the UI
                System.out.println(dataSnapshot);

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    System.out.println(child);
                    User result = child.getValue(User.class);
                    Log.w(TAG, result.toString());
                    contacts.add(result);
                }
                ListView lv = (ListView) findViewById(R.id.contacts_list_view);
                lv.setAdapter(new ArrayAdapter<User>(thisActivity, android.R.layout.simple_list_item_1, contacts));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting info failed
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
        db.addValueEventListener(userListener);
    }
}