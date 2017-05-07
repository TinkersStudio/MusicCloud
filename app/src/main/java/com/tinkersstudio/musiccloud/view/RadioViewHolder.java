package com.tinkersstudio.musiccloud.view;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.tinkersstudio.musiccloud.R;
import com.tinkersstudio.musiccloud.adapter.RadioListAdapter;
import com.tinkersstudio.musiccloud.controller.MusicService;
import com.tinkersstudio.musiccloud.controller.MyRadio;
import com.tinkersstudio.musiccloud.model.Radio;
import com.tinkersstudio.musiccloud.util.MyFlag;

/**
 * Created by anhnguyen on 5/3/17.
 */

public class RadioViewHolder extends RecyclerView.ViewHolder{
    private String LOG_TAG = "RadioViewHolder";
    private View view;
    private TextView name;
    private ImageButton play;
    private ImageButton delete;
    //LinearLayout station;
    private RadioListAdapter adapter;

    private MusicService myService;

    private Radio radio;

    public void setService(MusicService musicService){myService = musicService;}

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
    }

    public void setRadio(Radio radio){
        //Log.i(LOG_TAG, "seting radio : " + radio.getName());
        this.radio = radio;
        name.setText(radio.getName());
        final MyRadio myRadio = ((MyRadio)myService.getPlayer(MyFlag.RADIO_MODE));

        Log.i(LOG_TAG,"adapterPos: " + getAdapterPosition() + " , stationPos: " + myRadio.getCurrentStation() + !myRadio.getIsPause());

        if(getAdapterPosition() == myRadio.getCurrentStation() &&
                !myRadio.getIsPause()) {
            play.setImageResource(R.drawable.ic_action_pause);
            play.setColorFilter(Color.RED);
        } else {
            play.setImageResource(R.drawable.ic_action_play);
            play.setColorFilter(Color.WHITE);
            delete.setColorFilter(Color.WHITE);
        }

        play.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                myService.toggle(MyFlag.RADIO_MODE);
                myRadio.playAtIndex(getAdapterPosition());
                play.setImageResource(R.drawable.ic_action_pause);
                play.setColorFilter(Color.RED);
                adapter.notifyDataSetChanged();
                name.setSelected(true);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                delete.setColorFilter(Color.RED);
                myRadio.deleteStation(getAdapterPosition());
                adapter.notifyDataSetChanged();
            }
        });
    }
}
