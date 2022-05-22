package com.example.chatapp.listener;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.chatapp.notification.NotificationHandlerService;


// this class is to detect when the entire application goes background or foreground
// reference: https://medium.com/@iamsadesh/android-how-to-detect-when-app-goes-background-foreground-fd5a4d331f8a

// the Application.ActivityLifecycleCallbacks contains all lifecycle methods of activity
// and triggers based on each activity state change. If an activity starts the counter is incremented
// otherwise if it is stopped it is decremented. If the counter reaches 0 the app is in background.
public class ActivityCallbacksListeners extends Application implements Application.ActivityLifecycleCallbacks {
    private int activityReferences = 0; //counts the number of activity in the started states
    private boolean isActivityChangingConfigurations = false; //to check for changing orientation of an activity

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (++activityReferences == 1 && !isActivityChangingConfigurations) {
            // App enters foreground
            Intent intent = new Intent (getApplicationContext(), NotificationHandlerService.class);
            intent.putExtra("isForeground", true);
            startService(intent);
        }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

        isActivityChangingConfigurations = activity.isChangingConfigurations();
        if (--activityReferences == 0 && !isActivityChangingConfigurations) {
            // App enters background
            Intent intent = new Intent (getApplicationContext(), NotificationHandlerService.class);
            intent.putExtra("isForeground", false);
            startService(intent);
        }
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }
}
