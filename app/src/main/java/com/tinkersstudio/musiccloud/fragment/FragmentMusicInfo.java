package com.tinkersstudio.musiccloud.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tinkersstudio.musiccloud.R;
import com.tinkersstudio.musiccloud.activities.MainActivity;
import com.tinkersstudio.musiccloud.controller.MusicService;

import org.jmusixmatch.MusixMatch;

/**
 * Created by Owner on 3/4/2017.
 */

public class FragmentMusicInfo extends Fragment {

    String API_KEY = "f4337155f55d30c22e85a96f2dc674c8";
    MusixMatch musixMatch = new MusixMatch(API_KEY);
    String LOG_TAG = "FragmentSongLyric";
    MusicService newService = ((MainActivity)getActivity()).myService;
    //LyricView mLyricView;
    TextView lyricText;
    String trackName = "";
    String artistName = "";

    public FragmentMusicInfo() {
        //require constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_song_info, container, false);
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
