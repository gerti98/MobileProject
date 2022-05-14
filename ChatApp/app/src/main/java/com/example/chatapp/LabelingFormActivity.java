package com.example.chatapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.example.chatapp.adapter.LabelingAdapter;

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
    }
}