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

import com.google.firebase.crash.FirebaseCrash;
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
    private long    lastCurrentPosition = 0;

    /* song list to play */
    private ArrayList<Song> songList;

    /**
     * Constructing a new Media Player which belong to a MusicService
     * @param owner the ownner of this Player
     */
    public MyPlayer(MusicService owner) {
        this.owner = owner;
        player = new MediaPlayer();
        this.initializePlayer();
    }

    public void initializePlayer() {
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
        // Turning on the media player.
        // This does not mean it plays the song yet,
        player.start();

        if (lastCurrentPosition > 0) {
            player.seekTo((int)lastCurrentPosition);
            lastCurrentPosition = 0;
        }
    }

    /**
     * Complete a song, auto proceed to the next song and play it
     * @param mediaPlayer
     */
    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
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

    public boolean isRepeat() {
        return isRepeat;
    }

    public boolean isShuffle() {
        return isShuffle;
    }

    /**
     * Play the music from a specific index
     * @param index
     */
    public void playAtIndex(int index){
        setCurrentSongPosition(index);
        this.pause();
        this.play();
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
            setCurrentSongPosition(0);
        }

        // Pause player if it is playing, then return
        if (player.isPlaying()) {
            lastCurrentPosition = player.getCurrentPosition();
            pause();
            isPaused = true;
            owner.updateNotifBar(MyFlag.PLAY, currentSong.getTitle(), currentSong.getArtist());
            return;
        }

        // PLAY music
        player.reset();
        isPaused = false;
        // get song to play
        currentSong =  songList.get(currentSongPosition);
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
            FirebaseCrash.report(e);
            return;
        }
        // The song is feeded into the player and ready to be played
        player.prepareAsync();
        //TODO: Update the database
        owner.updateNotifBar(MyFlag.PAUSE, currentSong.getTitle(), currentSong.getArtist());
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

        lastCurrentPosition = 0;

        // If the player is playing, pause it first before seeking to prev song
        if (player.isPlaying())
            pause();

        // MOVE index to prev song according to shuffle/repeat/regular mode

        //shuffle case
        if(isShuffle) {
            setCurrentSongPosition( (int) (Math.random() * (songList.size() - 1)));
        }
        // Regular case
        else if (!isRepeat) {
            if (currentSongPosition == 0)
                setCurrentSongPosition( songList.size() - 1);
            else
                setCurrentSongPosition(currentSongPosition -1);
        }

        currentSong =  songList.get(currentSongPosition);

        // After moving the index to prev song, resume the player, update the notif bar
        if(wasPlaying) {
            play();
            isPaused = false;
            owner.updateNotifBar(MyFlag.PAUSE, currentSong.getTitle(), currentSong.getArtist());
        } else {
            isPaused = true;
            owner.updateNotifBar(MyFlag.PLAY, currentSong.getTitle(), currentSong.getArtist());
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

        lastCurrentPosition = 0;

        // If the player is playing, pause it first before seeking to next song
        if (player.isPlaying())
            pause();

        // MOVE index to next song according to shuffle/repeat/regular mode
        //shuffle case
        if(isShuffle){
            setCurrentSongPosition( (int)(Math.random() * (songList.size()-1)));
        }
            // Regular case
        else if (!isRepeat) {
            if (currentSongPosition == songList.size() - 1)
                setCurrentSongPosition( 0);
            else
                setCurrentSongPosition(currentSongPosition + 1);
        }

        currentSong =  songList.get(currentSongPosition);

        // After moving the index to prev song, resume the player
        if(wasPlaying) {
            play();
            isPaused = false;
            owner.updateNotifBar(MyFlag.PAUSE, currentSong.getTitle(), currentSong.getArtist());
        } else {
            isPaused = true;
            owner.updateNotifBar(MyFlag.PLAY, currentSong.getTitle(), currentSong.getArtist());
        }
    }

    /**
     * Get the whole list of song. The player will search and play all song in case
     * playlist of song have not been set by user.
     */
    public int getSongFromStorage() {

        if (songList == null) {
            songList = new ArrayList<Song>();
        }
        else {
            songList.clear();
        }

        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DURATION,
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
            FirebaseCrash.report(e);
        } finally{
            if( cursor != null){
                cursor.close();
            }
        }
        return songList.size();
    }

    /**
     * To set new song position
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

    public ArrayList<Song> getSongList(){return songList;}
    public long getTotalDuration() {
        return this.player.getDuration();
    }
    public int getCurrentSongPosition() {return this.currentSongPosition;}
    public long getCurrentPosn() {
        return this.player.getCurrentPosition();
    }
    public boolean seekPosition(int seekTo){
        this.player.seekTo(seekTo);
        return true;
    }
    public int getAudioSessionId(){return player.getAudioSessionId();}
    public void setVolume(float left, float right) {
        player.setVolume(left,right);
    }
}
