package com.example.chatapp;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class NotificationHandlerService extends Service {
    private DatabaseReference db_notifications_ref;

    public NotificationHandlerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public int onStartCommand(Intent intent,  int flags, int startId) {
        //db reference to the current users notification object
        db_notifications_ref = new FirebaseDbManager().getFirebaseDbInstance()
        .getReference("notifications/"+FirebaseAuth.getInstance().getUid());

        //start tracking new messages
        trackNotificationMessages();
        return START_NOT_STICKY;
    }



    // track the new messages for the logged user
    public void trackNotificationMessages(){
        NotificationHandlerService context = this;
        ValueEventListener notificationsListener = new ValueEventListener() {

            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //if no users are logged in I remove the listener
                if (FirebaseAuth.getInstance().getCurrentUser() == null){
                    System.out.println("removing "+FirebaseAuth.getInstance().getCurrentUser());
                    db_notifications_ref.removeEventListener(this);
                }
                else{
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        NotificationMessageEntity nEntity = child.getValue(NotificationMessageEntity.class);

                        //for each entry check if there are new messages from that sender
                        if (!nEntity.isChecked()) {
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
                            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
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
                            .getUid(), nEntity.sender, child.getKey(), true);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(this.getClass().toString(), "Database error: "+error.getDetails());
            }

        };
        db_notifications_ref.addValueEventListener(notificationsListener);
    }

}