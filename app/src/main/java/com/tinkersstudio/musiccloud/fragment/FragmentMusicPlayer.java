package com.tinkersstudio.musiccloud.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.crash.FirebaseCrash;
import com.tinkersstudio.musiccloud.controller.MusicService;
import com.tinkersstudio.musiccloud.controller.MyPlayer;
import com.tinkersstudio.musiccloud.util.MyFlag;
import com.tinkersstudio.musiccloud.util.NoSongToPlayException;
import es.dmoral.toasty.Toasty;
import info.abdolahi.CircularMusicProgressBar;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.widget.SeekBar;
import android.view.MotionEvent;
import com.tinkersstudio.musiccloud.R;
import com.tinkersstudio.musiccloud.util.TimeConverter;


/**
 * Created by anhnguyen on 2/6/17.
 *
 * 4/21 add Seekbar function
 */

public class FragmentMusicPlayer extends Fragment {
    private String LOG_TAG = "FragmentMusicPlayer";

    private Context context;

    // Widget elements
    private ImageButton favor, lyricsButton, infoButton;
    private TextView songTitle, artist, timePast, timeTotal;
    private CircularMusicProgressBar circularProgressBar;
    private SeekBar seekBar;
    private boolean isMovingSeekBar;

    // A handler to manage the Runnable which is used to update UI
    private static Handler mHandler = new Handler();

    //Service to control playback (provide by Main activity)
    private MusicService musicService;
    private MyPlayer myPlayer;

    // The album picture to extract color;
    private Bitmap bitmap;
    private int compColor = Color.TRANSPARENT;

    //com.tinkersstudio.musiccloud.view items
    private View rootView;

    //music player group
    private ImageButton repeatButton, playPrevButton, playButton, playNextButton, shuffleButton;

    /**
     * Default constructor
     */
    public FragmentMusicPlayer() {
        //require constructor
    }

    /**
     * Setter of Music Service
     *
     * @param musicService is the musicService on back ground
     */
    public void setMusicService(MusicService musicService) {
        this.musicService = musicService;
        this.myPlayer = (MyPlayer)musicService.getPlayer(MyFlag.OFFLINE_MUSIC_MODE);
    }

    /**
     * Create the view of this Fragment
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_music_player, container, false);
        context = getActivity().getBaseContext();

        //initialize all elements values
        initLayout();
        initAction();

        return rootView;

    }

    @Override
    public void onStart() {
        super.onStart();
        // Start the handler, which run the Runnable mUpdateTimeTask
        mHandler.postDelayed(mUpdateTimeTask, 1000);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Try to set the whole screen if there were some music is playing
        try {
            this.updateSongPlaying();
            this.setColor();
        } catch (Exception e) {
            Log.i(LOG_TAG, "onResume fail to get the current Song playing");
            // No need to report
        }
    }

    /**
     * A Runnable which run separately on the back ground to update UI of this fragment
     * It is scheduled to update the seekbar every 1 second,
     * and update the whole screen when a song is complete playing
     */
    private Runnable mUpdateTimeTask = new Runnable() {
        @Override
        public void run() {
            try {
                // Update seekbar only if the song playing
                if (!myPlayer.getIsPause()) {
                    // Displaying time completed playing
                    long currentDuration = myPlayer.getCurrentPosn();
                    timePast.setText("" + TimeConverter.milliSecondsToTimeString(currentDuration));

                    // Displaying Total Duration time
                    long totalDuration = myPlayer.getTotalDuration();
                    timeTotal.setText("" + TimeConverter.milliSecondsToTimeString(totalDuration));

                    // Updating progress bar
                    int progress = (int) (TimeConverter.currentDurationToPercentage(currentDuration, totalDuration));
                    seekBar.setProgress(progress);

                    // Finish a song, update whole screen
                    if (currentDuration < 1000) {
                        updateSongPlaying();
                        setColor();
                    }
                }
                // Running this thread after 1000 milliseconds
                mHandler.postDelayed(mUpdateTimeTask, 1000);
            } catch (Exception e) {
                //Exception thrown when Service haven't up yet
                e.printStackTrace();
                FirebaseCrash.report(e);
            }
        }
    };

