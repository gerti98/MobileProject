package com.example.chatapp.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.chatapp.activity.LabelingFormActivity;
import com.example.chatapp.dto.Message;

import java.util.ArrayList;
import java.util.List;


public class AlertDialogueFragment extends DialogFragment {
    Context applicationContext;
    ArrayList<Message> messages;

    public AlertDialogueFragment(Context context, List<Message> messages){
        this.applicationContext = context;
        this.messages = new ArrayList<Message>(messages);
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Labeling");
        builder.setMessage("Labeling of random data needed");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent intent = new Intent(applicationContext, LabelingFormActivity.class);
                intent.putExtra("messages", messages);
                startActivity(intent);
            }
        });

        // Create the AlertDialog object and return it
        return builder.create();
    }
}