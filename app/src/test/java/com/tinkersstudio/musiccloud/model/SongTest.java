package com.tinkersstudio.musiccloud.model;

/**
 * Created by Jun on 2/22/2017.
 */
import static org.junit.Assert.*;

import org.junit.Test;

import com.tinkersstudio.musiccloud.model.Song;
/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class SongTest {
    @Test
    public void testGetSongId() {
        Song newSong = new Song(1, "Forever Love", "Sia", 1, "path");
        // use this method because float is not precise
        assertEquals("Failed to get songId", 1, newSong.getID(), 0.000);
    }

    @Test
    public void testGetSongTittle() {
        Song newSong = new Song(1, "Forever Love", "Sia", 1, "path");
        // use this method because float is not precise
        assertSame("Failed to get song title", "Forever Love", newSong.getTitle());
    }

    @Test
    public void testGetSongAlbum() {
        Song newSong = new Song(1, "Forever Love", "Sia", 1, "path");
        // use this method because float is not precise
        assertEquals("Failed to get songId", 1, newSong.getAlbumArt(), 0.000);
    }

    @Test
    public void testGetSongArtist() {
        Song newSong = new Song(1, "Forever Love", "Sia", 1, "path");
        // use this method because float is not precise
        assertSame("Failed to get song title", "Sia", newSong.getArtist());
    }

}
