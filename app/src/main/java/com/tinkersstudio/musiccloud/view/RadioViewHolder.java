package com.tinkersstudio.musiccloud.view;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.tinkersstudio.musiccloud.R;
import com.tinkersstudio.musiccloud.adapter.RadioListAdapter;
import com.tinkersstudio.musiccloud.controller.MusicService;
import com.tinkersstudio.musiccloud.controller.MyRadio;
import com.tinkersstudio.musiccloud.model.Radio;
import com.tinkersstudio.musiccloud.util.MyFlag;

import es.dmoral.toasty.Toasty;

/**
 * Created by anhnguyen on 5/3/17.
 */

public class RadioViewHolder extends RecyclerView.ViewHolder{
    private String LOG_TAG = "RadioViewHolder";
    private View view;
    private TextView name;
    private ImageButton play;
    private ImageButton addOrdelete;
    private LinearLayout station;
    private RadioListAdapter adapter;
    private MyFlag mode;
    private MusicService myService;
    private MyRadio myRadio;

    public void setService(MusicService musicService){
        myService = musicService;
        myRadio = (MyRadio)musicService.getPlayer(MyFlag.RADIO_MODE);
    }


    public RadioViewHolder(View itemView, RadioListAdapter adapter, MyFlag mode) {
        super(itemView);
        view = itemView;
        this.adapter = adapter;
        this.mode = mode;
        initView();
    }

    private void initView(){
        name = (TextView)view.findViewById(R.id.fr_header_text);
        play = (ImageButton)view.findViewById(R.id.fr_play);
        addOrdelete = (ImageButton)view.findViewById(R.id.fr_delete);
        station = (LinearLayout)view.findViewById(R.id.fr_station);
        if(mode == MyFlag.LIST_LIBRARY){
            addOrdelete.setImageResource(R.drawable.ic_action_plus2);
            addOrdelete.setBackgroundColor(Color.TRANSPARENT);
            addOrdelete.setColorFilter(Color.WHITE);
            play.setEnabled(false);
            play.setColorFilter(view.getContext().getResources().getColor(R.color.tw__composer_black));
            play.setBackgroundColor(Color.TRANSPARENT);
            station.setBackgroundColor(view.getContext().getResources().getColor(R.color.tw__composer_black));
        } else if (mode == MyFlag.LIST_FAVORITE) {
            play.setBackgroundColor(Color.TRANSPARENT);
            play.setColorFilter(Color.WHITE);
            addOrdelete.setBackgroundColor(Color.TRANSPARENT);
            addOrdelete.setColorFilter(Color.WHITE);
        }
    }

    public void setRadio(final Radio radio) {
        //Log.i(LOG_TAG, "seting radio : " + radio.getName());
        name.setText(radio.getName());

        //Log.i(LOG_TAG,"adapterPos: " + getAdapterPosition() + " , stationPos: " + myRadio.getCurrentStation() + !myRadio.getIsPause());

        if (mode == MyFlag.LIST_FAVORITE) {
            if (getAdapterPosition() == myRadio.getCurrentStation() &&
                    !myRadio.getIsPause()) {
                play.setImageResource(R.drawable.ic_action_pause);
                station.setBackgroundColor(station.getContext().getResources().getColor(R.color.play_red));
            } else {
                play.setImageResource(R.drawable.ic_action_play);
                addOrdelete.setColorFilter(Color.WHITE);
                station.setBackgroundColor(view.getContext().getResources().getColor(R.color.tw__composer_black));
            }
        }
        play.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                myService.toggle(MyFlag.RADIO_MODE);
                //TODO: handle not playable
                myRadio.playAtIndex(getAdapterPosition());
                play.setImageResource(R.drawable.ic_action_pause);
                adapter.notifyDataSetChanged();
                name.setSelected(true);
            }
        });

        addOrdelete.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

             }
        });
        // Open a new fragment when click on info button
        addOrdelete.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    addOrdelete.setColorFilter(Color.RED);
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    addOrdelete.setColorFilter(Color.WHITE);
                    if(mode == MyFlag.LIST_FAVORITE){
                        myRadio.deleteStation(getAdapterPosition());
                        adapter.notifyDataSetChanged();
                        //Toasty.info(view.getContext(), "Remove Station " + radio.getName(), Toast.LENGTH_SHORT, true).show();
                    } else if(mode == MyFlag.LIST_LIBRARY) {
                        myRadio.addRadio(getAdapterPosition());
                        adapter.updateFavoriteList();
                        Toasty.info(view.getContext(), "Add to My Station\n" + radio.getName(), Toast.LENGTH_SHORT, true).show();
                        addOrdelete.setEnabled(false);
                    }
                    return true;
                }
                return false;
            }

        });
    }
}
