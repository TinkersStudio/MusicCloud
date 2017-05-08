package com.tinkersstudio.musiccloud.controller;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

public class MyRadio implements Player, MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener{

    private String LOG_TAG = "MyRadio";
    private MediaPlayer radioPlayer;
    private static MusicService owner;

    private boolean isPaused;
    private static int currentStation = -1;

    private static boolean infoReady = false;
    private static boolean currentStationChanged = false;
    private static String currentStationName = "";
    private static String currentStationGenre = "";
    private static String currentStationStatus = "";
    private static String currentStationDescription = "";
    private static String currentTitle = "";
    private static String currentArtist = "";
    private static String currentSource = "";
    private static String currentBitrate = "";


    private static ArrayList<Radio> radioList;
    /**
     * Constructing a new Media Player which belong to a MusicService
     * @param owner the ownner of this Player
     */
    public MyRadio(MusicService owner) {
        this.owner = owner;
        this.getDataSource();
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
        radioPlayer.setOnErrorListener(this);
        radioPlayer.setOnCompletionListener(this);
        radioPlayer.setOnInfoListener(this);
    }

    /**
     * Move currentStation cursor, then call playRadio
     * @param index new station to play
     */
    public void playAtIndex(int index){
        if(radioPlayer.isPlaying()) {
            radioPlayer.pause();
            this.isPaused = true;
            //User mean to pause the player
            if (currentStation == index){
                return;
            }
        }
        currentStation = index;
        currentStationChanged = true;
        playCurrent();
    }

    /**
     * Set source of radio streaming with the radio at currentStation
     * then stream radio
     */
    public void playCurrent() {
        owner.setMode(MyFlag.RADIO_MODE);
        owner.updateNotifBar(MyFlag.PAUSE, radioList.get(currentStation).getName(), "Loading Stream .......");

        if(radioPlayer.isPlaying()){
            radioPlayer.pause();
            this.isPaused = true;
            owner.updateNotifBar(MyFlag.PLAY, currentStationName, currentStationGenre);
            return;
        }

        radioPlayer.reset();
        isPaused = false;

        try {
            radioPlayer.setDataSource(radioList.get(currentStation).getUrl());

            radioPlayer.prepareAsync();

            radioPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                public void onPrepared(MediaPlayer mp) {
                    radioPlayer.start();  //start streaming
                }
            });

