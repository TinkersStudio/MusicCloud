package com.tinkersstudio.musiccloud.controller;

/**
 * Created by anhnguyen on 2/11/17.
 * 4/21/2017 implements methods
 */

/**
 * convert time a progress in order to display text, or set the position for player, seekbar
 */
public class TimeConverter {

    /**
     * Conver time from millisenconds to String
     * @param milliseconds total time in milliseconds
     * @return a string in form [hh:]mm:ss
     */
    public static String milliSecondsToTimeString(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int)( milliseconds / (1000*60*60));
        int minutes = (int)(milliseconds % (1000*60*60)) / (1000*60);
        int seconds = (int) ((milliseconds % (1000*60*60)) % (1000*60) / 1000);
        // Add hours if there
        if(hours > 0){
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if(seconds < 10){
            secondsString = "0" + seconds;
        }else{
            secondsString = "" + seconds;}

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

    /**
     * Having the current position, and total position, calculate the percentage
     * @param currentDuration
     * @param totalDuration
     * @return the integer indicate the percentage
     */
    public static int currentDurationToPercentage(long currentDuration, long totalDuration) {
        Double percentage = (double) 0;

        // calculating percentage
        percentage =(((double)currentDuration)/totalDuration)*100;

        // return percentage
        return percentage.intValue();
    }


    public static int percentageToCurrentDuration(int progress, long totalDuration) {
        // return current duration in milliseconds
        return (int) ((((double)progress) / 100) * totalDuration);
    }
}
