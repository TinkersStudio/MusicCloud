package com.tinkersstudio.musiccloud.fragment;

import android.content.Context;
import android.media.audiofx.Equalizer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.SeekBar;

import com.tinkersstudio.musiccloud.R;
import com.tinkersstudio.musiccloud.controller.Player;
import com.tinkersstudio.musiccloud.model.PresetFrequency;
import com.xw.repo.BubbleSeekBar;



/**
 * Created by anhnguyen on 2/6/17.
 *
 *
 *  Equalizer Stats
 *  Band	Freq(Hz)	    Center(Hz)	Type
 *  ————————————————————————————————————————
 *  1		30  - 120  	    60		    Bass
 *  2 		120 - 460 	    230		    Low-Mid
 *  3		460 - 1800	    910		    Mid
 *  4		1.8 k - 7 k 	3.6 k		Low-Treble
 *  5		7k - …		    14 k		Treble
 *
 *  Preset frequencies
 *  Band    Folk    Hip-Hop	HeavyMetal  Jazz    Pop 	Rock    Normal	Classical   Dance   Flat
 *  ——————————————————————————————————————————————————————————————————————-------------------
 *  1		300		500		400		    400	    -100	500		300		500		    600     0
 *  2 		0		300		100		    200	     200	300		0		300		    0		0
 *  3		0		0		900		    -200	500		-100	0		-200	    200		0
 *  4		200		100		300		    200		100		300		0		400		    400		0
 *  5		-100	300		  0		    500    	-200	500		300		400		    100		0
 *
 */

public class FragmentEqualizer extends Fragment {

    private String LOG_TAG = "FragmentEqualizer";

    private View viewRoot;

    // To modify sound
    private Equalizer myEQ;
    private Player myPlayer;

    // Preset variables
    private NumberPicker presetMode;
    private String[] presetListName;    // Just list of Name to display
    private PresetFrequency[] presetFrequencies;    // Actual preset frequencies data

    private BubbleSeekBar mBubbleSeekBar1, mBubbleSeekBar2, mBubbleSeekBar3, mBubbleSeekBar4, mBubbleSeekBar5;
    private boolean isMovingBalance;

    private SeekBar volumeBalance;