            // fetch data from server to update notification bar
            fetch(radioList.get(currentStation).getUrl());
        } catch (Exception e) {
            // IllegalArgumentException , IOException, IllegalStateException
            // Are all because of Error setting data source (bad url)
            e.printStackTrace();
            currentStationName = radioList.get(currentStation).getName();
            currentStationGenre = "";
            currentStationStatus = "Url not accessible";
            currentStationDescription = "";
            currentTitle = "";
            currentArtist = "";
            currentSource = radioList.get(currentStation).getUrl();
            owner.updateNotifBar(MyFlag.PAUSE, currentStationName, currentStationStatus);
            infoReady = true;
        }
    }

    public void seekNext(boolean wasPlaying) {
        if (wasPlaying) {
            playAtIndex((currentStation == radioList.size() - 1) ? 0 : currentStation + 1);
        }
        else {
            radioPlayer.pause();
            currentStation = (currentStation == radioList.size() - 1) ? 0 : currentStation + 1;
            currentStationChanged = true;
            owner.updateNotifBar(MyFlag.PLAY,
                    radioList.get(currentStation).getName(), radioList.get(currentStation).getUrl());
        }
    }
    public void seekPrev(boolean wasPlaying) {
        if (wasPlaying) {
            playAtIndex((currentStation == 0) ? radioList.size()-1 : currentStation - 1);
        }
        else {
            radioPlayer.pause();
            currentStation = (currentStation == 0) ? radioList.size()-1 : currentStation - 1;
            currentStationChanged = true;
            owner.updateNotifBar(MyFlag.PLAY,
                    radioList.get(currentStation).getName(), radioList.get(currentStation).getUrl());
        }

    }

    /**
     * Stop streaming radio
     */
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
    public int getDataSource(){
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
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    String title = eElement.getElementsByTagName("title").item(0).getTextContent();
                    String url = eElement.getElementsByTagName("location").item(0).getTextContent();
                    Log.i(LOG_TAG,"\t" + nNode.getNodeName() + " #" + temp + ": Title :" + title + " | Url :" + url);
                    radioList.add(new Radio(url, title));
                }
            }
        }


        if (radioList.size()>0){
            currentStation = 0;
        }
        return radioList.size();
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
            getDataSource();
        } catch (Exception e) {
            //TODO FireBase report
            e.printStackTrace();
        }
    }

    /**
     *  Make an AsyncTask to Retrieve Metadata from Server
     */
    public void fetch(String serverUrl) {
        //Log.i(LOG_TAG, "Make Asynctask to fetch meta data");
        new FetchMetaDataTask().execute(serverUrl);
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
     *
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

    /**
     * Update the xml source file
     */
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

    /**
     * pause the radio.
     * For Streaming, pause is actually stop streaming
     */
    public void pausePlayer(){
        this.stopRadio();
    }

    /**
     * release the radio, ready to stop
     */
    public void releaseRadio(){
        if (radioPlayer.isPlaying()) {
            radioPlayer.stop();
            radioPlayer.release();
        }
        else {
            radioPlayer.release();
        }
    }

    /**
     * Stop the radio player when a stream is completed
     * @param mp
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        radioPlayer.stop();
    }

    /**
     * Error while streaming
     * @param mp
     * @param what
     * @param extra
     * @return
     */
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        StringBuilder sb = new StringBuilder();
        sb.append("Media Player Error: ");
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                sb.append("Not Valid for Progressive Playback");
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                currentStationStatus = "Server is currently unavailable";
                infoReady = true;
                sb.append("Server Died");
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                sb.append("Unknown");
                break;
            default:
                sb.append(" Non standard (");
                sb.append(what);
                sb.append(")");
        }
        sb.append(" (" + what + ") ");
        sb.append(extra);
        Log.e(LOG_TAG, sb.toString());
        return true;
    }

    /**
     * refereces <link>https://developer.android.com/reference/android/media/MediaPlayer.html#MEDIA_INFO_METADATA_UPDATE</link>
     * @param mp
     * @param what
     * @param extra
     * @return
     */
    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
            // MediaPlayer is temporarily pausing playback internally in order to buffer more data.
            case MediaPlayer.MEDIA_INFO_BUFFERING_START :
                Log.i(LOG_TAG, "got info from server : BUFFERING_START");
                currentStationStatus = "Buffering....";
                owner.updateNotifBar(MyFlag.PAUSE, currentStationName, "buffering...");
                infoReady = true;
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END :
                Log.i(LOG_TAG, "got info from server : BUFFERING_END");
                currentStationStatus = currentBitrate + " Kbps";
                owner.updateNotifBar(MyFlag.PAUSE, currentStationName, currentTitle + " - " + currentArtist);
                infoReady = true;
                break;
            case MediaPlayer.MEDIA_INFO_METADATA_UPDATE:
                Log.i(LOG_TAG, "got info from server : METADATA_UPDATE");
                fetch(radioList.get(currentStation).getUrl());
                break;
            case MediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE:
                Log.i(LOG_TAG, "got info from server : UNSUPPORTED_SUBTITLE");
                break;
            case MediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT:
                Log.i(LOG_TAG, "got info from server : SUBTITLE_TIMED_OUT");
                break;
            case 703: //MediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH
                Log.i(LOG_TAG, "got info from server : NETWORK_BANDWIDTH");
                currentStationStatus = "slow connection...";
                infoReady = true;
                break;
            default:
                Log.i(LOG_TAG, "got info from server : " + what + ", " + extra);
                break;
        }
        return true;
    }

    public String getFirstTitle(){
        return this.getCurrentStationName();
    }
    public String getSecondTitle(){
        return (getCurrentTitle() + " - " + getCurrentArtist());
    }
    /**
     * Retrieve the list of radios
     */
    public ArrayList<Radio> getRadioList(){
        return this.radioList;
    }

    public static String getCurrentArtist() {
        return currentArtist;
    }

    public static String getCurrentTitle() {
        return currentTitle;
    }

    public static String getCurrentStationDescription() {
        return currentStationDescription;
    }

    public static String getCurrentStationStatus() {
        return currentStationStatus;
    }

    public static String getCurrentStationGenre() {
        return currentStationGenre;
    }

    public static String getCurrentStationName() {
        return currentStationName;
    }

    public static String getCurrentSource() {
        return currentSource;
    }

    public int getCurrentStation() {
        return currentStation;
    }

    public void setCurrentStationChanged() {currentStationChanged = false;}
    public boolean getCurrentStationChanged() { return currentStationChanged;}

    /**
     * Tell client that info from fetching is ready to use
     * @return
     */
    public boolean isInfoReady() {
        return infoReady;
    }

    /**
     * After client pickup info from fetching, reset the flag
     * @param infoReady
     */
    public void setInfoReady(boolean infoReady) {
        this.infoReady = infoReady;
    }

    public int getAudioSessionId(){return radioPlayer.getAudioSessionId();}

    public void setVolume(float left, float right) {
        radioPlayer.setVolume(left,right);
    }

    public boolean getIsPause(){
        //Log.i(LOG_TAG, "MyRadio isPause = " + isPaused );
        return isPaused;
    }

    /**------------------------------------------------------------------------------------------
     * an AsyncTask to fetch data from server
     */
    public static class FetchMetaDataTask extends AsyncTask<String, Void, String> {

        private String LOG_Task = "FetchMetaDataTask";
        private URL url;
        private URLConnection conn;
        private Map<String, List<String>> hList;
        private String success = "success";
        private String connError = "Network Error, connection fail...";
        private String fail = "Fail to Stream";

        /**
         * Open a connection to the streaming server (by url)
         * Make request for metadata from the server for that specific stream
         * Extract stream info in the return header, and metadata in middle of stream
         * @param serverUrls is the URL to the server
         * @return
         */
        protected String doInBackground(String... serverUrls) {
            // Reset metadata to make sure it's not holding previous station's data
            currentStationName = radioList.get(currentStation).getName();
            currentStationGenre = "";
            currentStationStatus = "";
            currentStationDescription = "";
            currentTitle = "";
            currentArtist = "";
            currentSource = serverUrls[0];

            try {
                //Open connection to streamming server with a specific stream URL
                //Log.i(LOG_Task, "Open connection....");
                url = new URL(serverUrls[0]);
                conn = url.openConnection();

                //Request for Icy-MetaData
                conn.addRequestProperty("Icy-MetaData", "1");
                conn.connect();

                // Retrieve and print the metadata just got from streaming server
                //Log.i(LOG_Task, "Connect success, printing metadata in return header from server...");
                hList = conn.getHeaderFields();
                // The headerFields is a Map<String, List<String>>
                for (Map.Entry<String, List<String>> entry : hList.entrySet()) {
                    String key = entry.getKey();
                    List<String> values = entry.getValue();
                    // Get the interested general stream information
                    if (key != null) { // could be null key
                        switch (key) {
                            case "icy-br":
                                Log.i(LOG_Task, "BiteRate: " + values.get(0));
                                currentBitrate = values.get(0);
                                currentStationStatus = currentBitrate + " Kbps";
                                break;
                            case "icy-description":
                                Log.i(LOG_Task, "Description: " + values.get(0));
                                currentStationDescription = values.get(0);
                                break;
                            case "icy-name":
                                Log.i(LOG_Task, "StationName: " + values.get(0));
                                currentStationName = values.get(0);
                                break;
                            case "icy-genre":
                                Log.i(LOG_Task, "StationGenre: " + values.get(0));
                                currentStationGenre = values.get(0);
                                break;
                            case "icy-url":
                                Log.i(LOG_Task, "URL: " + values.get(0));
                                currentSource = values.get(0);
                                break;
                            // read metadata in the middle of the stream
                            case "icy-metaint":
                                //Log.i(LOG_Task, "MetaInt: " + values.get(0));
                                readMetaData(Integer.parseInt(hList.get("icy-metaint").get(0) + ""));
                                break;
                        }
                    }
                }
            } catch (java.net.ConnectException e1) {
                return connError;
            } catch (IOException e2) {
                //Just some error reading metadata, we still sucess in reading header
                return success;
            } catch (Exception e3) {
                e3.printStackTrace();
                return fail;
            }

            return success;
        }

        /**
         * Get the stream, getting to the middle of the stream to extract metadata
         * @param location is the location of the metadata block in the stream
         * @throws IOException
         */
        private void readMetaData(int location) throws IOException {
            //Log.i(LOG_Task, "/readMetaData");

            long read = 0;
            String md = ""; // The metadata

            // Skipping stream until get to the metadata
            InputStream is = conn.getInputStream();
            while (read != location) { read += is.skip(location - read);}

            // The first byte of metadata tell how large the metadata is
            int mdl = is.read() * 16;
            //Log.i(LOG_Task, "Skipped " + read + " bytes, reading " + mdl + " next bytes for metadatas");

            // Start reading metadata if there is some
            if (mdl > 0) {
                read = 0;
                // try to read metadata 3 times at most, because the stream might be corrupted
                for (int i = 0; i < 3; i++){
                    byte[] mdb = new byte[mdl - (int) read];
                    read = is.read(mdb);
                    md += new String(mdb, 0, mdb.length);
                    //Log.i(LOG_Task, "read:" + read + "/" + mdl + "bytes");
                    if (read==mdl) {break;} // Got enough needed data
                }
                //Log.i(LOG_Task, "raw metadata: " + md);

                // Get the chunks of metadata
                String[] metdatas = new String(md.trim().getBytes(), "utf-8").split(";");

                // Extract metadata in form     StreamTitle='DELTA GOODREM - BORN TO TRY';
                for (String data : metdatas) {
                    //Log.i(LOG_Task, data);
                    String[] item = data.split("-|=");
                    // Extract the data line contains StreamTitle (replace "" to avoid this: StreamT��itle)
                    if (item[0].replaceAll("[^\\p{Alnum}\\s]", "").compareTo("StreamTitle") == 0) {
                        currentTitle = item[1].replaceAll("[^\\p{Alnum}\\s]", "");
                        currentArtist = (item.length == 3) ? item[2].replaceAll("[^\\p{Alnum}\\s]", "") : "";
                    }
                }
                Log.i(LOG_Task, "Title: " + currentTitle + " | Artist: " + currentArtist);

            }
            is.close();
        }

        /**
         * After retrieving all needed metadata, set the view of the notification bar
         * @param result is the result of preExecute
         */
        @Override
        protected void onPostExecute(String result) {
            Log.i(LOG_Task, "onPostExecute " + this.getStatus().toString() + " arg " + result);
            super.onPostExecute(result);
            infoReady = true; // turn on flag saying info is all set and ready to be use

            // Need to setup Notif Bar if the preExecute was failing
            if (result.compareTo(connError) == 0) {
                owner.updateNotifBar(MyFlag.PLAY, radioList.get(currentStation).getName(), connError);
                return;
            } else if (result.compareTo(fail) == 0) {
                owner.updateNotifBar(MyFlag.PLAY, radioList.get(currentStation).getName(), fail);
                return;
            }
            // Set up the view of notification bar
            else if(currentTitle.compareTo("")!=0) {
                if (currentArtist.compareTo("")!=0) {
                    owner.updateNotifBar(MyFlag.PAUSE, currentStationName, currentTitle + " - " + currentArtist);
                } else {
                    owner.updateNotifBar(MyFlag.PAUSE, currentStationName, currentTitle);
                }
            }
            owner.setNotificationBar(MyFlag.PAUSE, currentStationName, currentStationGenre);
        }
    }

}
