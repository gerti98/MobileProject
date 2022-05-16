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
                            Log.i(TAG, "["+position+"] Spinner selected, value: " + labelSpinner.getSelectedItem().toString());
                            Log.i(TAG, "["+position+"]["+1+"]Data before, value: " +   ((Message) messageAndLabels.get(position).get(0)).getText() + ", " + messageAndLabels.get(position).get(1));
                            messageAndLabels.get(position).set(1, selected);
                            Log.i(TAG, "["+position+"]["+1+"]Data then, value: " +   ((Message) messageAndLabels.get(position).get(0)).getText() + ", " + messageAndLabels.get(position).get(1));
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

/*
// This class is to update the chat UI and to choose 2 different layout for received and sent messages
public class MessageAdapter extends RecyclerView.Adapter {
    private static final String TAG = "ChatApp/MessageAdapter";
    private Context messageContext;
    private List<Message> messageList;
    private static final int MESSAGE_SENT_TYPE = 1;
    private static final int MESSAGE_RECEIVED_TYPE = 2;

    public MessageAdapter(Context mContext, List<Message> mList) {
        messageContext = mContext;
        messageList = mList;
    }

    // Determine the type of the message: sent or received
    @Override
    public int getItemViewType(int position) {
        Message message = (Message) messageList.get(position);

        //if the current user is in the sender, show his messages in sender pov
        if (message.getSender_name().equals(FirebaseAuth.getInstance().getCurrentUser().getDisplayName())) {
            return MESSAGE_SENT_TYPE;
        } else {
            return MESSAGE_RECEIVED_TYPE;
        }
    }

    // Return the item count of the message list
    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    // call the corresponding bind depending on the type of the message
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = (Message) messageList.get(position);


        switch (holder.getItemViewType()) {
            case MESSAGE_SENT_TYPE:
                ((SentMessage) holder).bind(message);
                break;
            case MESSAGE_RECEIVED_TYPE:
                ((ReceivedMessage) holder).bind(message);
        }
    }

    @NonNull
    @Override
    // it creates the message it ems for the chat and returns it
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int msgType) {
        View view;


        if (msgType == MESSAGE_SENT_TYPE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sender_msg_item, parent, false);
            return new SentMessage(view);
        } else if (msgType == MESSAGE_RECEIVED_TYPE) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.receiver_msg_item, parent, false);
            return new ReceivedMessage(view);
        }

        // unexpected behavior, no such type exists
        return null;
    }


    // Inner classes to display sent AND received messages in a different way
    // Using 2 different classes we can maintain 2 different styles for the messages
    private class SentMessage extends RecyclerView.ViewHolder {
        TextView messageText, timeText;

        SentMessage(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.text_gchat_message_sent);
            timeText = (TextView) itemView.findViewById(R.id.text_gchat_timestamp_sent);
        }

        void bind(Message message) {
            messageText.setText(message.getText());
            Date date = new Date(message.getTimestamp());
            Format format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            timeText.setText(format.format(date));
        }
    }

    private class ReceivedMessage extends RecyclerView.ViewHolder {
        TextView messageText, timeText;

        ReceivedMessage(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.text_gchat_message_received);
            timeText = (TextView) itemView.findViewById(R.id.text_gchat_timestamp_received);
        }

        void bind(Message message) {
            messageText.setText(message.getText());
            Date date = new Date(message.getTimestamp());
            Format format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            timeText.setText(format.format(date));
        }
    }

}

 */