    /**
     * Setter of MusicPlayer
     * @param myPlayer is the player on back ground
     */
    public void setMusicPlayer(Player myPlayer) {this.myPlayer = myPlayer;}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);

        viewRoot = inflater.inflate(R.layout.fragment_equalizer, container, false);

        // Get Equalizer
        myEQ = new Equalizer(0, myPlayer.getAudioSessionId());
        myEQ.setEnabled(true);

        getPresetList(); // get presetList before initLayout because the picker need data
        initLayout();
        setListener();
        return viewRoot;
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
     *  Initialize the layout
     */
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
        presetMode.setMaxValue(presetListName.length-1); //to array last value

        //Specify the NumberPicker data source as array elements
        presetMode.setDisplayedValues(presetListName);

        //Gets whether the selector wheel wraps when reaching the min/max value.
        presetMode.setWrapSelectorWheel(true);

        // Set Picker to show the current preset mode / or Normal (mode 0)
        try {
            presetMode.setValue(myEQ.getCurrentPreset());
        } catch (Exception e) {
            presetMode.setValue(0);
        }
    }

    /**
     * Set up listener
     */
    private void setListener(){
        //Set a value change listener for NumberPicker
        presetMode.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal){
                //Log.i(LOG_TAG, "User select preset mode: " + presetListName[newVal]);

                // SET the equalizer to use the preset frequencies
                // and reset seekbar to new Mode
                for (PresetFrequency pf : presetFrequencies) {
                    if (presetListName[newVal].toLowerCase().compareTo(pf.getName()) == 0) {
                        mBubbleSeekBar1.setProgress((float) pf.getBand1());
                        mBubbleSeekBar2.setProgress((float) pf.getBand2());
                        mBubbleSeekBar3.setProgress((float) pf.getBand3());
                        mBubbleSeekBar4.setProgress((float) pf.getBand4());
                        mBubbleSeekBar5.setProgress((float) pf.getBand5());
                        myEQ.setBandLevel((short)0, (short)pf.getBand1());
                        myEQ.setBandLevel((short)1, (short)pf.getBand2());
                        myEQ.setBandLevel((short)2, (short)pf.getBand3());
                        myEQ.setBandLevel((short)3, (short)pf.getBand4());
                        myEQ.setBandLevel((short)4, (short)pf.getBand5());
                    }
                }
            }
        });

        /** -----------------------CUSTOMIZE Equalizer-----------------------------
         * when user maka change to a band,
         * set new frequency of that band , reset other bands if user just start customizing
         * set picker to show "CUSTOM" mode
         */

        // BAND 1 - BASS (30  - 120 Hz)
        mBubbleSeekBar1.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListenerAdapter() {
            @Override
            public void getProgressOnActionUp(int progress) {

                // Reset all other seekbar if user start customizing equalizer
                // (current preset mode is not CUSTOM)
                if (presetListName[presetMode.getValue()].toLowerCase().compareTo("custom") != 0) {
                    mBubbleSeekBar2.setProgress((float)0);
                    mBubbleSeekBar3.setProgress((float)0);
                    mBubbleSeekBar4.setProgress((float)0);
                    mBubbleSeekBar5.setProgress((float)0);
                }
                // Set the picker to show the last preset mode which is "Custom"
                presetMode.setValue(presetListName.length - 1);

                // Set new frequency to this band
                // Make sure the hardware does support up to this band
                if (myEQ.getNumberOfBands() >= 0) {
                    myEQ.setBandLevel((short) 0, (short) progress);
                }
            }
        });

        // BAND 2 - Low-MID (120 - 460 Hz)
        mBubbleSeekBar2.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListenerAdapter() {
            @Override
            public void getProgressOnActionUp(int progress) {

                // Reset all other seekbar if user start customizing equalizer
                // (current preset mode is not CUSTOM)
                if (presetListName[presetMode.getValue()].toLowerCase().compareTo("custom") != 0) {
                    mBubbleSeekBar1.setProgress((float)0);
                    mBubbleSeekBar3.setProgress((float)0);
                    mBubbleSeekBar4.setProgress((float)0);
                    mBubbleSeekBar5.setProgress((float)0);
                }
                // Set the picker to show the last preset mode which is "Custom"
                presetMode.setValue(presetListName.length - 1);

                // Set new frequency to this band
                // Make sure the hardware does support up to this band
                if (myEQ.getNumberOfBands() >= 0) {
                    myEQ.setBandLevel((short) 1, (short) progress);
                }
            }
        });

        // BAND 3 - MID (460 Hz - 1.8 KHz)
        mBubbleSeekBar3.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListenerAdapter() {
            @Override
            public void getProgressOnActionUp(int progress) {

                // Reset all other seekbar if user start customizing equalizer
                // (current preset mode is not CUSTOM)
                if (presetListName[presetMode.getValue()].toLowerCase().compareTo("custom") != 0) {
                    mBubbleSeekBar1.setProgress((float)0);
                    mBubbleSeekBar2.setProgress((float)0);
                    mBubbleSeekBar4.setProgress((float)0);
                    mBubbleSeekBar5.setProgress((float)0);
                }
                // Set the picker to show the last preset mode which is "Custom"
                presetMode.setValue(presetListName.length - 1);

                // Set new frequency to this band
                // Make sure the hardware does support up to this band
                if (myEQ.getNumberOfBands() >= 0) {
                    myEQ.setBandLevel((short) 2, (short) progress);
                }
            }
        });

        // BAND 4 - Low-TREBLE (1.8 - 7 KHz)
        mBubbleSeekBar4.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListenerAdapter() {
            @Override
            public void getProgressOnActionUp(int progress) {

                // Reset all other seekbar if user start customizing equalizer
                // (current preset mode is not CUSTOM)
                if (presetListName[presetMode.getValue()].toLowerCase().compareTo("custom") != 0) {
                    mBubbleSeekBar1.setProgress((float)0);
                    mBubbleSeekBar2.setProgress((float)0);
                    mBubbleSeekBar3.setProgress((float)0);
                    mBubbleSeekBar5.setProgress((float)0);
                }
                // Set the picker to show the last preset mode which is "Custom"
                presetMode.setValue(presetListName.length - 1);

                /// Set new frequency to this band
                // Make sure the hardware does support up to this band
                if (myEQ.getNumberOfBands() >= 0) {
                    myEQ.setBandLevel((short) 3, (short) progress);
                }
            }
        });

        // BAND 5 - TREBLE ( > 7 KHz)
        mBubbleSeekBar5.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListenerAdapter() {
            @Override
            public void getProgressOnActionUp(int progress) {

                // Reset all other seekbar if user start customizing equalizer
                // (current preset mode is not CUSTOM)
                if (presetListName[presetMode.getValue()].toLowerCase().compareTo("custom") != 0) {
                    mBubbleSeekBar1.setProgress((float)0);
                    mBubbleSeekBar2.setProgress((float)0);
                    mBubbleSeekBar3.setProgress((float)0);
                    mBubbleSeekBar4.setProgress((float)0);
                }
                // Set the picker to show the last preset mode which is "Custom"
                presetMode.setValue(presetListName.length - 1);

                // Set new frequency to this band
                // Make sure the hardware does support up to this band
                if (myEQ.getNumberOfBands() >= 0) {
                    myEQ.setBandLevel((short) 4, (short) progress);
                }
            }
        });

        // Change balance of Speaker (Left|Right)
        volumeBalance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {isMovingBalance = true;}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {isMovingBalance = false;}

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (isMovingBalance) {
                    // Calculate value to set Volume
                    // parameter of setVolume is from 0.0 - 1.0
                    // Maximize the speaker that is set higher (1.0)
                    float right = Float.valueOf(2) * (progress/Float.valueOf(100));
                    float left = Float.valueOf(2) * (Float.valueOf(2) - right);
                    if (left > Float.valueOf(1)) {left = Float.valueOf(1);}
                    if (right > Float.valueOf(1)) {right= Float.valueOf(1);}

                    myPlayer.setVolume(left, right);
                    //Log.i("OnSeekBarChangeListener", "Progress: " + progress + ". Set Volume L(" + left + ")\tR(" + right + ")");
                }
            }
        });
    }

    /**
     * Build the preset list
     */
    private void getPresetList(){

        // There are 10 presets, and CUSTOM mode
        // The order of 10 Presets is
        // Normal | Classical | Dance | Flat | Folk | HeavyMetal | Hip Hop | Jazz | Pop | Rock | Custom
        int numOfPreset = 11;
        presetFrequencies = new PresetFrequency[numOfPreset];
        presetFrequencies[0] = new PresetFrequency("normal"     ,  300,    0,    0,    0,  300);
        presetFrequencies[1] = new PresetFrequency("classical"  ,  500,  300, -200,  400,  400);
        presetFrequencies[2] = new PresetFrequency("dance"      ,  600,    0,  200,  400,  100);
        presetFrequencies[3] = new PresetFrequency("flat"       ,    0,    0,    0,    0,    0);
        presetFrequencies[4] = new PresetFrequency("folk"       ,  300,    0,    0,  200, -100);
        presetFrequencies[5] = new PresetFrequency("heavy metal",  400,  100,  900,  300,    0);
        presetFrequencies[6] = new PresetFrequency("hip hop"    ,  500,  300,    0,  100,  300);
        presetFrequencies[7] = new PresetFrequency("jazz"       ,  400,  200, -200,  200,  500);
        presetFrequencies[8] = new PresetFrequency("pop"        , -100,  200,  500,  100, -200);
        presetFrequencies[9] = new PresetFrequency("rock"       ,  500,  300, -100,  300,  500);
        presetFrequencies[10] = new PresetFrequency("custom"    ,    0,    0,    0,    0,    0);
        // Set Name list
        presetListName = new String[numOfPreset];
        for (int i = 0; i < numOfPreset; i ++)
            presetListName[i] = presetFrequencies[i].getName().toUpperCase();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_main, menu);
    }
}
