package controller;

/**
 * Created by anhnguyen on 2/12/17.
 */

public class NoSongToPlayException extends RuntimeException {

    public NoSongToPlayException(){super();}
    public NoSongToPlayException(String msg){super(msg);}
}
