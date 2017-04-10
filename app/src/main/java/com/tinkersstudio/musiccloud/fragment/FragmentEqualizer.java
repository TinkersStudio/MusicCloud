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
import android.widget.Toast;

import com.tinkersstudio.musiccloud.R;
import com.xw.repo.BubbleSeekBar;

/**
 * Created by anhnguyen on 2/6/17.
 */

public class FragmentEqualizer extends Fragment {
    Context context;
    BubbleSeekBar mBubbleSeekBar1;
    BubbleSeekBar mBubbleSeekBar2;
    View viewRoot;

    public FragmentEqualizer(){
        //require an empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);

        viewRoot = inflater.inflate(R.layout.fragment_equalizer, container, false);
        initLayout();
        return viewRoot;
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
        mBubbleSeekBar1 = (BubbleSeekBar) viewRoot.findViewById(R.id.bubble_seek_bar_0);
        mBubbleSeekBar2 = (BubbleSeekBar) viewRoot.findViewById(R.id.bubble_seek_bar_1);

        mBubbleSeekBar1.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListenerAdapter() {
            @Override
            public void getProgressOnActionUp(int progress) {
                Toast.makeText(getActivity(),
                        "progressOnActionUp:" + progress,
                        Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_main, menu);
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
