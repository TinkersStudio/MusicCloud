package com.tinkersstudio.musiccloud;

import android.*;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
//import io.swagger.client.api.DefaultApi;

import com.google.firebase.crash.FirebaseCrash;
import com.musixmatch.lyrics.MissingPluginException;
import com.musixmatch.lyrics.musiXmatchLyricsConnector;

import org.jmusixmatch.MusixMatch;
import org.jmusixmatch.MusixMatchException;
import org.jmusixmatch.entity.lyrics.Lyrics;
import org.jmusixmatch.entity.track.Track;
import org.jmusixmatch.entity.track.TrackData;

import java.util.ArrayList;

import me.zhengken.lyricview.LyricView;

/**
 * Created by Owner on 3/4/2017.
 * Should support both text and file text lyric
 */
public class FragmentSongLyric extends Fragment {

    String API_KEY = "f4337155f55d30c22e85a96f2dc674c8";
    MusixMatch musixMatch = new MusixMatch(API_KEY);
    String LOG_TAG = "FragmentSongLyric";
    //LyricView mLyricView;
    TextView lyricText;
    String trackName;
    String artistName;

    public FragmentSongLyric() {
        //require constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_song_lyric, container, false);
        //mLyricView = (LyricView)rootView.findViewById(R.id.custom_lyric_view);
        lyricText = (TextView) rootView.findViewById(R.id.lyric_text);
        /**
        if (trackName != null && artistName != null)
        {
            new retriveLyric().execute();
        }
        else
        {
            lyricText.setText("Can't find the song");
        }
        */
        final String trackName = "Don't stop the Party";
        final String artistName = "The Black Eyed Peas";
        ArrayList<String> values = new ArrayList<String>();
        values.add(trackName);
        values.add(artistName);
        new retriveLyric().execute(values);


        return rootView;

        //initialize button in here
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
            Log.d(FragmentSongLyric.this.LOG_TAG, "Start API called");
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(FragmentSongLyric.this.LOG_TAG, "Completed retrieve lyric");
            lyricText.setText(lyricsLyric);
            //FirebaseCrash.log("Failed to check permission");
        }

        public void getLyric(String trackName, String artistName) {
            try{
                Track track = musixMatch.getMatchingTrack(trackName, artistName);
                TrackData data = track.getTrack();
                int trackID = data.getTrackId();
                Lyrics lyrics = musixMatch.getLyrics(trackID);
                this.lyricsLyric = lyrics.getLyricsBody();
                //Log.i(LOG_TAG, lyricsLyric);
                //lyricText.setText(lyricsLyric);
            }
            catch (MusixMatchException e) {
                FirebaseCrash.logcat(Log.ERROR, LOG_TAG, "Can't get the song lyric");
                FirebaseCrash.report(e);
                e.printStackTrace();
            }
            catch (Exception e)
            {
                FirebaseCrash.report(e);
                Log.e(e.toString(), "Error in setting value");
            }
        }


    }


}
