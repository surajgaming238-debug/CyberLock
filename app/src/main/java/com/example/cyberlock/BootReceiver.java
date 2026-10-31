package com.example.cyberlock;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent i = new Intent(context, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ trick to launch from background
                NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                String channelId = "lockdown_channel";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel(channelId, "Lockdown", NotificationManager.IMPORTANCE_HIGH);
                    nm.createNotificationChannel(channel);
                }
                PendingIntent pi = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                Notification.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    builder = new Notification.Builder(context, channelId);
                } else {
                    builder = new Notification.Builder(context);
                }
                builder.setSmallIcon(android.R.drawable.ic_lock_lock)
                       .setContentTitle("Device Locked")
                       .setContentText("Boot detected. Lockdown active.")
                       .setFullScreenIntent(pi, true)
                       .setOngoing(true);
                nm.notify(1, builder.build());
            }
            
            // Fallback direct start
            try { context.startActivity(i); } catch (Exception e) {}
        }
    }
}
