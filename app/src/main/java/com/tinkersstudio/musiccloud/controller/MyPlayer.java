package com.tinkersstudio.musiccloud.controller;
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
import com.tinkersstudio.musiccloud.model.Song;

/**
 * Created by anhnguyen on 2/11/17.
 *
 * 4/21/2017 add method to get the times of current song playing
 *           modify getSong to also get the data path
 */

public class MyPlayer implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener,
                                 MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnErrorListener
{
    private static final String LOG_TAG = "MyPlayer";

    // the music service which whole an instance of this PLAYER
    private MusicService owner;

    // The media player
    private MediaPlayer player;

    // State of the the PLAYER
    private boolean isPaused;
    private boolean isShuffle;
    private boolean isRepeat;
    private int     currentSongPosition;
    private Song    currentSong;

    // Hole the currentPosition of the song before being pause
    private long    lastCurrentPosition;

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

    /**
     * Complete a song, auto proceed to the next song and play it
     * @param mediaPlayer
     */
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
    public void onSeekComplete(MediaPlayer var1) {
        //NOT IMPLEMENT
    }

    /**
     * Set the shuffle mode
     * @return the new state of shuffle mode
     */
    public boolean setShuffle(){
        isShuffle = !isShuffle;
        return isShuffle;
    }

    /**
     * Set the repeat mode
     * @return the new state of repeat mode
     */
    public boolean setRepeat() {
        isRepeat = !isRepeat;
        return isRepeat;
    }

    /**
     * This method get called when client the button play/pause invoked
     * This will either play the music or pause the music
     */
    public void play() {

        // Check to see if there is a valid song to play
        if (songList == null && getSongFromStorage() <= 0 || songList.size() == 0) {
            throw new NoSongToPlayException("There no such a song to play");
        }

        // First time playing music, set the index to the first song on the list
        if (currentSongPosition < 0 ) {
            currentSongPosition = 0;
        }

        // Pause player if it is playing, then return
        if (player.isPlaying()) {
            Log.i(LOG_TAG, "pause");
            pause();
            isPaused = true;
            return;
        }

        // PLAY music
        player.reset();

        isPaused = false;
        // get song to play
        currentSong =  songList.get(currentSongPosition);
        Log.i(LOG_TAG, "play " + currentSong.getTitle());
        // get song id
        long currSong = currentSong.getID();
        // set uri path
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);

        try{
            player.setDataSource(owner.getApplicationContext(), trackUri);
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        // The song is feeded into the player and ready to be played
        player.prepareAsync();
    }

    /**
     * Pause player
     */
    public void pause() {
        player.pause();
    }

    /**
     * Move the index to the prev song on the list
     * Play/Pause the prev song if player was playing/pausing
     * @param wasPlaying indicates if the player was playing before seeking to Prev song
     */
    public void seekPrev(boolean wasPlaying) {
        // Check for valid prev song
        if (songList == null && getSongFromStorage() <= 0 || songList.size() == 0) {
            throw new NoSongToPlayException("There no such a song to play");
        }

        // If the player is playing, pause it first before seeking to prev song
        if (player.isPlaying())
            pause();

        // MOVE index to prev song according to shuffle/repeat/regular mode

        //shuffle case
        if(isShuffle) {
            currentSongPosition = (int) (Math.random() * (songList.size() - 1));
            Log.e(LOG_TAG, "currentPosition by shuffle: " + currentSongPosition);
        }
        // Regular case
        else if (!isRepeat) {
            if (currentSongPosition == 0)
                currentSongPosition = songList.size() - 1;
            else
                currentSongPosition--;

            Log.e(LOG_TAG, "currentPosition by regular: " + currentSongPosition);
        }
        else
            Log.e(LOG_TAG, "currentPosition by repeat: " + currentSongPosition);

        // After moving the index to prev song, resume the player
        if(wasPlaying) {
            play();
            isPaused = false;
        } else {
            isPaused = true;
        }
    }

    /**
     * Move the index to the next song on the list
     * Play/Pause the next song if player was playing/pausing
     * @param wasPlaying indicated if the player was playing before seeking to Next song
     */
    public void seekNext(boolean wasPlaying) {
        // Check for valid next song
        if (songList == null && getSongFromStorage() <= 0 || songList.size() == 0) {
            throw new NoSongToPlayException("There no such a song to play");
        }
        // If the player is playing, pause it first before seeking to next song
        if (player.isPlaying())
            pause();


        // MOVE index to next song according to shuffle/repeat/regular mode
        //shuffle case
        if(isShuffle){
            currentSongPosition = (int)(Math.random() * (songList.size()-1));
            Log.e(LOG_TAG, "currentPosition by shuffle: " + currentSongPosition);
        }
            // Regular case
        else if (!isRepeat) {
            if (currentSongPosition == songList.size() - 1)
                currentSongPosition = 0;
            else
                currentSongPosition++;
            Log.e(LOG_TAG, "currentPosition by regular: " + currentSongPosition);
        }
        else
            Log.e(LOG_TAG, "currentPosition by repeat: " + currentSongPosition);

        // After moving the index to prev song, resume the player
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
                        songList.add(new Song(id, title, artist, albumID, path));
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

    /**
     * To play a specific song on the list
     * @param newPos
     */
    public void setCurrentSongPosition(int newPos) {
        this.currentSongPosition = newPos;
    }

    /**
     * get the current song playing to extract info
     * @return
     */
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

    public long getTotalDuration() {
        return this.player.getDuration();
    }
    public long getCurrentPosn() {
        return this.player.getCurrentPosition();
    }
}
