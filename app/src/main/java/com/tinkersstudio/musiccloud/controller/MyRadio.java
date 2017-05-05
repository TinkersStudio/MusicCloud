package com.tinkersstudio.musiccloud.controller;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.PowerManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import com.tinkersstudio.musiccloud.model.Radio;
import com.tinkersstudio.musiccloud.R;
import com.tinkersstudio.musiccloud.util.MyFlag;

/**
 * Created by anhnguyen on 5/3/17.
 */

public class MyRadio {

    private String LOG_TAG = "MyRadio";
    private MediaPlayer radioPlayer;
    private MusicService owner;

    private boolean isPaused;
    private int currentStation = -1;

    private ArrayList<Radio> radioList;
    /**
     * Constructing a new Media Player which belong to a MusicService
     * @param owner the ownner of this Player
     */
    public MyRadio(MusicService owner) {
        this.owner = owner;
        this.getRadios();
        if (radioList.size() <= 0) {
            reloadRadioStationFromRawXML();
        }
        this.initializePlayer();
    }

    /**
     * Initialize the radio player
     */
    public void initializePlayer() {
        radioPlayer = new MediaPlayer();

        isPaused = true;

        radioPlayer.setWakeMode(owner.getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        radioPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

    }

    /**
     * Move currentStation cursor, then call playRadio
     * @param index new station to play
     */
    public void playAtIndex(int index){
        if(radioPlayer.isPlaying()) {
            radioPlayer.pause();
            this.isPaused = true;
            if (currentStation == index){
                return;
            }
        }
        currentStation = index;
        playRadio();
    }

    /**
     * Set source of radio streaming with the radio at currentStation
     * then stream radio
     */
    public void playRadio() {
        owner.setMode(MyFlag.RADIO_MODE);

        if(radioPlayer.isPlaying()){
            radioPlayer.pause();
            this.isPaused = true;
            owner.updateNotifBar(MyFlag.PLAY, radioList.get(currentStation).getName(), "Online Radio Streaming");
            return;
        }

        radioPlayer.reset();
        isPaused = false;

        try {
            radioPlayer.setDataSource(radioList.get(currentStation).getUrl());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        radioPlayer.prepareAsync();

        radioPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            public void onPrepared(MediaPlayer mp) {
                radioPlayer.start();  //start streaming
            }
        });

        owner.updateNotifBar(MyFlag.PAUSE, radioList.get(currentStation).getName(), "Online Radio Streaming");
    }

    public void playNext() {
        if (currentStation == radioList.size() - 1) {
            playAtIndex(0);
        }
        else {
            playAtIndex(currentStation + 1);
        }
    }
    public void playPrev() {
        if (currentStation == 0) {
            playAtIndex(radioList.size()-1);
        }
        else {
            playAtIndex(currentStation - 1);
        }
    }

    private void stopRadio() {
        isPaused = true;
        if (radioPlayer.isPlaying()) {
            radioPlayer.stop();
            radioPlayer.release();
            initializePlayer();
        }
    }

    /**
     * Read the xml file to retrieve Radios information, save it to the list
     * Set current Station to the first available station
     */
    private void getRadios(){
        radioList = new ArrayList<Radio>();
        Document doc = null;
        // Try to open an internal source file first
        try {
            String filePath = owner.getBaseContext().getFilesDir() + "/" + "radios.xml";
            Log.i(LOG_TAG,"FILE LOCATION: " + filePath);
            File fXmlFile = new File(filePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(fXmlFile);

        }
        // There is no internal source file, open the default file in Res
        catch (Exception e1) {
            try {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                doc = dBuilder.parse(owner.getResources().openRawResource(R.raw.radios));
            } catch (Exception e2) {
                //TODO: FireBase Crash report
                e2.printStackTrace();
            }
        }
        // Start reading data file, save into the list of radios
        finally {
            doc.getDocumentElement().normalize();

            Log.i(LOG_TAG, "Read radios.xml file :" + doc.getDocumentElement().getNodeName());
            NodeList nList = doc.getElementsByTagName("track");
            Log.i(LOG_TAG,"Found  " + nList.getLength() + " items");

            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                Log.i(LOG_TAG, "\t\t" + nNode.getNodeName() + " #" + temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    String title = eElement.getElementsByTagName("title").item(0).getTextContent();
                    String url = eElement.getElementsByTagName("location").item(0).getTextContent();
                    Log.i(LOG_TAG,"Title :" + title + "\nUrl :" + url);
                    radioList.add(new Radio(url, title));
                }
            }
        }


        if (radioList.size()>0){
            currentStation = 0;
        }
    }

    /**
     * Incase the Internal Source XML file is empty
     * Deleting the Internal data file
     * Call <method>getRadios</method> to reload station from raw xml file in res
     */
    private void reloadRadioStationFromRawXML(){
        Log.i(LOG_TAG, "reloadRadioStationFromRawXML");
        try {
            String filePath = owner.getBaseContext().getFilesDir() + "/" + "radios.xml";
            File fXmlFile = new File(filePath);
            fXmlFile.delete();
            getRadios();
        } catch (Exception e) {
            //TODO FireBase report
            e.printStackTrace();
        }
    }

    /**
     * Retrieve the list of radios
     */
    public ArrayList<Radio> getRadioList(){
        return this.radioList;
    }

    /**
     * Access the XML file of the radio and change info
     * @param index the index of that radio on the list
     * @param newUrl
     * @param newName
     */
    public void editRadio(int index,String newUrl, String newName) {

    }

    /**
     * Add new radio station to the list
     * @param aChanel
     */
    public void addRadio(Radio aChanel) {
        radioList.add(aChanel);
        rewriteXmlSource();
    }

    /**
     * delete a station in xml source, update the list of Radios
     * @param index
     */
    public void deleteStation (int index){

        // check if the current station is the one to be deleted
        if(index == currentStation && !isPaused){
            radioPlayer.pause();
            isPaused = true;
            currentStation = 0;
        }

        if(currentStation > index) {
            currentStation = currentStation -1;
        }

        // search the station, and delete it, also update the xml file
        try {
            radioList.remove(index);
            rewriteXmlSource();
        } catch (Exception e) {
            e.printStackTrace();
            //TODO report this
            Log.e(LOG_TAG, "Error on deleting");
        }

    }

    private void rewriteXmlSource(){
        Log.i(LOG_TAG, "Updating radios.xml....");
        String fileName = "radios.xml";
        String content = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                                "<playlist>\n";

        for (Radio radio : radioList) {
            content += "<track>\n";
            content += "<location>" + radio.getUrl() + "</location>\n";
            content += "<title>" + radio.getName() + "</title>\n";
            content += "</track>\n";
        }
        content += "</playlist>";

        FileOutputStream outputStream = null;
        try {
            Log.i(LOG_TAG,"write new radios.xml file");
            outputStream = owner.openFileOutput(fileName, owner.getBaseContext().MODE_PRIVATE);
            outputStream.write(content.getBytes());
            outputStream.close();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    public int getCurrentStation() {
        return currentStation;
    }

    public boolean getIsPause(){
        return isPaused;
    }
    /**
     * release the radio, ready to stop
     */
    public void releaseRadio(){
        if (radioPlayer.isPlaying()) {
            radioPlayer.stop();
            radioPlayer.release();
        }
    }
}
