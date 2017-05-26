package com.tinkersstudio.musiccloud.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tinkersstudio.musiccloud.R;
import com.tinkersstudio.musiccloud.model.Song;

/**
 * Created by Owner on 3/4/2017.
 */

public class FragmentMusicInfo extends Fragment {
    private String LOG_TAG = "FragmentMusicInfo";
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private final int int_items = 3 ;
    private TextView title, artist;
    private ImageButton favor;
    private ImageView artCover;
    private Bitmap bitmap;
    private LinearLayout header;
    private int dominantColor, compColor, compColor2;
    private FragmentSongLyric fmSongLyric;
    private FragmentMusicInfoArtist fmInfoArtist;
    private FragmentMusicInfoDetails fmInfoDetails;
    private Song currentSong;

    public void setCurrentSong(Song currentSong){
        Log.i(LOG_TAG, "set current song: " + currentSong.getTitle());
        this.currentSong = currentSong;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_song_info, container, false);
        //setup the tab
        tabLayout = (TabLayout) view.findViewById(R.id.si_tabs);
        viewPager = (ViewPager) view.findViewById(R.id.si_viewpager);
        header = (LinearLayout) view.findViewById(R.id.si_header);
        title = (TextView)view.findViewById(R.id.si_title);
        artist = (TextView)view.findViewById(R.id.si_artist);
        favor = (ImageButton)view.findViewById(R.id.si_favor);
        artCover = (ImageView)view.findViewById(R.id.si_cover_art);

        //Set an Adater for the View Pager
        viewPager.setAdapter(new MyAdapter(getChildFragmentManager()));
        viewPager.setOffscreenPageLimit(int_items); // Keep all 3 tabs alive so it won't get recreate when user paging
        tabLayout.post(new Runnable() {
            @Override
            public void run() {
                tabLayout.setupWithViewPager(viewPager);
            }
        });

        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(currentSong.getPath());
            byte[] art = retriever.getEmbeddedPicture();
            bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
            retriever.release();
        } catch (Exception exception) {
            bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.cover_art_stock);
        } finally {
            dominantColor = FragmentMusicPlayer.getDominantColor(bitmap);
            compColor = FragmentMusicPlayer.getComplementaryColor(dominantColor);
            compColor2 = FragmentMusicPlayer.getComplementaryColor2(dominantColor);
            artCover.setImageBitmap(bitmap);
        }
        header.setBackgroundColor(dominantColor);
        title.setText(currentSong.getTitle());
        title.setTextColor(compColor2);
        title.setSelected(true);
        artist.setText(currentSong.getArtist());
        artist.setTextColor(compColor);
        artist.setSelected(true);


        fmInfoDetails = new FragmentMusicInfoDetails();
        fmInfoDetails.setCurrentSong(currentSong);

        try {
            fmSongLyric = new FragmentSongLyric();
            fmSongLyric.setCurrentSong(currentSong);
            fmSongLyric.hideQuitButton();
        } catch (java.lang.IllegalStateException e) {
            Log.i("FragmentMusicInfo", "not attached to Activity");
        }
        try {
            fmInfoArtist = new FragmentMusicInfoArtist();
            fmInfoArtist.setCurrentSong(currentSong);
        } catch (Exception e) {
            Log.i(LOG_TAG, "Fail to connect LastFM API");
        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    class MyAdapter extends FragmentPagerAdapter {

        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * Return fragment with respect to Position .
         */

        @Override
        public Fragment getItem(int position)
        {
            switch (position){
                case 0 : {
                    return fmInfoDetails;
                }
                case 1 : {
                    return fmSongLyric;
                }
                case 2: {
                    return fmInfoArtist;
                }
            }
            return null;
        }

        @Override
        public int getCount() {

            return int_items;

        }

        /**
         * This method returns the title of the tab according to the position.
         */

        @Override
        public CharSequence getPageTitle(int position) {

            switch (position){
                case 0 :
                    return "Details";
                case 1 :
                    return "Lyric";
                case 2 : {
                    return (currentSong == null ? "Artist" : currentSong.getArtist());
                }
            }
            return null;
        }
    }

}