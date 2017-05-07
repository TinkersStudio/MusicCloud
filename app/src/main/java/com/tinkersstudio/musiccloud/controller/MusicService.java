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
import android.app.Notification;
import android.util.Log;

import com.tinkersstudio.musiccloud.activities.MainActivity;
import com.tinkersstudio.musiccloud.R;
import com.tinkersstudio.musiccloud.util.MyFlag;
import com.tinkersstudio.musiccloud.util.NoSongToPlayException;

/**
 * Created by Owner on 2/10/2017.
 *
 */

public class MusicService extends Service {

    private static final String LOG_TAG = "MusicService";

    // NOT USE
    public static final int SERVICE_ID = 02112017;

    /* to bind with the MainActivity */
    private final IBinder musicBind = new MusicBinder();

    /* the reference to the Player */
    private Player thePlayer = null;

    /* the Offline Media Player */
    private MyPlayer player = null;

    /* the Radio Player */
    private  MyRadio radio = null;

    /* toggle between music mode or radio mode, default OFFLINE_MUSIC_MODE */
    private MyFlag mode = MyFlag.OFFLINE_MUSIC_MODE;

    /* notification bar */
    private Notification notifBar;
    private NotificationManager notificationManager;
    private int notificaitonId = 10231;  // just a random number to give this notif an ID


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
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent){
        super.onUnbind(intent);
        return false;
    }

    @Override
    public void onDestroy() {
        player.releasePlayer();
        radio.releaseRadio();
        super.onDestroy();
    }

    public void releasePlayer(){
        player.releasePlayer();
        radio.releaseRadio();
    }
    /**
     * Toggle the Player between MyPlayer and MyRadio mode
     */
    public void toggle(MyFlag mode) {
        if (this.mode == mode) {
            return;
        }
        // Pause what ever is playing
        if(thePlayer != null & !thePlayer.getIsPause()) {
            thePlayer.pausePlayer();
        }
        //switch references of Player
        switch (mode) {
            case RADIO_MODE:
                if (radio == null) {
                    thePlayer = new MyRadio(this);
                }
                else {
                    thePlayer = radio;
                }
                break;
            case OFFLINE_MUSIC_MODE:
                if (player == null) {
                    thePlayer = new MyPlayer(this);
                }
                else {
                    thePlayer = player;
                }
                break;
            case ONLINE_MUSIC_MODE:
                //TODO switch to other Online mode
                break;
        }

        Log.i(LOG_TAG, "toogle " + this.mode + " to " + mode);
        this.mode = mode;

    }

    /**
     * This method will be call first right after Service is up
     * Start a Notification Bar on top of device represent this Service
     * When user click on a button on the notification bar, this method will get call with a
     *  specific action Intent. Those action Intent are used to control MyPlayer and notif bar
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // When service is just start, there is no action Intent received.
        // Initialize the Foreground service and Notif Bar
        if (intent.getAction() == null) {
            this.player = new MyPlayer(this);
            this.radio = new MyRadio(this);
            thePlayer = this.player; // default player is initialize with offline music mode

            if (thePlayer.getDataSource() == 0) {
                setNotificationBar(MyFlag.PLAY, "No song to play", "------");
            }
            else {
                setNotificationBar(MyFlag.PLAY, "Song Title", "Artist");
            }
            startForeground(notificaitonId, notifBar);

        }
        // Receive action NEXT, pass the action to MyPlayer, rebuild and update notif bar
        else if (intent.getAction().equals("ACTION.NEXT_ACTION")) {
            Log.i(LOG_TAG, "mode: " + mode + "player " + " wasPlaying" + thePlayer.getIsPause());
            // Pass action to MyPlayer, and rebuild notif bar
            try {
                thePlayer.seekNext(!thePlayer.getIsPause());
                /*switch (mode) {
                    case OFFLINE_MUSIC_MODE:
                        if (player.getIsPause()) {
                            player.seekNext(false);
                            setNotificationBar(player.getIsPause() ? MyFlag.PLAY : MyFlag.PAUSE,
                                    player.getCurrentSong().getTitle(),
                                    player.getCurrentSong().getArtist());
                        } else {
                            player.seekNext(true);
                            setNotificationBar(player.getIsPause() ? MyFlag.PLAY : MyFlag.PAUSE,
                                    player.getCurrentSong().getTitle(),
                                    player.getCurrentSong().getArtist());
                        }
                        break;
                    case RADIO_MODE:
                        radio.seekNext(radio.getIsPause());
                        break;
                }*/
            } catch (NoSongToPlayException e) {
                setNotificationBar(MyFlag.PAUSE, "No song to play", "------");
            }
            // Update notif bar with new info
            finally {
                notificationManager.notify(notificaitonId, notifBar);
            }
        }
        // Receive action PREV, pass the action to MyPlayer, rebuild and update notif bar
        else if (intent.getAction().equals("ACTION.PREV_ACTION")) {

            Log.i(LOG_TAG, "mode: " + mode + "player" + (thePlayer.getClass().isInstance(MyPlayer.class)? "MyPlayer" : "MyRadio"));
            // Pass action to MyPlayer, and rebuild notif bar
            try {
                thePlayer.seekPrev(!thePlayer.getIsPause());
                /*switch (mode) {
                    case OFFLINE_MUSIC_MODE:
                        if (player.getIsPause()) {
                            player.seekPrev(false);
                            setNotificationBar(player.getIsPause() ? MyFlag.PLAY : MyFlag.PAUSE,
                                    player.getCurrentSong().getTitle(),
                                    player.getCurrentSong().getArtist());
                        } else {
                            player.seekPrev(true);
                            setNotificationBar(player.getIsPause() ? MyFlag.PLAY : MyFlag.PAUSE,
                                    player.getCurrentSong().getTitle(),
                                    player.getCurrentSong().getArtist());
                        }
                        break;
                    case RADIO_MODE:
                        radio.seekPrev(radio.getIsPause());
                        break;
                }*/
            } catch (NoSongToPlayException e) {
                setNotificationBar(MyFlag.PAUSE, "No song to play", "------");
            }
            // Update notif bar with new info
            finally {
                notificationManager.notify(notificaitonId, notifBar);
            }
        }
        // Receive action PLAY/PAUSE, pass the action to MyPlayer, rebuild and update notif bar
        else if (intent.getAction().equals("ACTION.PLAY_ACTION")) {

            Log.i(LOG_TAG, "mode: " + mode + "player" + (thePlayer.getClass().isInstance(MyPlayer.class)? "MyPlayer" : "MyRadio"));
            // Pass action to MyPlayer, and rebuild notif bar
            try {
                thePlayer.playCurrent();
                /*
                switch (mode) {
                    case OFFLINE_MUSIC_MODE:
                        player.playCurrent();
                        setNotificationBar(player.getIsPause() ? MyFlag.PLAY : MyFlag.PAUSE,
                                player.getCurrentSong().getTitle(), player.getCurrentSong().getTitle());
                        break;
                    case RADIO_MODE:
                        radio.playCurrent();
                        break;
                }*/
            } catch (NoSongToPlayException e) {
                setNotificationBar(MyFlag.PAUSE, "No song to play", "------");
            }
            // Update notif bar with new info
            finally {
                notificationManager.notify(notificaitonId, notifBar);
            }
        }
        return START_NOT_STICKY;
    }

    /**
     * Rebuild the notification bar with new information (The change is not shown on the view yet)
     * @param playState
     * @param title
     * @param artist
     */
    public void setNotificationBar(MyFlag playState, String title, String artist) {
        // Build Main element on the notif bar
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction("ACTION.MAIN_ACTION");
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        // Build Play/Pause action button
        Intent playIntent = new Intent(this, MusicService.class);
        playIntent.setAction("ACTION.PLAY_ACTION");
        PendingIntent pplayIntent = PendingIntent.getService(this, 0, playIntent, 0);

        // Build Next action button
        Intent nextIntent = new Intent(this, MusicService.class);
        nextIntent.setAction("ACTION.NEXT_ACTION");
        PendingIntent pnextIntent = PendingIntent.getService(this, 0, nextIntent, 0);

        // Build Prev action button
        Intent previousIntent = new Intent(this, MusicService.class);
        previousIntent.setAction("ACTION.PREV_ACTION");
        PendingIntent ppreviousIntent = PendingIntent.getService(this, 0, previousIntent, 0);


        // add all button to the Main element of the notif bar
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
                .addAction(android.R.drawable.ic_media_previous,"Prev", ppreviousIntent)
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
        setNotificationBar(playState, title, artist);   // Rebuild notif bar
        notificationManager.notify(notificaitonId, notifBar); // Show the notif bar
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

    /**
     *  Access to the Player
     * @return the player
     */
    public Player getPlayer(MyFlag mode){
        return (mode == MyFlag.OFFLINE_MUSIC_MODE? player : radio);
    }

    public MyFlag getMode() {
        return mode;
    }

    public void setMode(MyFlag mode) {
        thePlayer = (mode == MyFlag.OFFLINE_MUSIC_MODE? player :radio);
        this.mode = mode;
    }
}
