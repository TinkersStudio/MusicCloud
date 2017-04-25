package com.tinkersstudio.musiccloud.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.tinkersstudio.musiccloud.adapter.InfoListAdapter;
import com.tinkersstudio.musiccloud.model.Info;
import com.tinkersstudio.musiccloud.R;

/**
 * Created by anhnguyen on 4/25/17.
 */

public class SongInfoViewHolder extends RecyclerView.ViewHolder {

    View view;
    TextView attribute;
    TextView value;

    public SongInfoViewHolder(View itemView, InfoListAdapter adapter) {
        super(itemView);
        view = itemView;

        initView();
    }

    private void initView(){
        this.attribute = (TextView) view.findViewById(R.id.si_detail_attribute);
        this.value = (TextView) view.findViewById(R.id.si_detail_value);
    }

    public void setInfo(Info info){
        this.attribute.setText(info.getAttribute());
        this.value.setText(info.getValue());
    }
}
