package com.tinkersstudio.musiccloud.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tinkersstudio.musiccloud.R;

/**
 * Created by Jun Trinh on 2/19/2017.
 * Display the user listening pattern
 */

public class FragmentUserStat extends Fragment {
    public FragmentUserStat(){
        //require an empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_stats, container, false);

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
