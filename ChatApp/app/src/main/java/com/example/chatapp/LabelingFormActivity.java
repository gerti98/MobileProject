package com.example.chatapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.chatapp.adapter.LabelingAdapter;
import com.example.chatapp.fragment.AlertDialogueFragment;
import com.example.chatapp.util.JSONBuilder;
import com.example.chatapp.util.LocalFileManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class LabelingFormActivity extends AppCompatActivity {

    String TAG = "LabelingFormActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_labeling_form);
        ArrayList<Message> messageList = (ArrayList<Message>) getIntent().getSerializableExtra("messages");

        RecyclerView recyclerView = findViewById(R.id.recycler_label);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(new LabelingAdapter(this, messageList));

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
                Intent intent = new Intent(getApplicationContext(), ContactsActivity.class);
                startActivity(intent);
            }
        }
        );
    }
}