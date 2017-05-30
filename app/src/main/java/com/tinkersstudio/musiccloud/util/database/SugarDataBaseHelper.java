package com.tinkersstudio.musiccloud.util.database;

import com.tinkersstudio.musiccloud.util.database.sugardata.SugarGenre;
import com.tinkersstudio.musiccloud.util.database.sugardata.SugarSongInfo;

import java.util.List;

/**
 * Created by Jun Trinh on 5/29/2017.
 */

public class SugarDataBaseHelper {
    public SugarDataBaseHelper(){

    }

    /**
     * Save the item into database. Use to save the sugargenre
     * Save a single SugarGenre into databse
     * @param item SongGenre item
     */
    public static void save(SugarGenre item){
        item.save();
    }

    /**
     * Save the item into database. Use to save the sugarsonginfo
     * Save a single sugarsonginfo into the database
     * @param item SongInfo item
     */
    public static void save(SugarSongInfo item){
        item.save();
    }

    /**
     * Return the list of item based on the genre.
     * Assuming the entry always has 1 items
     * @param genre Name of the genre
     * @return list of items
     */
    public static List<SugarGenre> returnEntry(String genre)
    {
        if (genre != null)
        {
            return SugarGenre.find(SugarGenre.class,genre);
        }
        return null;
    }

    /**
     * Update the counter for the genre.
     * If the item is found-> update. If not, insert a new item.
     * @param genre
     */
    public static void updateGenreEntry(String genre)
    {
        List<SugarGenre> genres = returnEntry(genre);
        if (!genre.isEmpty())
        {
            SugarGenre item = genres.get(0);
            int counter = Integer.parseInt(item.getTime());
            counter++;
            item.setTime(Integer.toString(counter));
            item.save();

        }
        else
        {
            save(new SugarGenre(genre, Integer.toString(0)));
        }

    }

    /**
     * Delete a single genre from the database
     * @param genre
     */
    public static void deleteEntry(String genre)
    {
        List<SugarGenre> genres = returnEntry(genre);
        if (!genre.isEmpty())
        {
            SugarGenre item = genres.get(0);
            item.delete();
        }

    }

    public static void deleteAllGenre()
    {
        List<SugarGenre> genres = SugarGenre.listAll(SugarGenre.class);
        SugarGenre.deleteAll(SugarGenre.class);
    }

    public static void deleteAllSong()
    {
        List<SugarSongInfo> song = SugarSongInfo.listAll(SugarSongInfo.class);
        SugarSongInfo.deleteAll(SugarSongInfo.class);
    }


    /**
     * Save the entry into the database
     * If the item exists. Update in the database.
     * If not, call SaveEntry to save the item
     * @param genre
     */
    public static void saveEntry(String genre)
    {
        List<SugarGenre> genres = returnEntry(genre);
        //check to make sure it is not null
        if (genres != null)
        {
            if (genres.isEmpty())
            {
                SugarGenre item = new SugarGenre(genre, "0");
                save(item);
            }
            else
            {
                updateGenreEntry(genre);
            }
        }

    }
}
