package com.example.chatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;


public class ContactsActivity extends AppCompatActivity {
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

        contacts = new ArrayList<>();
        new FirebaseDbManager("users").initializeUsersListener(this, contacts);

        ListView lv = (ListView) findViewById(R.id.contacts_list_view);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Toast.makeText(thisActivity, String.valueOf(i), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                //sending an intent about the user of the opened chat
                intent.putExtra("chat_user_name", contacts.get(i).getName());
                intent.putExtra("chat_user_uid", contacts.get(i).getUid());
                startActivity(intent);
            }
        });
    }
}