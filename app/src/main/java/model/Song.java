package model;

/**
 * Created by Owner on 2/10/2017.
 */

public class Song {
    private Long id;
    private String title;
    private String artist;
    private Long albumID;


    public Song(long songID, String songTitle, String songArtist, long albumID) {
        id=songID;
        title=songTitle;
        artist=songArtist;
        this.albumID = albumID;
    }

    public Long getID(){return id;}
    public String getTitle(){return title;}
    public String getArtist(){return artist;}
    public Long getAlbumArt(){return albumID;}
}
