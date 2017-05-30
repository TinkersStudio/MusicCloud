package com.tinkersstudio.musiccloud.util.database.sugardata;

import com.orm.SugarRecord;
import com.orm.dsl.Unique;

/**
 * Created by Jun Trinh on 5/28/2017.
 * Table class for Song Genre
 */

public class SugarGenre extends SugarRecord {
    @Unique
    private String genre;
    private String time;

    //default for sugar record
    public SugarGenre()
    {

    }

    public SugarGenre(String genre, String time) {
        this.genre = genre;
        this.time = time;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
