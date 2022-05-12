package com.example.chatapp;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.util.Constants;
import com.example.chatapp.util.JSONBuilder;
import com.google.firebase.auth.FirebaseAuth;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import okhttp3.Request;
import okhttp3.RequestBody;

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
    // it creates the message items for the chat and returns it
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
