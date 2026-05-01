package com.example.project_4_event_planner;

import static androidx.core.content.ContextCompat.getSystemService;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class BroadcastReceiverPassword extends BroadcastReceiver {
    private static final String NOTIFICATION_CHANNEL_ID = "password_invalid_komos";
    private static final String NOTIFICATION_CHANNEL_NAME = "Komos Access Attempts Notifications";
    private static final String TITLE = "Warning: Multiple Sign-In Attempts";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Set ContentText
        String vMess = "@" + intent.getStringExtra("attemptedUsername")
                + " has multiple failed sign-in attempts!";

        // Create notification channel for Failed Password Attempts
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);

            // Configure notification & send
            Notification _fin = buildNotification(context, vMess);
            notificationManager.notify((int) intent.getLongExtra("notificationID", 0), _fin);
        }
    }

    private Notification buildNotification(Context context, String message) {
        Notification _notif = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.komosforge_icon)
                .setContentTitle(TITLE)
                .setContentText(message)
                .setAutoCancel(true)    // automatically removes the notification when the user taps it
                .build();
        return _notif;
    }
}