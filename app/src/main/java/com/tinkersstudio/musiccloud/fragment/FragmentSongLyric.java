package com.tinkersstudio.musiccloud.fragment;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.graphics.Bitmap;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.view.WindowManager;
import android.view.Display;
import android.graphics.Point;
import android.widget.TextView;

import com.google.firebase.crash.FirebaseCrash;
import com.tinkersstudio.musiccloud.R;
import com.tinkersstudio.musiccloud.activities.MainActivity;

import org.jmusixmatch.MusixMatch;
import org.jmusixmatch.MusixMatchException;
import org.jmusixmatch.entity.lyrics.Lyrics;
import org.jmusixmatch.entity.track.Track;
import org.jmusixmatch.entity.track.TrackData;

import java.util.ArrayList;

import com.tinkersstudio.musiccloud.model.Song;

/**
 * Created by Owner on 3/4/2017.
 * Should support both text and file text lyric
 */
public class FragmentSongLyric extends Fragment {

    String API_KEY = "f4337155f55d30c22e85a96f2dc674c8";
    MusixMatch musixMatch = new MusixMatch(API_KEY);
    String LOG_TAG = "FragmentSongLyric";
    Song currentSong;
    TextView lyricText, lyricTitle, lyricArtist;
    LinearLayout wholeScreen, lyricHeaderBar;
    String trackName = "", artistName = "";
    Bitmap bitmap;
    BitmapDrawable newBitmap;
    boolean hideQuit = false;
    int dominantColor, compColor, compColor2;

    public void setCurrentSong(Song currentSong) {this.currentSong = currentSong;}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_song_lyric, container, false);
        lyricText = (TextView) rootView.findViewById(R.id.lyric_text);
        lyricTitle = (TextView) rootView.findViewById(R.id.lyric_title);
        lyricArtist = (TextView) rootView.findViewById(R.id.lyric_artist);
        wholeScreen = (LinearLayout) rootView.findViewById((R.id.lyric_screen));
        lyricHeaderBar = (LinearLayout) rootView.findViewById(R.id.lyric_header_bar);

        trackName = currentSong.getTitle();
        artistName = currentSong.getArtist();

        if (trackName.equals("") || artistName.equals(""))
        {
            new retriveLyric().execute();
        }
        else
        {
            lyricText.setText("Can't find the song");
        }
        ArrayList<String> values = new ArrayList<String>();
        values.add(trackName);
        values.add(artistName);
        new retriveLyric().execute(values);

        return rootView;
    }

    /**
     * Disable the quit button
     */
    public void hideQuitButton(){
        hideQuit = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (hideQuit) {
            lyricHeaderBar.removeAllViews();
            lyricText.setTextColor(Color.WHITE);
        }
        else {

            // Get screen size to scale the cover art
            WindowManager wm = (WindowManager) ((MainActivity)getActivity()).getBaseContext().getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x/75;
            int height = size.y/75;
            // Get the Cover art, scale it down to blur it, then set background with the blurred image
            try {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(currentSong.getPath());
                byte[] art = retriever.getEmbeddedPicture();
                retriever.release();
                bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
            } catch (Exception exception) {
                Log.i(LOG_TAG, "NO COVER ART FOUND " + exception.getClass().getName());
                bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.cover_art_stock);
            } finally {
                dominantColor = FragmentMusicPlayer.getDominantColor(bitmap);
                compColor = FragmentMusicPlayer.getComplementaryColor(dominantColor);
                compColor2 = FragmentMusicPlayer.getComplementaryColor2(dominantColor);
                newBitmap = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, width, height, true));
            }

            wholeScreen.setBackgroundDrawable(newBitmap);

            lyricHeaderBar.setBackgroundColor(dominantColor);

            lyricTitle.setText(trackName);
            lyricTitle.setTextSize((float)22);
            lyricTitle.setTextColor(compColor2);
            lyricTitle.setSelected(true);

            lyricArtist.setText(artistName);
            lyricArtist.setTextSize((float)18);
            lyricArtist.setTextColor(compColor);
            lyricArtist.setSelected(true);

            lyricText.setTextColor(compColor2);
            lyricText.setShadowLayer((float)5.0, (float)2.0, (float)2.0, dominantColor);
            lyricText.setTextSize((float)15);
        }
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void writeToFile(String lyric)
    {

    }

    /**
     * This class handle uploading joke
     * The order of parameter Params, Progress and Result
     */
    public class retriveLyric extends AsyncTask<ArrayList<String>, Void, String> {
        String lyricsLyric = "";
        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p/>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param params The parameters of the task. Normally would be an array
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected String doInBackground(ArrayList<String>... params) {
            try {
                String trackName = params[0].get(0);
                String artistName = params[0].get(1);
                getLyric(trackName, artistName);
            }
            catch (Exception e) {
                FirebaseCrash.logcat(Log.ERROR, FragmentSongLyric.this.LOG_TAG, "Exception in user case");
                FirebaseCrash.report(e);
                Log.e(LOG_TAG, "Error");
            }
            return "Success";
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Log.d(FragmentSongLyric.this.LOG_TAG, "Start API called");
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //Log.d(FragmentSongLyric.this.LOG_TAG, "Completed retrieve lyric");
            lyricText.setText(lyricsLyric);
        }

        public void getLyric(String trackName, String artistName) {
            try{
                Track track = musixMatch.getMatchingTrack(trackName, artistName);
                TrackData data = track.getTrack();
                int trackID = data.getTrackId();
                Lyrics lyrics = musixMatch.getLyrics(trackID);
                this.lyricsLyric = lyrics.getLyricsBody();
            }
            catch (MusixMatchException e) {
                Log.i(LOG_TAG, "No lyrics found online");
            }
            catch (Exception e)
            {
                FirebaseCrash.report(e);
                Log.e(e.toString(), "Error in setting value");
            }
        }
    }


}
