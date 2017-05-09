package com.tinkersstudio.musiccloud.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.crash.FirebaseCrash;
import com.tinkersstudio.musiccloud.R;
import com.tinkersstudio.musiccloud.activities.MainActivity;
import com.tinkersstudio.musiccloud.adapter.RadioListAdapter;
import com.tinkersstudio.musiccloud.controller.MusicService;
import com.tinkersstudio.musiccloud.controller.MyRadio;
import com.tinkersstudio.musiccloud.model.Radio;
import com.tinkersstudio.musiccloud.util.MyFlag;

import java.util.List;


/**
 * Created by anhnguyen on 5/3/17.
 */

public class FragmentRadio extends Fragment {
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


    // A handler to manage the Runnable which is used to update UI
    private static Handler mHandler = new Handler();
    private ImageButton addStation;
    private TextView stationName;
    private TextView title;
    private TextView artist;
    private TextView genre;
    private TextView status;
    private TextView source;

    // Need service to pass to Song View Holder in order to play song at index
    MusicService myService = ((MainActivity)getActivity()).myService;
    MyRadio myRadio = (MyRadio)myService.getPlayer(MyFlag.RADIO_MODE);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Log.i(LOG_TAG, "Call init data set");
        initDataset();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_radio, container, false);
        rootView.setTag(LOG_TAG);

        // BEGIN_INCLUDE(initializeRecyclerView)
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycle_radio_list);
        addStation = (ImageButton)rootView.findViewById(R.id.fr_add);
        stationName = (TextView)rootView.findViewById(R.id.fr_header_station_name);
        title = (TextView)rootView.findViewById(R.id.fr_header_title);
        artist = (TextView)rootView.findViewById(R.id.fr_header_artist);
        genre = (TextView)rootView.findViewById(R.id.fr_header_genre);
        status = (TextView)rootView.findViewById(R.id.fr_header_status);
        source = (TextView)rootView.findViewById(R.id.fr_header_source);
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
        this.setRecyclerViewLayoutManager(mCurrentLayoutManagerType);

        //Log.i(LOG_TAG, "create RadioListAdapter with list of " +  mDataset.size() + " radios");
        mAdapter = new RadioListAdapter(mDataset, myService);
        // Set CustomAdapter as the com.tinkersstudio.musiccloud.adapter for RecyclerView.
        mRecyclerView.setAdapter(mAdapter);

        addStation.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    addStation.setColorFilter(Color.RED);
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    addStation.setColorFilter(Color.WHITE);

                    //TODO: need a better way to add station since most user not knowing streaming url to enter

                    // Popup an dialog fragment to add a station, then update view
                    View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.view_add_radio, (ViewGroup) getView(), false);

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Add New Chanel");

                    // Set up the input
                    final EditText name = (EditText)viewInflated.findViewById(R.id.fr_dialog_name);
                    final EditText url = (EditText) viewInflated.findViewById(R.id.fr_dialog_url);

                    builder.setView(viewInflated);

                    // Set up the buttons
                    builder.setPositiveButton("ADD Channel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            myRadio.addRadio(new Radio(url.getText().toString(), name.getText().toString()));
                            mAdapter.notifyDataSetChanged();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();
                    return true;
                }
                return false;
            }
        });

        return rootView;
    }


    @Override
    public void onStart() {
        super.onStart();
        // Start the handler, which run the Runnable mUpdateTimeTask
        mHandler.postDelayed(mUpdateStreamTask, 1000);

        // Not pausing means there is a radio that is playing,
        // need to trigger the infoReady to update
        if (!myRadio.getIsPause()) {
            myRadio.setInfoReady(true);
        }
    }

    /**
     * A Runnable which run separately on the back ground to update UI of this fragment
     * It is scheduled to update the seekbar every 1 second,
     * and update the whole screen when a song is complete playing
     */
    private Runnable mUpdateStreamTask = new Runnable() {
        @Override
        public void run() {
            try {
                //Log.i(LOG_TAG, "mUpdateStreamTask");
                // Update seekbar only if the song playing
                if (myRadio.isInfoReady()) {
                    //Log.i(LOG_TAG, "Updating info");
                    stationName.setText(myRadio.getCurrentStationName());
                    stationName.setSelected(true);
                    title.setText(myRadio.getCurrentTitle());
                    title.setSelected(true);
                    artist.setText(myRadio.getCurrentArtist());
                    artist.setSelected(true);
                    genre.setText(myRadio.getCurrentStationGenre());
                    genre.setSelected(true);
                    status.setText(myRadio.getCurrentStationStatus());
                    source.setText(myRadio.getCurrentSource());
                    source.setSelected(true);

                    if (myRadio.getCurrentStationChanged()) {
                        mAdapter.notifyDataSetChanged();
                        myRadio.setCurrentStationChanged();
                    }
                    myRadio.setInfoReady(false);
                }
                // Running this thread after 1000 milliseconds
                mHandler.postDelayed(mUpdateStreamTask, 1000);
            } catch (Exception e) {
                //Exception thrown when Service haven't up yet
                e.printStackTrace();
                FirebaseCrash.report(e);
            }
        }
    };
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
        mHandler.removeCallbacks(mUpdateStreamTask);
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
        mDataset = ((MyRadio)myService.getPlayer(MyFlag.RADIO_MODE)).getRadioList();
    }
}
