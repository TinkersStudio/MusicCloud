package com.tinkersstudio.musiccloud.view;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.provider.Settings;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;

import com.tinkersstudio.musiccloud.R;

import com.tinkersstudio.musiccloud.activities.MainActivity;
import com.tinkersstudio.musiccloud.adapter.SongListAdapter;
import com.tinkersstudio.musiccloud.controller.MusicService;
import com.tinkersstudio.musiccloud.fragment.FragmentMusicPlayer;
import com.tinkersstudio.musiccloud.model.PlayList;
import com.tinkersstudio.musiccloud.model.Song;

/**
 * Created by Owner on 2/20/2017.
 */

public class SongViewHolder extends RecyclerView.ViewHolder{

    ImageView songArt;
    ImageButton playButton;
    TextView songTitle;
    TextView songSinger;
    Bitmap bitmap;
    LinearLayout pane;
    Song song;
    String LOG_TAG = "Song View Holder";
    View item;
    MusicService myService;
    SongListAdapter adapter;

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
        } catch (Exception exception) {
            Log.i(LOG_TAG, "NO COVER ART FOUND ");
            bitmap = BitmapFactory.decodeResource(item.getResources(),R.drawable.cover_art_stock);
        } finally {
            songArt.setImageBitmap(bitmap);
        }

        this.songSinger.setText(song.getArtist());
        this.songTitle.setText(song.getTitle());

        if(getAdapterPosition() == myService.getPlayer().getCurrentSongPosition()) {
            playButton.setImageResource(R.drawable.ic_action_pause);
            playButton.setColorFilter(Color.RED);
        } else {
            playButton.setImageResource(R.drawable.ic_action_play);
            playButton.setColorFilter(Color.WHITE);
        }

        playButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                myService.getPlayer().playAtIndex(getAdapterPosition());
                playButton.setImageResource(R.drawable.ic_action_pause);
                playButton.setColorFilter(Color.RED);
                adapter.notifyDataSetChanged();
            }
        });
    }
}
