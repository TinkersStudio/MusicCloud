package com.tinkersstudio.musiccloud.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
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
import com.tinkersstudio.musiccloud.controller.MusicService;
import com.tinkersstudio.musiccloud.controller.NoSongToPlayException;
import es.dmoral.toasty.Toasty;
import info.abdolahi.CircularMusicProgressBar;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.widget.SeekBar;
import android.view.MotionEvent;
import com.tinkersstudio.musiccloud.R;
import com.tinkersstudio.musiccloud.controller.TimeConverter;


/**
 * Created by anhnguyen on 2/6/17.
 *
 * 4/21 add Seekbar function
 */

public class FragmentMusicPlayer extends Fragment {
    String LOG_TAG = "FragmentMusicPlayer";

    Context context;

    // Widget elements
    ImageButton favor, lyricsButton, infoButton;
    static TextView songTitle, artist, timePast, timeTotal;
    CircularMusicProgressBar circularProgressBar;
    static SeekBar seekBar;
    boolean isMovingSeekBar;

    // A handler to manage the Runnable which is used to update UI
    Handler mHandler = new Handler();

    //Service to control playback (provide by Main activity)
    static MusicService musicService;

    // The album picture to extract color;
    static Bitmap bitmap;
    int compColor = Color.TRANSPARENT;

    //com.tinkersstudio.musiccloud.view items
    static View rootView;

    //music player group
    ImageButton repeatButton, playPrevButton, playButton, playNextButton, shuffleButton;

    /**
     * Default constructor
     */
    public FragmentMusicPlayer() {
        //require constructor
    }

    /**
     * Setter of Music Service
     * @param musicService is the musicService on back ground
     */
    public void setMusicService(MusicService musicService) {
        this.musicService = musicService;
    }

    /**
     * Create the view of this Fragment
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
        Log.e(LOG_TAG, "onStart");
        super.onStart();

        // Start the handler, which run the Runnable mUpdateTimeTask
        mHandler.postDelayed(mUpdateTimeTask, 1000);
    }

    @Override
    public void onResume() {
        Log.e(LOG_TAG, "onResume");
        super.onResume();

        // Try to set the whole screen if there were some music is playing
        try {
            this.setColor();
            this.updateSongPlaying();
        }
        catch (Exception e) {
            Log.i(LOG_TAG, "onResume fail to get the current Song playing");
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
                if(!musicService.getPlayer().getIsPause()) {
                    // Displaying time completed playing
                    long currentDuration = musicService.getPlayer().getCurrentPosn();
                    timePast.setText("" + TimeConverter.milliSecondsToTimeString(currentDuration));

                    // Displaying Total Duration time
                    long totalDuration = musicService.getPlayer().getTotalDuration();
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
            }
            catch (Exception e) {
                //Exception thrown when Service haven't up yet
                e.printStackTrace();
            }
        }
    };

    /**
     * Get the layout components of the page
     * Get all widget instances from layout
     */
    public void initLayout()
    {
        favor = (ImageButton) rootView.findViewById(R.id.mp_button_favorite);
        lyricsButton = (ImageButton) rootView.findViewById(R.id.mp_button_lyrics);
        infoButton = (ImageButton) rootView.findViewById(R.id.mp_button_info);
        repeatButton = (ImageButton)rootView.findViewById(R.id.mp_repeat);
        playPrevButton = (ImageButton)rootView.findViewById(R.id.mp_play_prev);
        playButton = (ImageButton)rootView.findViewById(R.id.mp_play);
        playNextButton =(ImageButton) rootView.findViewById(R.id.mp_play_next);
        shuffleButton = (ImageButton) rootView.findViewById(R.id.mp_shuffle);
        songTitle = (TextView)rootView.findViewById(R.id.mp_songBeingPlay);
        artist = (TextView)rootView.findViewById(R.id.mp_songBeingPlayArtist);
        circularProgressBar = (CircularMusicProgressBar)rootView.findViewById((R.id.mp_progress_bar));

        seekBar = (SeekBar)rootView.findViewById(R.id.mp_seekbar);
        timePast = (TextView)rootView.findViewById(R.id.mp_time_played);
        timeTotal = (TextView)rootView.findViewById(R.id.mp_time_total);
    }

