package com.tinkersstudio.musiccloud;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jmusixmatch.MusixMatch;

/**
 * Created by Owner on 3/4/2017.
 */

public class FragmentMusicInfo extends Fragment {
    String API_KEY = getActivity().getResources().getString(R.string.music_match_api_key);
    MusixMatch musixMatch = new MusixMatch(API_KEY);
    String LOG_TAG = "FragmentSongLyric";

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
