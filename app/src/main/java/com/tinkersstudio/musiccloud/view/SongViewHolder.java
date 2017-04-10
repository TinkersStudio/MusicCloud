package com.tinkersstudio.musiccloud.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tinkersstudio.musiccloud.R;

import com.tinkersstudio.musiccloud.model.Song;

/**
 * Created by Owner on 2/20/2017.
 */

public class SongViewHolder extends RecyclerView.ViewHolder{

    ImageView songArt;
    TextView songTitle;
    TextView songSinger;
    Song song;

    public SongViewHolder(View itemView) {
        super(itemView);
        this.songArt = (ImageView) itemView.findViewById(R.id.song_fragment_cover_art);
        this.songTitle = (TextView) itemView.findViewById(R.id.song_fragment_song_title);
        this.songSinger = (TextView) itemView.findViewById(R.id.song_fragment_singer);
    }

    public void setSong(Song song)
    {
        this.song = song;
        this.songSinger.setText(song.getArtist());
        this.songTitle.setText(song.getTitle());
    }
}
