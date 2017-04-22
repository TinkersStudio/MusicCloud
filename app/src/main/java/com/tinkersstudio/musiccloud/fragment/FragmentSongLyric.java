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
import android.view.MotionEvent;
import android.view.View;
import android.graphics.Bitmap;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.view.WindowManager;
import android.view.Display;
import 	android.graphics.Point;
import android.widget.TextView;
//import io.swagger.client.api.DefaultApi;

import com.google.firebase.crash.FirebaseCrash;
import com.tinkersstudio.musiccloud.R;
import com.tinkersstudio.musiccloud.activities.MainActivity;

import org.jmusixmatch.MusixMatch;
import org.jmusixmatch.MusixMatchException;
import org.jmusixmatch.entity.lyrics.Lyrics;
import org.jmusixmatch.entity.track.Track;
import org.jmusixmatch.entity.track.TrackData;

import java.util.ArrayList;

import com.tinkersstudio.musiccloud.controller.MusicService;
import com.tinkersstudio.musiccloud.controller.NoSongToPlayException;

import es.dmoral.toasty.Toasty;

/**
 * Created by Owner on 3/4/2017.
 * Should support both text and file text lyric
 */
public class FragmentSongLyric extends Fragment {

    String API_KEY = "f4337155f55d30c22e85a96f2dc674c8";
    MusixMatch musixMatch = new MusixMatch(API_KEY);
    String LOG_TAG = "FragmentSongLyric";
    MusicService newService = ((MainActivity)getActivity()).myService;
    //LyricView mLyricView;
    TextView lyricText;
    TextView lyricTitle;
    TextView lyricArtist;
    LinearLayout wholeScreen;
    RelativeLayout lyricHeaderBar;
    ImageButton exitButton;
    String trackName = "";
    String artistName = "";
    Bitmap bitmap;
    int dominantColor, compColor, compColor2;
    public FragmentSongLyric() {
        //require constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(com.tinkersstudio.musiccloud.R.layout.fragment_song_lyric, container, false);
        //mLyricView = (LyricView)rootView.findViewById(R.id.custom_lyric_view);
        lyricText = (TextView) rootView.findViewById(R.id.lyric_text);
        lyricTitle = (TextView) rootView.findViewById(R.id.lyric_title);
        lyricArtist = (TextView) rootView.findViewById(R.id.lyric_artist);
        exitButton = (ImageButton) rootView.findViewById(R.id.lyric_quit);
        wholeScreen = (LinearLayout) rootView.findViewById((R.id.lyric_screen));
        lyricHeaderBar = (RelativeLayout) rootView.findViewById(R.id.lyric_header_bar);

        trackName = newService.getPlayer().getCurrentSong().getTitle();
        artistName = newService.getPlayer().getCurrentSong().getArtist();

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
            retriever.setDataSource(newService.getPlayer().getCurrentSong().getPath());
            byte[] art = retriever.getEmbeddedPicture();
            bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
        } catch (Exception exception) {
            Log.e(LOG_TAG, "NO COVER ART FOUND " + exception.getClass().getName());
            bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.cover_art_stock);
        } finally {
            dominantColor = FragmentMusicPlayer.getDominantColor(bitmap);
            compColor = FragmentMusicPlayer.getComplementaryColor(dominantColor);
            compColor2 = FragmentMusicPlayer.getComplementaryColor2(dominantColor);
            BitmapDrawable newBitmap = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, width, height, true));
            wholeScreen.setBackgroundDrawable(newBitmap);
        }


        lyricHeaderBar.setBackgroundColor(dominantColor);

        exitButton.setColorFilter(compColor2); // TODO: Don't know why unable to set Color
        // set listener to quit button
        exitButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    exitButton.setColorFilter(Color.RED);
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    getActivity().getSupportFragmentManager().popBackStack();
                    exitButton.setColorFilter(compColor2);
                    return true;
                }
                return false;
            }
        });

        lyricTitle.setText(trackName);
        lyricTitle.setTextSize((float)20);
        lyricTitle.setTextColor(compColor2);

        lyricArtist.setText(artistName);
        lyricArtist.setTextSize((float)16);
        lyricArtist.setTextColor(compColor);

        lyricText.setTextColor(compColor2);
        lyricText.setShadowLayer((float)5.0, (float)2.0, (float)2.0, dominantColor);
        lyricText.setTextSize((float)15);

        return rootView;
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
