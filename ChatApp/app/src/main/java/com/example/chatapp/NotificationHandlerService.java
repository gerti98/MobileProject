package com.example.chatapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.chatapp.FirebaseEvent.FirebaseEventHandler;
import com.example.chatapp.activity.ChatActivity;
import com.example.chatapp.connection.FirebaseDbManager;
import com.example.chatapp.dto.NotificationMessageEntity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class NotificationHandlerService extends Service {
    private DatabaseReference dbNotificationsRef;
    private String activeUserChat = null;
    private ValueEventListener notificationsListener;
    private NotificationManager notificationManager;

    public NotificationHandlerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    // the first time the service is initiated, the second calls are to set
    // the user of the current chat to understand if we have or not to send notifications
    public int onStartCommand(Intent intent,  int flags, int startId) {
        if (intent.hasExtra("user_active_chat")){
            activeUserChat = intent.getStringExtra("user_active_chat");
        }
        //START OF THE SERVICE
        else {
            //db reference to the current users notification object
            dbNotificationsRef = new FirebaseDbManager().getFirebaseDbInstance()
            .getReference("notifications/" + FirebaseAuth.getInstance().getUid());

            //notification manager creation
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            //start tracking new messages
            trackNotificationMessages();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy(){
        //when service is destroyed the notification messages are removed
        cancelAllNotification();
    }


    // track the new messages for the logged user
    public void trackNotificationMessages(){
        NotificationHandlerService context = this;
        notificationsListener = new ValueEventListener() {

            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    NotificationMessageEntity nEntity = child.getValue(NotificationMessageEntity.class);

                    //for each entry check if there are new messages from that sender and if the chat is no
                    if (!nEntity.isChecked()) {

                        //if the notification is from the user of the active chat the application do not show it but
                        //just update the status of the notification
                        if(child.getKey().equals(activeUserChat)){
                            new FirebaseDbManager("notifications").updateMessageNotificationEntity(FirebaseAuth.getInstance()
                            .getUid(), nEntity.getSender(), child.getKey(), true);
                            continue;
                        }
                        //define the pending intent to open the chat on click action
                        Intent intent = new Intent(context, ChatActivity.class);
                        intent.putExtra("chat_user_name", nEntity.getSender());
                        intent.putExtra("chat_user_uid", child.getKey());
                        PendingIntent pendingIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(),
                                intent, 0);

                        //building a new notification
                        Notification.Builder notification = new Notification.Builder(context)
                                .setSmallIcon(R.drawable.ic_msg_name)
                                .setContentText("New Messages from " + nEntity.getSender())
                                .setContentTitle("Chatapp")
                                .setContentIntent(pendingIntent);

                        //show the notification, SDK_INT >= Build.VERSION_CODES.O a NotificationChannel is required
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            //group all the message notification in the same channel
                            String channelId = "Message Notification Channel";
                            NotificationChannel channel = new NotificationChannel(
                                    channelId,
                                    "Chatapp Notifications",
                                    NotificationManager.IMPORTANCE_HIGH);
                            notificationManager.createNotificationChannel(channel);
                            notification.setChannelId(channelId);
                        }
                        Notification built_notification = notification.build();
                        //set auto-cancel after selecting
                        built_notification.flags |= Notification.FLAG_AUTO_CANCEL;
                        //The group of notifications are distinguished by tags, if the key is the same the notification
                        //is just updated
                        notificationManager.notify(child.getKey(), 0, built_notification);

                        //update checked field to true (notifications has been checked)
                        new FirebaseDbManager("notifications").updateMessageNotificationEntity(FirebaseAuth.getInstance()
                        .getUid(), nEntity.getSender(), child.getKey(), true);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(this.getClass().toString(), "Database error: "+error.getDetails());
            }

        };
        FirebaseEventHandler.addValueEvent(dbNotificationsRef, notificationsListener);
        /*dbNotificationsRef.addValueEventListener(notificationsListener);*/
    }

    public void cancelAllNotification(){
        notificationManager.cancelAll();
    }

}