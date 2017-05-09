package com.tinkersstudio.musiccloud.controller;

/**
 * Created by anhnguyen on 5/6/17.
 */

public interface Player {

    // Getting the data from storage
    public int getDataSource();
    public boolean getIsPause();
    public void pausePlayer();
    public void playCurrent();
    public void playAtIndex(int index);
    public void seekNext(boolean wasPlaying);
    public void seekPrev(boolean wasPlaying);
    public String getFirstTitle();
    public String getSecondTitle();
    public int getAudioSessionId();
    public void setVolume(float left, float right);
}
