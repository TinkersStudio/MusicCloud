package controller;

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

import com.tinkersstudio.ui.MainActivity;

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
    private int notificaitonId = 10231;

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
            player.getSongFromStorage();
            setNotificationBar(MyFlag.PLAY, "song title", "artist");
            startForeground(101, notifBar);
        }
        else if (intent.getAction().equals("ACTION.NEXT_ACTION")) {
            Log.i(LOG_TAG, "Received Intent : NEXT");
            player.playNext();
            setNotificationBar(MyFlag.PLAY, player.getCurrentSong().getTitle(), player.getCurrentSong().getTitle());
            notificationManager.notify(notificaitonId, notifBar);
        }
        else if (intent.getAction().equals("ACTION.PREV_ACTION")) {
            Log.i(LOG_TAG, "Received Intent : PREV");
            player.playPrev();
            setNotificationBar(MyFlag.PLAY, player.getCurrentSong().getTitle(), player.getCurrentSong().getTitle());
            notificationManager.notify(notificaitonId, notifBar);
        }
        else if (intent.getAction().equals("ACTION.PLAY_ACTION")) {
            Log.i(LOG_TAG, "Received Intent : PLAY");
            player.play();
            setNotificationBar(player.getIsPause()? MyFlag.PAUSE: MyFlag.PLAY,
                    player.getCurrentSong().getTitle(), player.getCurrentSong().getTitle());
            notificationManager.notify(notificaitonId, notifBar);
        }
        return START_NOT_STICKY;
    }

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

        Bitmap icon = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_media_play);
        notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notifBar = new NotificationCompat.Builder(this)
                .setContentTitle(title)
                .setTicker("Playing").setContentText(artist)
                .setSmallIcon(android.R.drawable.ic_media_pause)
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


    public static class NotificationDismissedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int notificationId = intent.getExtras().getInt("notificationId");

            if (notificationId == 10231) {
                context.stopService(new Intent(context, MusicService.class));
            }
        }
    }

    private PendingIntent createOnDismissedIntent(Context context, int notificationId) {
        Intent intent = new Intent(context, NotificationDismissedReceiver.class);
        intent.putExtra("notificationId", notificationId);

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(context.getApplicationContext(),
                        notificationId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        return pendingIntent;
    }
}
