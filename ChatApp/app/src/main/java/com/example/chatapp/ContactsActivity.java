package com.example.chatapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import androidx.appcompat.widget.SearchView;
import android.widget.Toast;

import com.example.chatapp.FirebaseEvent.FirebaseEventHandler;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;


public class ContactsActivity extends AppCompatActivity {
    private ArrayList<User> contacts;
    final private AppCompatActivity thisActivity = this;
    final private String TAG = "ChatApp/Contacts";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check Permission
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
        } else {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            requestPermissionLauncher.launch(
                    Manifest.permission.RECORD_AUDIO);
        }

        //Start notification service for the authenticated user
        Intent intent = new Intent (getApplicationContext(), NotificationHandlerService.class);
        startService(intent);

        setContentView(R.layout.activity_contacts);
        findViewById(R.id.logout_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();

                FirebaseEventHandler.detachAll();

                //stop tracking notification service
                Intent auth_user = new Intent (getApplicationContext(), NotificationHandlerService.class);
                stopService(auth_user);

                //back to MainActivity
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
            }
        });

        contacts = new ArrayList<>();

        //GUI

        //search_box event definition, the search is by email
        SearchView search_box = findViewById(R.id.search_box);
        AppCompatActivity contextActivity = this;
        search_box.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                new FirebaseDbManager().getSearchResult(search_box.getQuery().toString(), contacts, contextActivity);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        //contacts_list event definition
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

    @Override
    public void onResume(){
        super.onResume();

        //reset the service variable to get the notifications from all the users (null value)
        Intent intent = new Intent (getApplicationContext(), NotificationHandlerService.class);
        intent.putExtra("user_active_chat", (String) null);
        //the service is already started, this will inform the service about the active user of the
        //chat to show no longer notification about his messages.
        startService(intent);
    }

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                } else {
                    Toast.makeText(ContactsActivity.this, "Since you do not give the permission you will not be able to send audio recording", Toast.LENGTH_SHORT).show();
                }
            });
}