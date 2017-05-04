package com.tinkersstudio.musiccloud.controller;

import com.tinkersstudio.musiccloud.activities.MainActivity;
import com.tinkersstudio.musiccloud.util.MyFlag;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

/**
 * Created by anhnguyen on 2/11/17.
 *
 * NOT USE
 */

public class MyNotification extends Notification {


    protected static final int SERVICE_ID = 02112017;

    private MusicService owner;

    private Notification notification;

    public MyNotification(NotificationManager notificationManager, MusicService owner, MyFlag playState, String title, String artist){
        this.owner = owner;
        Intent notificationIntent = new Intent(owner, MainActivity.class);
        notificationIntent.setAction("com.truiton.foregroundservice.action.main");
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(owner, 0, notificationIntent, 0);

        Intent previousIntent = new Intent(owner, MainActivity.class);
        previousIntent.setAction("com.truiton.foregroundservice.action.prev");
        PendingIntent ppreviousIntent = PendingIntent.getService(owner, 0, previousIntent, 0);

        Intent playIntent = new Intent(owner, MainActivity.class);
        playIntent.setAction("com.truiton.foregroundservice.action.play");
        PendingIntent pplayIntent = PendingIntent.getService(owner, 0, playIntent, 0);

        Intent nextIntent = new Intent(owner, MainActivity.class);
        nextIntent.setAction("com.truiton.foregroundservice.action.next");
        PendingIntent pnextIntent = PendingIntent.getService(owner, 0, nextIntent, 0);

        Bitmap icon = BitmapFactory.decodeResource(owner.getResources(), android.R.drawable.ic_media_play);
        notificationManager = (NotificationManager) owner.getSystemService(owner.getBaseContext().NOTIFICATION_SERVICE);

        notification = new NotificationCompat.Builder(owner)
                .setContentTitle(title)
                .setTicker("Playing").setContentText(artist)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .setOngoing(false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDeleteIntent(createOnDismissedIntent(owner, SERVICE_ID))
                .addAction(android.R.drawable.ic_media_previous, "Previous", ppreviousIntent)
                .addAction(android.R.drawable.ic_media_play, "Play", pplayIntent)
                .addAction(android.R.drawable.ic_media_next, "Next", pnextIntent)
                .build();
        notificationManager.notify(SERVICE_ID, notification);
    }

    public void setNewPlayState(MyFlag playState){
    }

    public MyFlag getPlayState(){return null;}

    public static class NotificationDismissedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int notificationId = intent.getExtras().getInt("com.my.app.SERVICE_ID");
            if (notificationId == SERVICE_ID) {
                context.stopService(new Intent(context, MusicService.class));
            }
        }
    }
    private PendingIntent createOnDismissedIntent(Context context, int notificationId) {
        Intent intent = new Intent(context, NotificationDismissedReceiver.class);
        intent.putExtra("com.my.app.notificationId", notificationId);

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(context.getApplicationContext(),
                        notificationId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        return pendingIntent;
    }

}
