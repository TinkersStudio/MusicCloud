package com.tinkersstudio.musiccloud.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.media.MediaMetadataRetriever;
import java.io.File;

import com.tinkersstudio.musiccloud.R;

import java.util.ArrayList;
import java.util.List;

import com.tinkersstudio.musiccloud.adapter.InfoListAdapter;
import com.tinkersstudio.musiccloud.controller.TimeConverter;
import com.tinkersstudio.musiccloud.model.Song;
import com.tinkersstudio.musiccloud.model.Info;

/**
 * Created by anhnguyen on 2/6/17.
 */

public class FragmentMusicInfoDetails extends Fragment {
    private static final String TAG = "RecyclerViewFragment";
    private static final String KEY_LAYOUT_MANAGER = "layoutManager";
    private static final int SPAN_COUNT = 2;

    private enum LayoutManagerType {
        GRID_LAYOUT_MANAGER,
        LINEAR_LAYOUT_MANAGER
    }

    protected LayoutManagerType mCurrentLayoutManagerType;

    protected RecyclerView mRecyclerView;
    protected InfoListAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected List<Info> mInfo;
    Song currentSong;
    public void setCurrentSong(Song currentSong) {this.currentSong = currentSong;}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInfo = new ArrayList<Info>();
        initDataset();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_song_info_detail, container, false);
        rootView.setTag(TAG);

        // BEGIN_INCLUDE(initializeRecyclerView)
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.si_detail_pane);

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        mLayoutManager = new LinearLayoutManager(getActivity());

        mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;

        if (savedInstanceState != null) {
            // Restore saved layout manager type.
            mCurrentLayoutManagerType = (LayoutManagerType) savedInstanceState
                    .getSerializable(KEY_LAYOUT_MANAGER);
        }
        setRecyclerViewLayoutManager(mCurrentLayoutManagerType);

        mAdapter = new InfoListAdapter(mInfo);
        // Set CustomAdapter as the com.tinkersstudio.musiccloud.adapter for RecyclerView.
        mRecyclerView.setAdapter(mAdapter);

        return rootView;
    }

    /**
     * Set RecyclerView's LayoutManager to the one given.
     *
     * @param layoutManagerType Type of layout manager to switch to.
     */
    public void setRecyclerViewLayoutManager(LayoutManagerType layoutManagerType) {
        int scrollPosition = 0;

        // If a layout manager has already been set, get current scroll position.
        if (mRecyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        }

        switch (layoutManagerType) {
            case GRID_LAYOUT_MANAGER:
                mLayoutManager = new GridLayoutManager(getActivity(), SPAN_COUNT);
                mCurrentLayoutManagerType = LayoutManagerType.GRID_LAYOUT_MANAGER;
                break;
            case LINEAR_LAYOUT_MANAGER:
                mLayoutManager = new LinearLayoutManager(getActivity());
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
                break;
            default:
                mLayoutManager = new LinearLayoutManager(getActivity());
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
        }

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.scrollToPosition(scrollPosition);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save currently selected layout manager.
        savedInstanceState.putSerializable(KEY_LAYOUT_MANAGER, mCurrentLayoutManagerType);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * Generates Strings for RecyclerView's com.tinkersstudio.musiccloud.adapter. This data would usually come
     * from a local content provider or remote server.
     */
    private void initDataset() {

        MediaMetadataRetriever metaRetriver;
        metaRetriver = new MediaMetadataRetriever();
        metaRetriver.setDataSource(currentSong.getPath());
        mInfo.add(new Info ("Album", metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)));
        mInfo.add(new Info ("Author", metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUTHOR)));
        mInfo.add(new Info ("Composer", metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPOSER)));
        mInfo.add(new Info ("Duration", TimeConverter.milliSecondsToTimeString(
                Integer.parseInt(metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)))));
        String rate = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
        mInfo.add(new Info ("Bit Rate", rate.substring(0, rate.length()-3) + " Kbps"));
        mInfo.add(new Info ("Genre", metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)));
        mInfo.add(new Info ("Year", metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)));
        mInfo.add(new Info ("Modified", metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE)));

        String value = "";
        File file = new File(currentSong.getPath());
        Double file_size = Double.parseDouble(String.valueOf(file.length()/1024));
        if(file_size >= 1024)
            value = String.format("%.2f", file_size/1024) +" Mb";
        else
            value = String.format("%.0f", file_size) + " Kb";
        mInfo.add(new Info ("Size", value));
        metaRetriver.release();
        //TODO Use myService
    }
}
