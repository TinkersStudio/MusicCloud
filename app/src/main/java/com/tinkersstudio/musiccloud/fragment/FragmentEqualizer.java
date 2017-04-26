package com.tinkersstudio.musiccloud.fragment;

import android.content.Context;
import android.media.audiofx.Equalizer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.SeekBar;

import com.tinkersstudio.musiccloud.R;
import com.tinkersstudio.musiccloud.activities.MainActivity;
import com.tinkersstudio.musiccloud.controller.MusicService;
import com.xw.repo.BubbleSeekBar;


/**
 * Created by anhnguyen on 2/6/17.
 */

public class FragmentEqualizer extends Fragment {
    String LOG_TAG = "FragmentEqualizer";
    Context context;
    BubbleSeekBar mBubbleSeekBar1;
    BubbleSeekBar mBubbleSeekBar2;
    BubbleSeekBar mBubbleSeekBar3;
    BubbleSeekBar mBubbleSeekBar4;
    BubbleSeekBar mBubbleSeekBar5;
    SeekBar volumeBalance;
    NumberPicker presetMode;
    View viewRoot;
    Equalizer myEQ;
    MusicService myService = ((MainActivity)getActivity()).myService;
    String[] presetList;

    boolean isMovingBalance;

    public FragmentEqualizer(){
        //require an empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);

        viewRoot = inflater.inflate(R.layout.fragment_equalizer, container, false);
        return viewRoot;
    }

    /**
     * Since the Equalizer need getAudioSessionId() to access the Audio
     *  to get data before seting up layout
     *
     *  on Resume will do jobs of setting up layout to make sure this Equalizer already have
     *  access to getAudioSessionId()
     */
    @Override
    public void onResume(){
        super.onResume();

        myEQ = new Equalizer(0, myService.getPlayer().getAudioSessionId());
        myEQ.setEnabled(true);


        // Need to have getAudioSessionId() prior to
        getPresetList(); // get presetList before initLayout
        initLayout();
        setListener();

    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void initLayout()
    {
        mBubbleSeekBar1 = (BubbleSeekBar) viewRoot.findViewById(R.id.eq_seekbar1);
        mBubbleSeekBar2 = (BubbleSeekBar) viewRoot.findViewById(R.id.eq_seekbar2);
        mBubbleSeekBar3 = (BubbleSeekBar) viewRoot.findViewById(R.id.eq_seekbar3);
        mBubbleSeekBar4 = (BubbleSeekBar) viewRoot.findViewById(R.id.eq_seekbar4);
        mBubbleSeekBar5 = (BubbleSeekBar) viewRoot.findViewById(R.id.eq_seekbar5);
        presetMode = (NumberPicker) viewRoot.findViewById(R.id.eq_preset_picker);
        volumeBalance = (SeekBar)viewRoot.findViewById(R.id.eq_balance);


        //Populate NumberPicker values from String array values
        //Set the minimum value of NumberPicker
        presetMode.setMinValue(0); //from array first value
        //Specify the maximum value/number of NumberPicker
        presetMode.setMaxValue(presetList.length-1); //to array last value

        //Specify the NumberPicker data source as array elements
        presetMode.setDisplayedValues(presetList);

        //Gets whether the selector wheel wraps when reaching the min/max value.
        presetMode.setWrapSelectorWheel(true);

        // Set Picker to show the current preset mode / or Normal (mode 0)
        try {
            presetMode.setValue(myEQ.getCurrentPreset());
        } catch (Exception e) {
            presetMode.setValue(0);
        }
    }

    private void setListener(){
        //Set a value change listener for NumberPicker
        presetMode.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal){

                // Set the preset Mode if selected preset mode is not the last one on the list (Custom)
                // Also reset all the seekbars according to new Mode
                Log.i(LOG_TAG, "User select preset mode: " + presetList[newVal]);
                if (newVal < presetList.length - 1) {
                    myEQ.usePreset((short) newVal);

                    //reset seekbar to new Mode
                    //FIXME fine the new range of each band to set
                    mBubbleSeekBar1.setProgress((float)0);
                    mBubbleSeekBar2.setProgress((float)0);
                    mBubbleSeekBar3.setProgress((float)0);
                    mBubbleSeekBar4.setProgress((float)0);
                    mBubbleSeekBar5.setProgress((float)0);
                }
            }
        });

        mBubbleSeekBar1.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListenerAdapter() {
            @Override
            public void getProgressOnActionUp(int progress) {
                // Set the picker to show the last preset mode which is "Custom"
                Log.i(LOG_TAG, "Set Picker to point to Custom: --> " + presetList[presetList.length - 1]);
                presetMode.setValue(presetList.length - 1);

                //TODO: change the Equalizer's range according to user's customization
            }
        });

        mBubbleSeekBar2.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListenerAdapter() {
            @Override
            public void getProgressOnActionUp(int progress) {
                // Set the picker to show the last preset mode which is "Custom"
                Log.i(LOG_TAG, "Set Picker to point to Custom: --> " + presetList[presetList.length - 1]);
                presetMode.setValue(presetList.length - 1);

                //TODO: change the Equalizer's range according to user's customization
            }
        });

        mBubbleSeekBar3.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListenerAdapter() {
            @Override
            public void getProgressOnActionUp(int progress) {
                // Set the picker to show the last preset mode which is "Custom"
                Log.i(LOG_TAG, "Set Picker to point to Custom: --> " + presetList[presetList.length - 1]);
                presetMode.setValue(presetList.length - 1);

                //TODO: change the Equalizer's range according to user's customization
            }
        });

        mBubbleSeekBar4.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListenerAdapter() {
            @Override
            public void getProgressOnActionUp(int progress) {
                // Set the picker to show the last preset mode which is "Custom"
                Log.i(LOG_TAG, "Set Picker to point to Custom: --> " + presetList[presetList.length - 1]);
                presetMode.setValue(presetList.length - 1);

                //TODO: change the Equalizer's range according to user's customization
            }
        });

        mBubbleSeekBar5.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListenerAdapter() {
            @Override
            public void getProgressOnActionUp(int progress) {
                // Set the picker to show the last preset mode which is "Custom"
                Log.i(LOG_TAG, "Set Picker to point to Custom: --> " + presetList[presetList.length - 1]);
                presetMode.setValue(presetList.length - 1);

                //TODO: change the Equalizer's range according to user's customization
            }
        });

        volumeBalance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /**
             * Start record the change on seek bar
             * @param seekBar
             */
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isMovingBalance = true;
            }

            /**
             * Stop record the change on seek bar
             * @param seekBar
             */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isMovingBalance = false;
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (isMovingBalance) {
                    Log.i("OnSeekBarChangeListener", "onProgressChanged");

                    //TODO change Speaker Balance by calling method setVolume on MyPlayer

                }
            }
        });
    }

    private void getPresetList(){
        int size = myEQ.getNumberOfPresets();
        presetList = new String[size + 1];

        Log.i(LOG_TAG, "# of preset: " +size );

        for(short preset = 0; preset < size; preset++) {
            Log.i(LOG_TAG, myEQ.getPresetName(preset));
            presetList[preset] = (myEQ.getPresetName(preset));
        }
        presetList[size] = "Custom";
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_main, menu);
    }
}
