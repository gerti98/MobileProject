package com.example.chatapp.activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ListView;
import androidx.appcompat.widget.SearchView;
import android.widget.Toast;

import com.example.chatapp.adapter.ContactsAdapter;
import com.example.chatapp.FavoritesHandler;
import com.example.chatapp.connection.FirebaseDbManager;
import com.example.chatapp.firebaseevent.FirebaseEventHandler;
import com.example.chatapp.NotificationHandlerService;
import com.example.chatapp.R;
import com.example.chatapp.dto.User;
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
        findViewById(R.id.logout_button).setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();

            //detach all the firebase events regarding this account
            FirebaseEventHandler.detachAll();

            //stop tracking notification service
            Intent auth_user = new Intent (getApplicationContext(), NotificationHandlerService.class);
            stopService(auth_user);

            //save all the current favorites in the persistent file
            /*FavoritesHandler.saveUserFavorites(getApplicationContext());
            FavoritesHandler.clearFavoritesFromMemory();*/

            //back to MainActivity
            Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent1);
        });

        contacts = new ArrayList<>();

        //GUI

        //search_box event definition, the search is by email
        SearchView search_box = findViewById(R.id.search_box);
        AppCompatActivity contextActivity = this;
        Context context = getApplicationContext();
        search_box.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                new FirebaseDbManager().getSearchResult(search_box.getQuery().toString(), contacts, contextActivity);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.equals(""))
                    resetFavorites();
                return false;
            }
        });

        //load favorites from file
        FavoritesHandler.loadUserFavorites(getApplicationContext());
        resetFavorites();
    }

    @Override
    public void onStop(){
        super.onStop();
        //when the activity stops the favorites are saved to the file
        FavoritesHandler.saveUserFavorites(getApplicationContext());
        if (FirebaseAuth.getInstance().getUid() == null) {
            FavoritesHandler.clearFavoritesFromMemory();
        }
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

        //reset the favorites again
        resetFavorites();
    }

    // reset the favorites from memory
    private void resetFavorites(){
        ArrayList<User> usersList = FavoritesHandler.getFavoritesList();
        contacts.clear();
        if (usersList != null) {
            contacts.addAll(usersList);
        }
        ListView lv = (ListView) findViewById(R.id.contacts_list_view);
        lv.setAdapter(new ContactsAdapter(this, contacts));
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