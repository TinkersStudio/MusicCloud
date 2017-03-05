package com.tinkersstudio.musiccloud;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import io.swagger.client.api.DefaultApi;

import static com.tinkersstudio.musiccloud.R.string.music_match_api_key;

/**
 * Created by Owner on 3/4/2017.
 */

public class FragmentSongLyric extends Fragment {

    String API_KEY = "f4337155f55d30c22e85a96f2dc674c8";
    MusixMatch musixMatch = new MusixMatch(API_KEY);
    public FragmentSongLyric() {
        //require constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_song_lyric, container, false);
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
}
