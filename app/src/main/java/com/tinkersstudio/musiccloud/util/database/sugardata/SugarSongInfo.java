package com.tinkersstudio.musiccloud.util.database.sugardata;

import com.orm.SugarRecord;
import com.orm.dsl.Unique;

/**
 * Created by Jun Trinh on 5/28/2017.
 * Table class for the song info
 */

public class SugarSongInfo extends SugarRecord{

    @Unique
    private String title;
    private String artist;
    private String album;
    private String author;
    private String duration;
    private String bitrate;
    private String genre;
    private String year;
    private String modified;
    private String size;

    // Default constructor is necessary for SugarRecord
    public SugarSongInfo()
    {

    }


    public SugarSongInfo(String title, String artist, String album, String author, String duration,
                         String bitrate, String genre, String year, String modified, String size) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.author = author;
        this.duration = duration;
        this.bitrate = bitrate;
        this.genre = genre;
        this.year = year;
        this.modified = modified;
        this.size = size;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getBitrate() {
        return bitrate;
    }

    public void setBitrate(String bitrate) {
        this.bitrate = bitrate;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getModified() {
        return modified;
    }

    public void setModified(String modified) {
        this.modified = modified;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}
