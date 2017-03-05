package com.tinkersstudio.musiccloud;

import android.app.FragmentTransaction;
import android.content.Context;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;


/**
 * Created by anhnguyen on 2/6/17.
 */

public class FragmentMusicPlayer extends Fragment {
    Context context;
    ImageButton lyricsButton, infoButton;

    //view items
    View rootView;

    //music player group
    ImageButton repeatButton, playPrevButton, playButton, playNextButton, shuffleButton;

    public FragmentMusicPlayer() {
        //require constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_music_player, container, false);

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

            }
        });


        this.playPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        this.playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        this.playNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        this.shuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
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
