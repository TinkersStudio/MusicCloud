package com.tinkersstudio.musiccloud.view;

import android.graphics.Color;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tinkersstudio.musiccloud.R;
import com.tinkersstudio.musiccloud.adapter.InfoListAdapter;
import com.tinkersstudio.musiccloud.adapter.RadioListAdapter;
import com.tinkersstudio.musiccloud.model.Radio;

/**
 * Created by anhnguyen on 5/3/17.
 */

public class RadioViewHolder extends RecyclerView.ViewHolder{

    View view;
    TextView name;
    ImageButton play;
    ImageButton delete;
    //LinearLayout station;
    RadioListAdapter adapter;

    Radio radio;

    public RadioViewHolder(View itemView, RadioListAdapter adapter) {
        super(itemView);
        view = itemView;
        this.adapter = adapter;

        initView();
    }

    private void initView(){
        name = (TextView)view.findViewById(R.id.fr_header_text);
        play = (ImageButton)view.findViewById(R.id.fr_play);
        delete = (ImageButton)view.findViewById(R.id.fr_delete);
        play.setBackgroundColor(Color.TRANSPARENT);
        delete.setBackgroundColor(Color.TRANSPARENT);

        setListener();
    }

    private void setListener(){
        play.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    play.setColorFilter(Color.RED);
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    // Play Radio & set PAUSE button
                    play.setImageResource(R.drawable.ic_action_pause);

                    play.setColorFilter(Color.WHITE);
                    return true;
                }
                return false;
            }
        });


        delete.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    delete.setColorFilter(Color.RED);
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    // delete this radio station

                    delete.setColorFilter(Color.WHITE);
                    return true;
                }
                return false;
            }
        });
    }

    public void setRadio(Radio radio){
        this.radio = radio;
        name.setText(radio.getName());
    }
}
