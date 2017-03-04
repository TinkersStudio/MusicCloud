package controller;
import android.content.ContentUris;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;
import java.util.ArrayList;
import java.util.Random;
import model.Song;

/**
 * Created by anhnguyen on 2/11/17.
 */

public class MyPlayer implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener,
                                 MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnErrorListener
{

    private static final String LOG_TAG = "MyPlayer";

    private MusicService owner;
    private MediaPlayer player;
    private boolean isPaused;
    private boolean isShuffle;
    private boolean isRepeat;
    private int     currentSongPosition;
    private Song    currentSong;
    private long    currentPosition;
    private Random  rand;
    /* song list to play */
    private ArrayList<Song> songList;

    /**
     * Constructing a new Media Player which belong to a MusicService
     * @param owner the ownner of this Player
     */
    public MyPlayer(MusicService owner) {
        Log.i(LOG_TAG, "create MusicPlayer");

        this.owner = owner;
        player = new MediaPlayer();
        this.initializePlayer();
    }
    public void initializePlayer() {

        Log.i(LOG_TAG, "initMusicPlayer");
        isPaused = true;
        isRepeat = false;
        isShuffle = false;
        currentSongPosition = -1;   // Player have not load any songs yet.
        currentPosition = (long)0.0;
        rand = new Random();

        player.setWakeMode(owner.getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {

        Log.i(LOG_TAG, "onPrepared");

        // Turning on the media player.
        // This does not mean it plays the song yet,
        player.start();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        Log.i(LOG_TAG, "onCompletion");
        seekNext(true);
        play();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        mediaPlayer.reset();
        return false;
    }
    @Override
    public void onSeekComplete(MediaPlayer var1) {}

    public void play() {

        if (songList == null && getSongFromStorage() <= 0) {
            throw new NoSongToPlayException("There no such a song to play");
        }
        if (currentSongPosition < 0 ) {
            currentSongPosition = 0;
        }

        if (player.isPlaying()) {

            Log.i(LOG_TAG, "pause");
            currentPosition = player.getCurrentPosition();
            pause();
            isPaused = true;
            return;
        }

        player.reset();
        currentSong =  songList.get(currentSongPosition);
        isPaused = false;

        Log.i(LOG_TAG, "play " + currentSong.getTitle());
        //get id
        long currSong = currentSong.getID();
        //set uri
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);

        try{
            player.setDataSource(owner.getApplicationContext(), trackUri);
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        player.prepareAsync();
    }

    public void pause() {
        player.pause();
    }
    public void seekPrev(boolean wasPlaying) {
        if (player.isPlaying())
            pause();
        if (currentSongPosition == 0)
            currentSongPosition = songList.size() - 1;
        else
            currentSongPosition--;
        if(wasPlaying) {
            play();
            isPaused = false;
        } else {
            isPaused = true;
        }
    }
    public void seekNext(boolean wasPlaying) {
        if (player.isPlaying())
            pause();
        if (currentSongPosition == songList.size() -1)
            currentSongPosition = 0;
        else
            currentSongPosition++;
        if(wasPlaying) {
            play();
            isPaused = false;
        } else {
            isPaused = true;
        }
    }




    /**
     * Get the whole list of song. The player will search and play all song in case
     * playlist of song have not been set by user.
     */
    public int getSongFromStorage() {
        Log.i(LOG_TAG, "Find Music....");

        if (songList == null) {
            songList = new ArrayList<Song>();
        }
        else
            songList.clear();

        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DURATION,
                //MediaStore.Audio.Media.
        };
        final String sortOrder = MediaStore.Audio.AudioColumns.TITLE + " COLLATE LOCALIZED ASC";

        Cursor cursor = null;
        try {
            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            cursor = owner.getContentResolver().query(uri, projection, selection, null, sortOrder);

            if( cursor != null){
                cursor.moveToFirst();

                while( !cursor.isAfterLast() ){
                    String title = cursor.getString(0);
                    String artist = cursor.getString(1);
                    String path = cursor.getString(2);
                    String album = cursor.getString(3);
                    Long albumID = cursor.getLong(4);
                    Long id = cursor.getLong(5);
                    String songDuration = cursor.getString(6);
                    //String albumArt = cursor.getString(7);
                    cursor.moveToNext();
                    if(path != null && path.endsWith(".mp3")) {
                        songList.add(new Song(id, title, artist, albumID));
                    }
                    Log.i(LOG_TAG, "Getting a song: " + title + "| by " + artist + "(" + songDuration
                            + "), albumID " + albumID + " : " + album);
                }
            }
        } catch (Exception e) {
            Log.e("TAG", e.toString());
            e.printStackTrace();
        } finally{
            if( cursor != null){
                cursor.close();
            }
        }
        return songList.size();
    }

    public void printSongList() {
        Log.i(LOG_TAG,"Song List on MyPLAYER");
        for (Song aSong: songList) {
            Log.i(LOG_TAG, aSong.getTitle() + " - " + aSong.getArtist());
        }
    }

    public void setCurrentSongPosition(int newPos) {
        this.currentSongPosition = newPos;
    }

    public Song getCurrentSong() {
        if (songList != null
                && currentSongPosition != -1
                && songList.size() > currentSongPosition)
            return songList.get(currentSongPosition);
        else
            throw new NoSongToPlayException("No Song Found");
    }

    public boolean getIsPause(){
        return isPaused;
    }

    public void releasePlayer(){
        this.player.stop();
        this.player.release();
    }
}
