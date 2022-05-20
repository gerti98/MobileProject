package com.example.chatapp.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.example.chatapp.R;
import com.example.chatapp.activity.LabelingFormActivity;
import com.example.chatapp.dto.Message;

import java.util.ArrayList;
import java.util.List;

public class EmotionClassificationBySenderAlertDialogueFragment extends DialogFragment {
    Context applicationContext;
    ArrayList<Message> messages;
    View dialogView;
    ImageView peerImageView;
    ImageView yourImageView;
    AppCompatActivity activity;

    public EmotionClassificationBySenderAlertDialogueFragment(Context context, List<Message> messages){
        this.applicationContext = context;
        this.messages = new ArrayList<Message>(messages);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(applicationContext);

        LayoutInflater inflater = this.getLayoutInflater();
        peerImageView = dialogView.findViewById(R.id.peer_emotion_imageview);
        yourImageView = dialogView.findViewById(R.id.your_emotion_imageview);
        dialogBuilder.setView(dialogView);
        return dialogBuilder.create();
    }


    public ImageView getPeerImageView() {
        return peerImageView;
    }

    public ImageView getYourImageView(){
        return yourImageView;
    }

}
