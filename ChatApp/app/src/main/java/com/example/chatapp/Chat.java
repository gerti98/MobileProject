package com.example.chatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class Chat extends AppCompatActivity {
    private String peerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Intent i = getIntent();
        String peerName = i.getStringExtra("name");
        TextView tv = (TextView) findViewById(R.id.peerName);
        tv.setText(peerName);
    }
}