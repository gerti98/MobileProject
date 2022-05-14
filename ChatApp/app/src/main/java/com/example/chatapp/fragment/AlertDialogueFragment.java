package com.example.chatapp.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.chatapp.ContactsActivity;
import com.example.chatapp.LabelingFormActivity;
import com.example.chatapp.Message;
import com.example.chatapp.R;

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