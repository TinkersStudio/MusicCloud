package com.tinkersstudio.musiccloud.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.suke.widget.SwitchButton;
import com.tinkersstudio.musiccloud.R;

/**
 * Created by anhnguyen on 2/6/17.
 */

public class FragmentSetting extends Fragment {
    Context context;
    View rootView;
    com.suke.widget.SwitchButton switchButton;
    public FragmentSetting() {
        //require constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set the menu
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_setting, container, false);
        initLayout();
        initListener();
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


    public void initLayout()
    {
        switchButton = (com.suke.widget.SwitchButton)rootView.findViewById(R.id.switch_button);
        switchButton.setChecked(false);
        switchButton.setShadowEffect(false);//disable shadow effect
        switchButton.setEnabled(true);//disable button
        switchButton.setEnableEffect(true);//disable the switch animation

    }

    public void initListener()
    {
        switchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                //TODO do your job
                //switchButton.isChecked();
            }
        });

        switchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //switch state
                switchButton.toggle();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.test_menu, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //if (drawerToggle.onOptionsItemSelected(item))
        //    return true;

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
        //   return true;
        //}

        //return super.onOptionsItemSelected(item);
        return super.onOptionsItemSelected(item);
    }
}
