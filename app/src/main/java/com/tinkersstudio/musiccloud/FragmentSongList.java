package com.tinkersstudio.musiccloud;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by anhnguyen on 2/6/17.
 */

public class FragmentSongList extends AppCompatActivity {
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_song_list);
        context = getApplicationContext();
    }
}