    /**
     * Get the layout components of the page
     * Get all widget instances from layout
     */
    public void initLayout() {
        favor = (ImageButton) rootView.findViewById(R.id.mp_button_favorite);
        lyricsButton = (ImageButton) rootView.findViewById(R.id.mp_button_lyrics);
        infoButton = (ImageButton) rootView.findViewById(R.id.mp_button_info);
        repeatButton = (ImageButton) rootView.findViewById(R.id.mp_repeat);
        playPrevButton = (ImageButton) rootView.findViewById(R.id.mp_play_prev);
        playButton = (ImageButton) rootView.findViewById(R.id.mp_play);
        playNextButton = (ImageButton) rootView.findViewById(R.id.mp_play_next);
        shuffleButton = (ImageButton) rootView.findViewById(R.id.mp_shuffle);
        songTitle = (TextView) rootView.findViewById(R.id.mp_songBeingPlay);
        artist = (TextView) rootView.findViewById(R.id.mp_songBeingPlayArtist);
        circularProgressBar = (CircularMusicProgressBar) rootView.findViewById((R.id.mp_progress_bar));

        seekBar = (SeekBar) rootView.findViewById(R.id.mp_seekbar);
        timePast = (TextView) rootView.findViewById(R.id.mp_time_played);
        timeTotal = (TextView) rootView.findViewById(R.id.mp_time_total);
    }

