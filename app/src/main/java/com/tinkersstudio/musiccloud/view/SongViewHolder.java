package com.tinkersstudio.musiccloud.view;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;

import com.tinkersstudio.musiccloud.R;
import com.tinkersstudio.musiccloud.adapter.SongListAdapter;
import com.tinkersstudio.musiccloud.controller.MusicService;
import com.tinkersstudio.musiccloud.controller.MyPlayer;
import com.tinkersstudio.musiccloud.model.Song;
import com.tinkersstudio.musiccloud.util.MyFlag;

/**
 * Created by Owner on 2/20/2017.
 */

public class SongViewHolder extends RecyclerView.ViewHolder{

    private ImageView songArt;
    private ImageButton playButton;
    private TextView songTitle;
    private TextView songSinger;
    private Bitmap bitmap;
    private LinearLayout pane;
    private Song song;
    private String LOG_TAG = "Song View Holder";
    private View item;
    private MusicService myService;
    private SongListAdapter adapter;

    public SongViewHolder(View itemView, SongListAdapter adapter) {
        super(itemView);
        item = itemView;
        this.adapter = adapter;
        this.songArt = (ImageView) itemView.findViewById(R.id.song_fragment_cover_art);
        this.songTitle = (TextView) itemView.findViewById(R.id.song_fragment_song_title);
        this.songSinger = (TextView) itemView.findViewById(R.id.song_fragment_singer);
        this.playButton = (ImageButton) itemView.findViewById(R.id.song_fragment_play);
        this.pane = (LinearLayout)itemView.findViewById(R.id.song_fragment_pane);
    }

    public void setService(MusicService musicService){myService = musicService;}

    public void setSong(Song song)
    {
        this.song = song;

        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(song.getPath());
            byte[] art = retriever.getEmbeddedPicture();
            bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
            retriever.release();
        } catch (Exception exception) {
            Log.i(LOG_TAG, "NO COVER ART FOUND ");
            bitmap = BitmapFactory.decodeResource(item.getResources(),R.drawable.cover_art_stock);
        } finally {
            songArt.setImageBitmap(bitmap);
        }

        this.songSinger.setText(song.getArtist());
        this.songTitle.setText(song.getTitle());

        if(getAdapterPosition() == ((MyPlayer)myService.getPlayer(MyFlag.OFFLINE_MUSIC_MODE)).getCurrentSongPosition()
                && !myService.getPlayer(MyFlag.OFFLINE_MUSIC_MODE).getIsPause()) {
            playButton.setImageResource(R.drawable.ic_action_pause);
            pane.setBackgroundColor(item.getContext().getResources().getColor(R.color.play_red));
        } else {
            playButton.setImageResource(R.drawable.ic_action_play);
            pane.setBackgroundColor(item.getContext().getResources().getColor(R.color.tw__composer_black));
        }

        playButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                myService.toggle(MyFlag.OFFLINE_MUSIC_MODE);
                myService.getPlayer(MyFlag.OFFLINE_MUSIC_MODE).playAtIndex(getAdapterPosition());
                playButton.setImageResource(R.drawable.ic_action_pause);
                adapter.notifyDataSetChanged();
            }
        });
    }
}
