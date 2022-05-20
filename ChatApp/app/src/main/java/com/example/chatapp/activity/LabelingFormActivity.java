package com.example.chatapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.chatapp.RecyclerItemClickListener;
import com.example.chatapp.connection.FirebaseDbManager;
import com.example.chatapp.R;
import com.example.chatapp.adapter.LabelingAdapter;
import com.example.chatapp.dto.Message;
import com.example.chatapp.util.JSONBuilder;
import com.example.chatapp.connection.LocalFileManager;

import java.util.ArrayList;

public class LabelingFormActivity extends AppCompatActivity {

    String TAG = "LabelingFormActivity";
    private AppCompatActivity thisActivity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_labeling_form);
        Intent i = getIntent();
        ArrayList<Message> messageList = (ArrayList<Message>) i.getSerializableExtra("messages");
        String chatUsername = i.getStringExtra("username");
        String chatUserId = i.getStringExtra("userid");

        RecyclerView recyclerView = findViewById(R.id.recycler_label);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(new LabelingAdapter(this, messageList));
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, recyclerView ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        Message msg = messageList.get(position);
                        if(!msg.getIsAudio())
                            return;
                        String receivedRecFilePath = getExternalCacheDir().getAbsolutePath();
                        receivedRecFilePath += msg.getFilename();
                        MediaPlayer mediaPlayer = MediaPlayer.create(thisActivity, Uri.parse(receivedRecFilePath));
                        mediaPlayer.start(); // no need to call prepare(); create() does that for you
                    }

                    @Override public void onLongItemClick(View view, int position) {
                        // do whatever
                    }
                })
        );
        Button submitButton = findViewById(R.id.submit_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Getting form data");

                //Get data from recycler view (labels)
                LabelingAdapter labelingAdapter = (LabelingAdapter) recyclerView.getAdapter();
                ArrayList<ArrayList<Object>> formData = labelingAdapter.getFormData();

                String jsonToSend = JSONBuilder.buildLabelingJSON(formData);
                Log.i(TAG, "jsonToSend: " +jsonToSend);


                Long tsLong = System.currentTimeMillis()/1000;
                String ts = tsLong.toString();
                String jsonFilename = "/labeling" + ts + ".json";
                Uri uri = new LocalFileManager().createFileFromString(jsonToSend, jsonFilename, getApplicationContext());


                new FirebaseDbManager().uploadJSONLabel(uri, jsonFilename);
                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                intent.putExtra("askLabelling", false);
                intent.putExtra("chat_user_name", chatUsername);
                intent.putExtra("chat_user_uid", chatUserId);
                startActivity(intent);
            }
        }
        );
    }
}