package com.example.chatapp.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.dto.Message;
import com.example.chatapp.R;

import java.util.ArrayList;

public class LabelingAdapter extends RecyclerView.Adapter{
    private static final String TAG = "LabelingFormActivity";
    private static final int AUDIO_MESSAGE = 0;
    private static final int TEXT_MESSAGE = 1;

    private Context labelingContext;
    private ArrayList<Message> messageList;
    private ArrayList<ArrayList<Object>> messageAndLabels;

    public LabelingAdapter(Context mContext, ArrayList<Message> mList) {
        labelingContext = mContext;
        messageList = mList;
        messageAndLabels = new ArrayList<>();
        ArrayList<Object> temp;

        for(Message m: messageList){
            temp = new ArrayList<>();
            temp.add(m);
            temp.add("neutral");

            messageAndLabels.add(temp);
        }
    }


    public ArrayList<ArrayList<Object>> getFormData(){
        return messageAndLabels;
    }


    // Determine the type of the message: sent or received
    @Override
    public int getItemViewType(int position) {
        Message message = (Message) messageList.get(position);

        Log.i(TAG, "getItemViwType of " + position);
        //if the current user is in the sender, show his messages in sender pov
        if (message.getIsAudio()) {
            return AUDIO_MESSAGE;
        } else {
            return TEXT_MESSAGE;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        Log.i(TAG, "onCreateViewHolder");


        if (viewType == AUDIO_MESSAGE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.text_labeling_item, parent, false);
            return new AudioMessage(view);
        } else if (viewType == TEXT_MESSAGE) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.text_labeling_item, parent, false);
            return new TextMessage(view);
        }

        // unexpected behavior, no such type exists
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = (Message) messageList.get(position);
        switch (holder.getItemViewType()) {
            case AUDIO_MESSAGE:
                ((AudioMessage) holder).bind(message, position);
                break;
            case TEXT_MESSAGE:
                ((TextMessage) holder).bind(message, position);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    // Inner classes to display sent AND received messages in a different way
    // Using 2 different classes we can maintain 2 different styles for the messages
    private class AudioMessage extends RecyclerView.ViewHolder {
        EditText messageText;
        Spinner labelSpinner;
        int index;

        AudioMessage(View itemView) {
            super(itemView);
            messageText = (EditText) itemView.findViewById(R.id.label_edit);
            labelSpinner = (Spinner) itemView.findViewById(R.id.label_spinner);
            labelSpinner.setOnItemSelectedListener(
                    new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                            String selected = labelSpinner.getSelectedItem().toString();
                            Log.i(TAG, "["+index+"] Spinner selected, value: " + labelSpinner.getSelectedItem().toString());
                            Log.i(TAG, "["+index+"]["+1+"]Data before, value: " +   ((Message) messageAndLabels.get(index).get(0)).getText() + ", " + messageAndLabels.get(index).get(1));
                            messageAndLabels.get(index).set(1, selected);
                            Log.i(TAG, "["+index+"]["+1+"]Data then, value: " +   ((Message) messageAndLabels.get(index).get(0)).getText() + ", " + messageAndLabels.get(index).get(1));
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parentView) {
                            // your code here
                        }

                    }
            );
        }

        void bind(Message message, int position) {
            this.index = position;
            messageText.setText(message.getText());
        }
    }

    private class TextMessage extends RecyclerView.ViewHolder {
        EditText messageText;
        Spinner labelSpinner;
        int index;

        TextMessage(View itemView) {
            super(itemView);
            messageText = (EditText) itemView.findViewById(R.id.label_edit);
            labelSpinner = (Spinner) itemView.findViewById(R.id.label_spinner);
            labelSpinner.setOnItemSelectedListener(
                    new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                            String selected = labelSpinner.getSelectedItem().toString();
                            Log.i(TAG, "["+index+"] Spinner selected, value: " + labelSpinner.getSelectedItem().toString());
                            Log.i(TAG, "["+index+"]["+1+"]Data before, value: " +   ((Message) messageAndLabels.get(index).get(0)).getText() + ", " + messageAndLabels.get(index).get(1));
                            messageAndLabels.get(index).set(1, selected);
                            Log.i(TAG, "["+index+"]["+1+"]Data then, value: " +   ((Message) messageAndLabels.get(index).get(0)).getText() + ", " + messageAndLabels.get(index).get(1));
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parentView) {
                            // your code here
                        }

                    }
            );
        }

        void bind(Message message, int position) {
            this.index = position;
            messageText.setText(message.getText());
        }
    }
}
