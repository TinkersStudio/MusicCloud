package controller;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
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
    private int     currentSongPosition = -1;
    private Song    currentSong;
    private long    currentPosition;
    private Random  rand;
    /* song list to play */
    private ArrayList<Song> songList;

    public MyPlayer(MusicService owner) {
        this.owner = owner;
    }


    public void initMusicPlayer(){}

    /**
     * Get the whole list of song. The player will search and play all song incase
     * playlist of song have not been set by user.
     */
    public int getSongFromStorage() {
        Log.i(LOG_TAG, "Find Music....");
        songList = new ArrayList<Song>();

        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DURATION,
                //MediaStore.Audio.Albums.ALBUM_ART
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


    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {}

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {}

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        mediaPlayer.reset();
        return false;
    }
    @Override
    public void onSeekComplete(MediaPlayer var1) {}

    public void playMusic() {}
    public void pauseMusic() {}
    public void playPrev() {}
    public void playNext() {}

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

}
