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
    Context context;
    public static TabLayout tabLayout;
    public static ViewPager viewPager;
    public static int int_items = 3 ;
    TextView title, artist;
    ImageButton favor;
    ImageView artCover;
    Bitmap bitmap;
    LinearLayout header;
    int dominantColor, compColor, compColor2;
    FragmentSongLyric fmSongLyric;
    Song currentSong;

    public FragmentMusicInfo() {
        //require constructor
    }

    public void setCurrentSong(Song currentSong){this.currentSong = currentSong;}

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

        //Set an Apater for the View Pager
        viewPager.setAdapter(new MyAdapter(getChildFragmentManager()));
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

        fmSongLyric = new FragmentSongLyric();
        fmSongLyric.hideQuitButton();

        return view;

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
                    FragmentMusicInfoDetails fmInfoDetails = new FragmentMusicInfoDetails();
                    fmInfoDetails.setCurrentSong(currentSong);
                    return fmInfoDetails;
                }
                case 1 : {
                    fmSongLyric.setCurrentSong(currentSong);
                    return fmSongLyric;
                }
                case 2: {
                    FragmentMusicInfoDetails fmInfoDetails = new FragmentMusicInfoDetails();
                    fmInfoDetails.setCurrentSong(currentSong);
                    return fmInfoDetails;
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