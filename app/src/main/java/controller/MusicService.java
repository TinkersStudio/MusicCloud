package controller;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.util.Log;

/**
 * Created by Owner on 2/10/2017.
 */

public class MusicService extends Service {

    private static final String LOG_TAG = "MusicService";

    public static final int SERVICE_ID = 02112017;

    /* to bind with the MainActivity */
    private final IBinder musicBind = new MusicBinder();

    private MyPlayer player;

    private MyNotification notifBar;

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
            player.printSongList();
            player.setCurrentSongPosition(1);

            //FIXME Using the catch block
            // Put up the Notification bar and run the service on foreground
            try {
                notifBar = new MyNotification(this, MyFlag.PLAY,
                        player.getCurrentSong().getTitle(),
                        player.getCurrentSong().getArtist());
                startForeground(101, notifBar);
            }
            catch (Exception e)
            {
                Log.e(LOG_TAG, "Error in the onStartCommand. Suspend the service");
            }

        } else if (intent.getAction().equals(
                "com.truiton.foregroundservice.action.stopforeground")) {
            Log.i(LOG_TAG, "Received Stop Foreground Intent");
            stopForeground(true);
            stopSelf();
        }
        return START_NOT_STICKY;
    }
}