    /**
     * Init all the button listeners
     */
    public void initAction() {

        favor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO Implement Favorite field in Song to keep a list of favorist Song
                favor.setColorFilter(Color.RED);
            }
        });

        // Open a new fragment when click on lyrics button
        lyricsButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                musicService.toggle(MyFlag.OFFLINE_MUSIC_MODE);
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    lyricsButton.setColorFilter(Color.RED);
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    lyricsButton.setColorFilter(compColor);
                    try {
                        // Try get song to make sure there is a current song playing
                        myPlayer.getCurrentSong();
                        FragmentManager fragmentManager = getFragmentManager();
                        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        FragmentSongLyric songLyric = new FragmentSongLyric();
                        songLyric.setCurrentSong(myPlayer.getCurrentSong());
                        fragmentTransaction.addToBackStack("FragmentMusicPlayer");
                        fragmentTransaction.hide(FragmentMusicPlayer.this);
                        fragmentTransaction.add(R.id.fragment_container, songLyric);
                        fragmentTransaction.commit();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toasty.info(context, "Current Song Empty", Toast.LENGTH_SHORT, true).show();
                        // No need to report
                    }
                    return true;
                }
                return false;
            }

        });

        // Open a new fragment when click on info button
        infoButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                musicService.toggle(MyFlag.OFFLINE_MUSIC_MODE);
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    infoButton.setColorFilter(Color.RED);
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {

                    Toasty.info(context, "One Moment, Retrieving Info", Toast.LENGTH_SHORT, true).show();
                    infoButton.setColorFilter(compColor);
                    try {
                        // Try get song to make sure there is a current song playing
                        myPlayer.getCurrentSong();
                        FragmentManager fragmentManager = getFragmentManager();
                        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        FragmentMusicInfo songInfo = new FragmentMusicInfo();
                        songInfo.setCurrentSong(myPlayer.getCurrentSong());
                        fragmentTransaction.addToBackStack("FragmentMusicPlayer");
                        fragmentTransaction.hide(FragmentMusicPlayer.this);
                        fragmentTransaction.add(R.id.fragment_container, songInfo);
                        fragmentTransaction.commit();
                    } catch (Exception e) {
                        Toasty.info(context, "Current Song Empty", Toast.LENGTH_SHORT, true).show();
                        // No need to report
                    }
                    return true;

                }
                return false;
            }
        });

        // Reponse to seekBar change when user drag the dot
        this.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isMovingSeekBar = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isMovingSeekBar = false;
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                musicService.toggle(MyFlag.OFFLINE_MUSIC_MODE);
                if (isMovingSeekBar) {
                    long totalDuration = myPlayer.getTotalDuration();
                    int seekToPosition = TimeConverter.percentageToCurrentDuration(seekBar.getProgress(), totalDuration);
                    // forward or backward to certain seconds
                    myPlayer.seekPosition(seekToPosition);
                }
            }
        });

        /*---- THESE BUTTON OnTouchListener can modify the Button while touching it---*/
        // Set repeat mode on player, also change the button view
        this.repeatButton.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                musicService.toggle(MyFlag.OFFLINE_MUSIC_MODE);
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    repeatButton.setColorFilter(Color.RED);
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    repeatButton.setImageResource(myPlayer.setRepeat() ?
                            R.drawable.ic_action_replay : R.drawable.ic_action_repeat);
                    repeatButton.setColorFilter(compColor);
                    return true;
                }
                return false;
            }
        });

        this.playPrevButton.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                musicService.toggle(MyFlag.OFFLINE_MUSIC_MODE);
                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    playPrevButton.setColorFilter(Color.RED);
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    // Move the prev song if there is a valid prev song
                    try {
                        // Pause player if the player was pausing before move to prev song
                        myPlayer.seekPrev(!myPlayer.getIsPause());
                        // update whole screen with new song info
                        updateSongPlaying();
                        setColor();
                    } catch (NoSongToPlayException e) {
                        Toasty.info(context, "No Song To Play", Toast.LENGTH_SHORT, true).show();
                    }
                    playPrevButton.setColorFilter(compColor);
                    return true;
                }
                return false;
            }
        });

        this.playButton.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                musicService.toggle(MyFlag.OFFLINE_MUSIC_MODE);
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    playButton.setColorFilter(Color.RED);
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {

                    try {
                        myPlayer.playCurrent();
                        // Need to update whole screen incase of the first time launching this fragment
                        // other cases, song info should stay the same after user click on play/pause
                        updateSongPlaying();
                        setColor();
                    } catch (NoSongToPlayException e) {
                        Toasty.info(context, "No Song To Play", Toast.LENGTH_SHORT, true).show();
                        // No need to report
                    }
                    playButton.setColorFilter(compColor);
                    return true;
                }
                return false;
            }
        });

        this.playNextButton.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                musicService.toggle(MyFlag.OFFLINE_MUSIC_MODE);
                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    playNextButton.setColorFilter(Color.RED);
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    // Move the prev song if there is a valid prev song
                    try {
                        // Pause player if the player was pausing before move to next song
                        myPlayer.seekNext(!myPlayer.getIsPause());
                        // update whole screen with new song info
                        updateSongPlaying();
                        setColor();
                    } catch (NoSongToPlayException e) {
                        Toasty.info(context, "No Song To Play", Toast.LENGTH_SHORT, true).show();
                        // No need to report
                    }
                    playNextButton.setColorFilter(compColor);
                    return true;
                }
                return false;
            }
        });

        // Set shuffle mode on player, also change the button view
        this.shuffleButton.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                musicService.toggle(MyFlag.OFFLINE_MUSIC_MODE);
                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    shuffleButton.setColorFilter(Color.RED);
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    shuffleButton.setImageResource(myPlayer.setShuffle() ?
                            R.drawable.ic_action_shuffle : R.drawable.ic_action_shuffle_disabled);
                    shuffleButton.setColorFilter(compColor);
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * Update the screen with the current playing song
     * elems to be updated: SONG TITLE, ARTIST, COVER ART
     */
    private void updateSongPlaying() {
        songTitle.setText(myPlayer.getFirstTitle());
        artist.setText(myPlayer.getSecondTitle());

        if (myPlayer.getIsPause())
            playButton.setImageResource(R.drawable.ic_action_play);
        else
            playButton.setImageResource(R.drawable.ic_action_pause);

        // Setting the album image
        {
            try {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(myPlayer.getCurrentSong().getPath());
                byte[] art = retriever.getEmbeddedPicture();
                bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
                retriever.release();
            } catch (Exception exception) {
                try {
                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cover_art_stock);
                } catch (java.lang.IllegalStateException e) {
                    Log.i(LOG_TAG, "This Fragment not shown");
                }
                // No need to report
            } finally {
                circularProgressBar.setImageBitmap(bitmap);
            }
        }

        if (myPlayer.isRepeat()) {
            repeatButton.setImageResource(R.drawable.ic_action_replay);
        }
        else {
            repeatButton.setImageResource(R.drawable.ic_action_repeat);
        }

        if (myPlayer.isShuffle()) {
            shuffleButton.setImageResource(R.drawable.ic_action_shuffle);
        }
        else {
            shuffleButton.setImageResource(R.drawable.ic_action_shuffle_disabled);
        }
    }

    /**
     * Update widget elems with color extract from the current COVER ART
     * elems to be updated: all BUTTON color
     * all TEXT color
     */
    public void setColor() {
        int color = getDominantColor(bitmap);
        compColor = getComplementaryColor(color);
        int compColor2 = getComplementaryColor2(color);
        circularProgressBar.setBorderColor(compColor);

        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.WHITE, color});

        rootView.setBackgroundDrawable(gd);

        //rootView.setBackgroundColor(color);
        repeatButton.setColorFilter(compColor);
        shuffleButton.setColorFilter(compColor);
        playNextButton.setColorFilter(compColor);
        playPrevButton.setColorFilter(compColor);
        playButton.setColorFilter(compColor);
        songTitle.setTextColor(compColor2);
        artist.setTextColor(compColor);
        infoButton.setColorFilter(compColor);
        lyricsButton.setColorFilter(compColor);
        favor.setColorFilter(compColor);
        timePast.setTextColor(compColor2);
        timeTotal.setTextColor(compColor2);
    }


    /**
     * extract the dominant color in first 200x200 area of the album cover
     * Get nealy correct dominant color of the bitmap
     * SLOW - O(n2) Algorithm
     */
    public static int getDominantColor(Bitmap bitmap) {
        int redBucket = 0, greenBucket = 0, blueBucket = 0, pixelCount = 0;
        int w = (bitmap.getWidth() > 200 ? 200 : bitmap.getWidth());
        int h = (bitmap.getHeight() > 200 ? 200 : bitmap.getHeight());
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int c = bitmap.getPixel(x, y);

                pixelCount++;
                redBucket += Color.red(c);
                greenBucket += Color.green(c);
                blueBucket += Color.blue(c);
            }
        }
        return Color.rgb(redBucket / pixelCount,
                greenBucket / pixelCount,
                blueBucket / pixelCount);
    }

    // Calculate the opposite color of a color
    public static int getComplementaryColor(int colorToInvert) {
        float[] hsv = new float[3];
        Color.RGBToHSV(Color.red(colorToInvert), Color.green(colorToInvert),
                Color.blue(colorToInvert), hsv);
        if (hsv[2] < 0.5) {
            hsv[2] = 0.7f;
        } else {
            hsv[2] = 0.3f;
        }
        hsv[1] = hsv[1] * 0.2f;
        return Color.HSVToColor(hsv);
    }

    // Calculate the best contrast color (either black or white) of a color
    public static int getComplementaryColor2(int colorToInvert) {
        int ave = (Color.red(colorToInvert)
                + Color.green(colorToInvert)
                + Color.blue(colorToInvert)) / 3;
        return ave >= Color.BLACK ? Color.WHITE : -1;
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        mHandler.removeCallbacks(mUpdateTimeTask);
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
