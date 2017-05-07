package com.tinkersstudio.musiccloud.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.tinkersstudio.musiccloud.R;
import com.tinkersstudio.musiccloud.controller.MusicService;
import com.tinkersstudio.musiccloud.model.Radio;
import com.tinkersstudio.musiccloud.view.RadioViewHolder;

import java.util.List;

/**
 * Created by anhnguyen on 5/3/17.
 * version 5/3/2017
 */

public class RadioListAdapter extends RecyclerView.Adapter<RadioViewHolder>  {
    String LOG_TAG = "RadioListAdapter";

    /** The application Context in which this RadioListAdapter is being used. */
    //private Context m_context;

    /** The data set to which this RadioListAdapter is bound. */
    private List<Radio> mRadioList;
    private MusicService myService;

    private View v;

    /**
     * Parameterized constructor that takes in the application Context in which
     * it is being used and the Collection of Radio objects to which it is bound.
     *
     * @param radioList
     *            The Collection of Radio objects to which this RadioListAdapter
     *            is bound.
     */
    public RadioListAdapter(List<Radio> radioList, MusicService musicService) {
        this.mRadioList = radioList;
        this.myService = musicService;
        Log.i(LOG_TAG, "construct RadioListAdapter with " + radioList.size() + " radios");
    }

    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    public int getCount() {

        Log.i(LOG_TAG, "get Count " + mRadioList.size());
        return this.mRadioList.size();

    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the com.tinkersstudio.musiccloud.adapter's
     *                 data set.
     * @return The data at the specified position.
     */
    public Object getItem(int position) {
        //Log.i(LOG_TAG, "get Item " + position);
        return this.mRadioList.get(position);
    }

    /**
     * Called when RecyclerView needs a new {@link RecyclerView.ViewHolder} of the given type to represent
     * an item.
     * <p/>
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     * <p/>
     * The new ViewHolder will be used to display items of the com.tinkersstudio.musiccloud.adapter using
     * {@link #onBindViewHolder(RecyclerView.ViewHolder, int, List)}. Since it will be re-used to display
     * different items in the data set, it is a good idea to cache references to sub views of
     * the View to avoid unnecessary {@link View#findViewById(int)} calls.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an com.tinkersstudio.musiccloud.adapter position.
     * @param viewType The com.tinkersstudio.musiccloud.view type of the new View.
     * @return A new ViewHolder that holds a View of the given com.tinkersstudio.musiccloud.view type.
     * @see #getItemViewType(int)
     * @see #onBindViewHolder(RecyclerView.ViewHolder, int)
     */
    @Override
    public RadioViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.i(LOG_TAG, "inflate new Radio");
        // create a new com.tinkersstudio.musiccloud.view
        v = LayoutInflater.from(parent.getContext())
                .inflate(viewType, parent, false);
        // set the com.tinkersstudio.musiccloud.view's size, margins, paddings and layout parameters
        RadioViewHolder vh = new RadioViewHolder(v, this);
        vh.setService(myService);
        return vh;
    }


    @Override
    public int getItemViewType(int position) {
        return R.layout.view_radio;
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link RecyclerView.ViewHolder#itemView} to reflect the item at the given
     * position.
     * <p/>
     * Note that unlike {@link ListView}, RecyclerView will not call this method
     * again if the position of the item changes in the data set unless the item itself is
     * invalidated or the new position cannot be determined. For this reason, you should only
     * use the <code>position</code> parameter while acquiring the related data item inside
     * this method and should not keep a copy of it. If you need the position of an item later
     * on (e.g. in a click listener), use {@link RecyclerView.ViewHolder#getAdapterPosition()} which will
     * have the updated com.tinkersstudio.musiccloud.adapter position.
     * <p/>
     * Override {@link #onBindViewHolder(RecyclerView.ViewHolder, int, List)} instead if Adapter can
     * handle effcient partial bind.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the com.tinkersstudio.musiccloud.adapter's data set.
     */
    @Override
    public void onBindViewHolder(RadioViewHolder holder, final int position) {

        //Log.i(LOG_TAG, "call set Radio on position " + position);
        holder.setRadio(mRadioList.get(position));
    }


    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the com.tinkersstudio.musiccloud.adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Returns the total number of items in the data set hold by the com.tinkersstudio.musiccloud.adapter.
     *
     * @return The total number of items in this com.tinkersstudio.musiccloud.adapter.
     */
    @Override
    public int getItemCount() {
        return this.mRadioList.size();
    }
}
