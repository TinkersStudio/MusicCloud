package com.tinkersstudio.musiccloud;

import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.Toast;
import java.io.InputStream;
import controller.MusicService;
import controller.MyFlag;
import controller.NoSongToPlayException;
import es.dmoral.toasty.Toasty;
import info.abdolahi.CircularMusicProgressBar;
import android.graphics.Color;

/**
 * Created by anhnguyen on 2/6/17.
 */

public class FragmentMusicPlayer extends Fragment {
    String LOG_TAG = "FragmentMusicPlayer";
    Context context;
    ImageButton lyricsButton, infoButton;
    TextView songTitle, artist;
    CircularMusicProgressBar circularProgressBar;

    Handler myHandler = new Handler();

    //Service to cotrol playback
    MusicService musicService;

    // The album picture to extract color;
    Bitmap bitmap = null;

    //view items
    View rootView;

    //music player group
    ImageButton repeatButton, playPrevButton, playButton, playNextButton, shuffleButton;

    public FragmentMusicPlayer() {
        //require constructor
    }

    public void setMusicService(MusicService musicService) {
        this.musicService = musicService;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_music_player, container, false);

        //get value from the
        initLayout();
        initAction();
        return rootView;

        //initialize button in here
    }

    /**
     * Init the layout component of the page
     */
    public void initLayout()
    {
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
    }

    /**
     * Init the button listener
     */
    public void initAction(){
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
                try {
                    if(musicService.getPlayer().getIsPause()) {
                        musicService.getPlayer().seekPrev(false);
                    }
                    else {
                        musicService.getPlayer().seekPrev(true);
                    }
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
                try {
                    if(musicService.getPlayer().getIsPause()) {
                        musicService.getPlayer().seekNext(false);
                    }
                    else {
                        musicService.getPlayer().seekNext(true);
                    }
                    updateSongPlaying();
                    setColor();
                } catch (NoSongToPlayException e) {
                    Toasty.info(context, "No Song To Play", Toast.LENGTH_SHORT, true).show();
                }
            }
        });

        this.shuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shuffleButton.setImageResource(musicService.getPlayer().setShuffle() ?
                        R.drawable.ic_action_shuffle: R.drawable.ic_action_shuffle_disabled);
                Log.e(LOG_TAG, "Shuffle : " + musicService.getPlayer().isShuffle);
            }
        });
    }

    private void updateSongPlaying() {
        songTitle.setText(musicService.getPlayer().getCurrentSong().getTitle());
        artist.setText(musicService.getPlayer().getCurrentSong().getArtist());

        if(musicService.getPlayer().getIsPause())
            playButton.setImageResource(R.drawable.ic_action_play);
        else
            playButton.setImageResource(R.drawable.ic_action_pause);

        // Setting the album image
        {
            Uri sArtworkUri = Uri
                    .parse("content://media/external/audio/albumart");

            try {
                Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, musicService.getPlayer().getCurrentSong().getAlbumArt());
                ContentResolver res = getActivity().getContentResolver();
                InputStream in = res.openInputStream(albumArtUri);
                bitmap = BitmapFactory.decodeStream(in);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    circularProgressBar.setClipToOutline(true);
                    circularProgressBar.setImageBitmap(bitmap);
                }
            } catch (Exception exception) {
                //exception.printStackTrace();
                circularProgressBar.setImageResource(R.drawable.cover_art_stock);
                bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.cover_art_stock);
            }
        }
    }

    // Set color of the ui according to the most popular color appear on the album cover
    public void setColor(){
        int color = getDominantColor(bitmap);
        int compColor = getComplementaryColor(color);
        circularProgressBar.setBorderColor(compColor);
        rootView.setBackgroundColor(color);
        repeatButton.setColorFilter(compColor);
        shuffleButton.setColorFilter(compColor);
        playNextButton.setColorFilter(compColor);
        playPrevButton.setColorFilter(compColor);
        playButton.setColorFilter(compColor);
    }

    // extract the dominant color in the album cover
    public static int getDominantColor(Bitmap bitmap) {
        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, 1, 1, true);
        final int color = newBitmap.getPixel(0, 0);
        newBitmap.recycle();
        return color;
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
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
