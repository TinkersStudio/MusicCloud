package com.tinkersstudio.musiccloud.model;

import android.util.Log;
/**
 * Created by anhnguyen on 5/3/17.
 */

public class Radio {
    private String url;
    private String name;
    private String genre;

    public Radio (String url, String name) {
        this.url = url;
        this.name = name;
        //this.genre = genre;
    }

    public String getUrl() {
        return url;
    }

    public String getGenre() {
        return genre;
    }

    public String getName() {
        return name;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }
}
