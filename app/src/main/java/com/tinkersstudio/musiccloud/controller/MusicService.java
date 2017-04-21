package com.tinkersstudio.musiccloud.controller;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.app.Notification;
import com.tinkersstudio.musiccloud.activities.MainActivity;
import com.tinkersstudio.musiccloud.R;

/**
 * Created by Owner on 2/10/2017.
 */

public class MusicService extends Service {

    private static final String LOG_TAG = "MusicService";

    public static final int SERVICE_ID = 02112017;

    /* to bind with the MainActivity */
    private final IBinder musicBind = new MusicBinder();

    private MyPlayer player;

    private Notification notifBar;
    NotificationManager notificationManager;
    private int notificaitonId = 10231;  // just a random number


    /**
     * Binder to bind this service with Activities
     */
    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    /**
     *
     * @param arg0
     * @return
     */
    @Override
    public IBinder onBind(Intent arg0) {
        Log.i(LOG_TAG,"onBind");
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent){
        Log.i(LOG_TAG,"onUnbind");
        return false;
    }

    @Override
    public void onCreate() {

        Log.i(LOG_TAG, "onCreate");
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        player.releasePlayer();
        Log.i(LOG_TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOG_TAG, "onStartCommand");
        if (intent.getAction() == null) {
            Log.i(LOG_TAG, "Received Start Foreground Intent ");
            // Getting Music files from Storage
            player = new MyPlayer(this);
            if (player.getSongFromStorage() == 0)
                setNotificationBar(MyFlag.PLAY, "No song to play", "------");
            setNotificationBar(MyFlag.PLAY, "song title", "artist");
            startForeground(notificaitonId, notifBar);

        }
        else if (intent.getAction().equals("ACTION.NEXT_ACTION")) {
            Log.i(LOG_TAG, "Received Intent : NEXT");
            try {
                if(player.getIsPause()) {
                    player.seekNext(false);
                    setNotificationBar(player.getIsPause() ? MyFlag.PLAY : MyFlag.PAUSE, player.getCurrentSong().getTitle(), player.getCurrentSong().getTitle());
                }
                else {
                    player.seekNext(true);
                    setNotificationBar(player.getIsPause() ? MyFlag.PLAY : MyFlag.PAUSE, player.getCurrentSong().getTitle(), player.getCurrentSong().getTitle());
                }
            } catch (NoSongToPlayException e) {
                setNotificationBar(MyFlag.PAUSE, "No song to play", "------");
            }
            finally {
                notificationManager.notify(notificaitonId, notifBar);
            }
        }
        else if (intent.getAction().equals("ACTION.PREV_ACTION")) {
            Log.i(LOG_TAG, "Received Intent : PREV");
            try {
                if(player.getIsPause()) {
                    player.seekPrev(false);
                    setNotificationBar(player.getIsPause() ? MyFlag.PLAY : MyFlag.PAUSE, player.getCurrentSong().getTitle(), player.getCurrentSong().getTitle());
                }
                else {
                    player.seekPrev(true);
                    setNotificationBar(player.getIsPause() ? MyFlag.PLAY : MyFlag.PAUSE, player.getCurrentSong().getTitle(), player.getCurrentSong().getTitle());
                }
            } catch (NoSongToPlayException e) {
                setNotificationBar(MyFlag.PAUSE, "No song to play", "------");
            }
            finally {
                notificationManager.notify(notificaitonId, notifBar);
            }
        }
        else if (intent.getAction().equals("ACTION.PLAY_ACTION")) {
            Log.i(LOG_TAG, "Received Intent : PLAY");
            try {
                player.play();
                setNotificationBar(player.getIsPause() ? MyFlag.PLAY : MyFlag.PAUSE,
                        player.getCurrentSong().getTitle(), player.getCurrentSong().getTitle());
            } catch (NoSongToPlayException e) {
                setNotificationBar(MyFlag.PAUSE, "No song to play", "------");
            } finally {
                notificationManager.notify(notificaitonId, notifBar);
            }
        }
        return START_NOT_STICKY;
    }

    /**
     * Rebuild the notification bar with new information (Not update the notif bar's view yet)
     * @param playState
     * @param title
     * @param artist
     */
    public void setNotificationBar(MyFlag playState, String title, String artist) {
        Log.i(LOG_TAG, "Set Notification Bar");

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction("ACTION.MAIN_ACTION");
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Intent previousIntent = new Intent(this, MusicService.class);
        previousIntent.setAction("ACTION.PREV_ACTION");
        PendingIntent ppreviousIntent = PendingIntent.getService(this, 0, previousIntent, 0);

        Intent playIntent = new Intent(this, MusicService.class);
        playIntent.setAction("ACTION.PLAY_ACTION");
        PendingIntent pplayIntent = PendingIntent.getService(this, 0, playIntent, 0);

        Intent nextIntent = new Intent(this, MusicService.class);
        nextIntent.setAction("ACTION.NEXT_ACTION");
        PendingIntent pnextIntent = PendingIntent.getService(this, 0, nextIntent, 0);

        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.launcher);
        notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notifBar = new NotificationCompat.Builder(this)
                .setContentTitle(title)
                .setTicker("Playing").setContentText(artist)
                .setSmallIcon(R.drawable.ic_headphones)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .setOngoing(false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDeleteIntent(createOnDismissedIntent(this, notificaitonId))
                .addAction(android.R.drawable.ic_media_previous, "Previous", ppreviousIntent)
                .addAction(playState == MyFlag.PLAY? android.R.drawable.ic_media_play : android.R.drawable.ic_media_pause,
                        "Play", pplayIntent)
                .addAction(android.R.drawable.ic_media_next, "Next", pnextIntent)
                .build();
    }

    /**
     * Rebuild and Update the Notification bar's view
     * @param playState
     * @param title
     * @param artist
     */
    public void updateNotifBar(MyFlag playState, String title, String artist) {
        setNotificationBar(playState, title, artist);
        notificationManager.notify(notificaitonId, notifBar);
    }

    public static class NotificationDismissedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(LOG_TAG, "on Receive");
            int notificationId = intent.getExtras().getInt("notificationId");

            if (notificationId == 10231) {
                context.stopService(new Intent(context, MusicService.class));
            }
        }
    }

    private PendingIntent createOnDismissedIntent(Context context, int notificationId) {
        Log.e(LOG_TAG, "createOnDismissedIntent");
        Intent intent = new Intent(context, NotificationDismissedReceiver.class);
        intent.putExtra("notificationId", notificationId);

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(context.getApplicationContext(),
                        notificationId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        return pendingIntent;
    }

    public MyPlayer getPlayer(){return player;}

}