    /**
     * Init all the button listeners
     */
    public void initAction(){

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
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    lyricsButton.setColorFilter(Color.RED);
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    lyricsButton.setColorFilter(compColor);
                    try {
                        // Try get song to make sure there is a current song playing
                        musicService.getPlayer().getCurrentSong();
                        FragmentManager fragmentManager = getFragmentManager();
                        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        FragmentSongLyric songLyric = new FragmentSongLyric();
                        fragmentTransaction.addToBackStack("FragmentMusicPlayer");
                        fragmentTransaction.hide(FragmentMusicPlayer.this);
                        fragmentTransaction.add(R.id.fragment_container, songLyric);
                        fragmentTransaction.commit();
                    } catch (Exception e) {
                        Toasty.info(context, "Current Song Empty", Toast.LENGTH_SHORT, true).show();
                    }
                    return true;
                }
                return false;
            }

        });

        // Open a new fragment when click on info button
        infoButton.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    infoButton.setColorFilter(Color.RED);
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    infoButton.setColorFilter(compColor);
                    try {
                        // Try get song to make sure there is a current song playing
                        musicService.getPlayer().getCurrentSong();
                        FragmentManager fragmentManager = getFragmentManager();
                        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        FragmentMusicInfo songLyric = new FragmentMusicInfo();
                        fragmentTransaction.addToBackStack("FragmentMusicPlayer");
                        fragmentTransaction.hide(FragmentMusicPlayer.this);
                        fragmentTransaction.add(R.id.fragment_container, songLyric);
                        fragmentTransaction.commit();
                    } catch (Exception e) {
                        Toasty.info(context, "Current Song Empty", Toast.LENGTH_SHORT, true).show();
                    }
                    return true;

                }
                return false;
            }
        });

        // Reponse to seekBar change when user drag the dot
        this.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /**
             * Start record the change on seek bar
             * @param seekBar
             */
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isMovingSeekBar = true;
            }

            /**
             * Stop record the change on seek bar
             * @param seekBar
             */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isMovingSeekBar = false;
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (isMovingSeekBar) {
                    Log.i("OnSeekBarChangeListener", "onProgressChanged");
                    long totalDuration = musicService.getPlayer().getTotalDuration();
                    int seekToPosition = TimeConverter.percentageToCurrentDuration(seekBar.getProgress(), totalDuration);

                    // forward or backward to certain seconds
                    musicService.getPlayer().seekPosition(seekToPosition);
                }
            }
        });

        /*---- THESE BUTTON OnTouchListener able modify the Button while touching it---*/
        // Set repeat mode on player, also change the button view
        this.repeatButton.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    repeatButton.setColorFilter(Color.RED);
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    repeatButton.setImageResource(musicService.getPlayer().setRepeat() ?
                            R.drawable.ic_action_replay: R.drawable.ic_action_repeat);
                    repeatButton.setColorFilter(compColor);
                    return true;
                }
                return false;
            }
        });

        this.playPrevButton.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    playPrevButton.setColorFilter(Color.RED);
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    Log.i(LOG_TAG, "Received Intent : PREV");
                    // Move the prev song if there is a valid prev song
                    try {
                        // Pause player if the player was pausing before move to prev song
                        if(musicService.getPlayer().getIsPause()) {
                            musicService.getPlayer().seekPrev(false);
                        }
                        // Play if the player was playing before moving to prev song
                        else {
                            musicService.getPlayer().seekPrev(true);
                        }
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
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    playButton.setColorFilter(Color.RED);
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {

                    try {
                        musicService.getPlayer().play();
                        // Need to update whole screen incase of the first time launching this fragment
                        // other cases, song info should stay the same after user click on play/pause
                        updateSongPlaying();
                        setColor();
                    } catch (NoSongToPlayException e) {
                        Toasty.info(context, "No Song To Play", Toast.LENGTH_SHORT, true).show();
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
                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    playNextButton.setColorFilter(Color.RED);
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    // Move the prev song if there is a valid prev song
                    try {
                        // Pause player if the player was pausing before move to next song
                        if(musicService.getPlayer().getIsPause()) {
                            musicService.getPlayer().seekNext(false);
                        }
                        else {
                            // Pause player if the player was pausing before move to next song
                            musicService.getPlayer().seekNext(true);
                        }
                        // update whole screen with new song info
                        updateSongPlaying();
                        setColor();
                    } catch (NoSongToPlayException e) {
                        Toasty.info(context, "No Song To Play", Toast.LENGTH_SHORT, true).show();
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
                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    shuffleButton.setColorFilter(Color.RED);
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    shuffleButton.setImageResource(musicService.getPlayer().setShuffle() ?
                            R.drawable.ic_action_shuffle: R.drawable.ic_action_shuffle_disabled);
                    shuffleButton.setColorFilter(compColor);
                    return true;
                }
                return false;
            }
        });


        /*-------------- THESE BUTTON OnClickListener works with ripple effect----------*/
        /*
        // Open a new fragment when click on lyrics button
        lyricsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getFragmentManager();
                android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                FragmentSongLyric songLyric = new FragmentSongLyric();
                fragmentTransaction.addToBackStack("FragmentMusicPlayer");
                fragmentTransaction.hide(FragmentMusicPlayer.this);
                fragmentTransaction.add(R.id.fragment_container, songLyric);
                fragmentTransaction.commit();
            }
        });

        // Open a new fragment when click on info button
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getFragmentManager();
                android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                FragmentMusicInfo songLyric = new FragmentMusicInfo();
                fragmentTransaction.addToBackStack("FragmentMusicPlayer");
                fragmentTransaction.hide(FragmentMusicPlayer.this);
                fragmentTransaction.add(R.id.fragment_container, songLyric);
                fragmentTransaction.commit();
            }
        });

        // Set repeat mode on player, also change the button view
        this.repeatButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                repeatButton.setImageResource(musicService.getPlayer().setRepeat() ?
                        R.drawable.ic_action_replay: R.drawable.ic_action_repeat);
            }
        });

        this.playPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(LOG_TAG, "Received Intent : PREV");
                // Move the prev song if there is a valid prev song
                try {
                    // Pause player if the player was pausing before move to prev song
                    if(musicService.getPlayer().getIsPause()) {
                        musicService.getPlayer().seekPrev(false);
                    }
                    // Play if the player was playing before moving to prev song
                    else {
                        musicService.getPlayer().seekPrev(true);
                    }
                    // update whole screen with new song info
                    updateSongPlaying();
                    setColor();
                } catch (NoSongToPlayException e) {
                    Toasty.info(context, "No Song To Play", Toast.LENGTH_SHORT, true).show();
                }
            }
        });

        this.playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    musicService.getPlayer().play();
                    // Need to update whole screen incase of the first time launching this fragment
                    // other cases, song info should stay the same after user click on play/pause
                    updateSongPlaying();
                    setColor();
                } catch (NoSongToPlayException e) {
                    Toasty.info(context, "No Song To Play", Toast.LENGTH_SHORT, true).show();
                }
            }
        });

        this.playNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Move the prev song if there is a valid prev song
                try {
                    // Pause player if the player was pausing before move to next song
                    if(musicService.getPlayer().getIsPause()) {
                        musicService.getPlayer().seekNext(false);
                    }
                    else {
                        // Pause player if the player was pausing before move to next song
                        musicService.getPlayer().seekNext(true);
                    }
                    // update whole screen with new song info
                    updateSongPlaying();
                    setColor();
                } catch (NoSongToPlayException e) {
                    Toasty.info(context, "No Song To Play", Toast.LENGTH_SHORT, true).show();
                }
            }
        });

        // Set shuffle mode on player, also change the button view
        this.shuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shuffleButton.setImageResource(musicService.getPlayer().setShuffle() ?
                        R.drawable.ic_action_shuffle: R.drawable.ic_action_shuffle_disabled);
            }
        });*/
    }

    /**
     * Update the screen with the current playing song
     * elems to be updated: SONG TITLE, ARTIST, COVER ART
     */
    private void updateSongPlaying() {
        songTitle.setText(musicService.getPlayer().getCurrentSong().getTitle());
        artist.setText(musicService.getPlayer().getCurrentSong().getArtist());

        if(musicService.getPlayer().getIsPause())
            playButton.setImageResource(R.drawable.ic_action_play);
        else
            playButton.setImageResource(R.drawable.ic_action_pause);

        // Setting the album image
        {
            try {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(musicService.getPlayer().getCurrentSong().getPath());
                byte[] art = retriever.getEmbeddedPicture();
                bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
            } catch (Exception exception) {
                Log.e(LOG_TAG, "NO COVER ART FOUND " + exception.getClass().getName());
                bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.cover_art_stock);
            } finally {
                circularProgressBar.setImageBitmap(bitmap);
            }
        }
    }

    /**
     * Update widget elems with color extract from the current COVER ART
     * elems to be updated: all BUTTON color
     *                      all TEXT color
     *
     */
    public void setColor(){
        int color = getDominantColor(bitmap);
        compColor = getComplementaryColor(color);
        int compColor2 = getComplementaryColor2(color);
        circularProgressBar.setBorderColor(compColor);
        rootView.setBackgroundColor(color);
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
        for (int y = 0; y < h ; y++) {
            for (int x = 0; x < w ; x++) {
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

    /*
    /**
     * Get the medium pixel color
     * Fast and easy way to extract color from bitmap
     * Out put not necessary the dominant color of the bitmap
     * /
    public static int getDominantColor(Bitmap bitmap) {
        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, 1, 1, true);
        final int color = newBitmap.getPixel(0, 0);
        newBitmap.recycle();
        return color;
    }*/

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
        int ave =  (Color.red(colorToInvert)
                + Color.green(colorToInvert)
                + Color.blue(colorToInvert)) / 3;
        return ave >= Color.BLACK ?  Color.WHITE : -1;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    //TODO AsynTask for update the song.
}
