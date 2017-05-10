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

import com.tinkersstudio.musiccloud.R;
import com.tinkersstudio.musiccloud.adapter.RadioListAdapter;
import com.tinkersstudio.musiccloud.controller.MusicService;
import com.tinkersstudio.musiccloud.controller.MyRadio;
import com.tinkersstudio.musiccloud.model.Radio;
import com.tinkersstudio.musiccloud.util.MyFlag;

import java.util.List;

/**
 * Created by anhnguyen on 5/3/17.
 */

public class FragmentRadioLibrary extends Fragment {
    private static final String LOG_TAG = "FragmentRadio";
    private static final String KEY_LAYOUT_MANAGER = "layoutManager";
    private static final int SPAN_COUNT = 2;

    private enum LayoutManagerType {
        GRID_LAYOUT_MANAGER,
        LINEAR_LAYOUT_MANAGER
    }

    private LayoutManagerType mCurrentLayoutManagerType;

    private RecyclerView mRecyclerView;
    private RadioListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<Radio> mDataset;
    private MyRadio myRadio;
    private FragmentRadio fmRadio;
    private MusicService musicService;

    public void setService(MusicService service) {musicService = service;}
    public void setRadio(MyRadio radio){this.myRadio = radio;}
    public void setTargetResult(FragmentRadio fmRadio) {this.fmRadio = fmRadio;}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Log.i(LOG_TAG, "Call init data set");
        initDataset();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_radio_list, container, false);
        rootView.setTag(LOG_TAG);

        // BEGIN_INCLUDE(initializeRecyclerView)
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycle_view_radio_list);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
        if (savedInstanceState != null) {
            // Restore saved layout manager type.
            mCurrentLayoutManagerType = (LayoutManagerType) savedInstanceState
                    .getSerializable(KEY_LAYOUT_MANAGER);
        }
        this.setRecyclerViewLayoutManager(mCurrentLayoutManagerType);

        //Log.i(LOG_TAG, "create RadioListAdapter with list of " +  mDataset.size() + " radios");
        mAdapter = new RadioListAdapter(mDataset, musicService, MyFlag.LIST_LIBRARY);
        mAdapter.setTargetResult(fmRadio);
        // Set CustomAdapter as the com.tinkersstudio.musiccloud.adapter for RecyclerView.
        mRecyclerView.setAdapter(mAdapter);
        return rootView;
    }


    @Override
    public void onStart() {
        super.onStart();
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
    public void onDestroyView(){
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void initDataset() {
        mDataset = myRadio.getLibraryStations();
    }
}
