package com.tinkersstudio.musiccloud.controller;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;
import android.net.NetworkInfo;
import android.net.ConnectivityManager;
import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.net.URL;
import java.net.URLConnection;

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
    private static boolean currentStationChanged = false;

    private static boolean infoReady = false;
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
            infoReady = true;
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

        // Case PAUSE
        if(radioPlayer.isPlaying()){
            radioPlayer.pause();
            this.isPaused = true;
            currentStationStatus = "stop";
            notifyUser(MyFlag.PLAY, currentStationName, currentStationGenre);
            return;
        }

        // Otherwise, case PLAY
        // Check for internet connection and update UI before streaming
        resetAllInfo();
        if(isOnline()) {
            currentStationStatus = "Loading Stream...";
            notifyUser(MyFlag.PAUSE, currentStationName, currentStationStatus);
        } else {
            currentStationStatus = "No Internet Connection";
            notifyUser(MyFlag.PAUSE, currentStationName, currentStationStatus);
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
            //e.printStackTrace();
            resetAllInfoExceptBitrate();
            currentStationStatus =  "Url not accessible";
            notifyUser(MyFlag.PAUSE, currentStationName, currentStationStatus);
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
            notifyUser(MyFlag.PLAY, radioList.get(currentStation).getName(), radioList.get(currentStation).getUrl());
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
            notifyUser(MyFlag.PLAY, radioList.get(currentStation).getName(), radioList.get(currentStation).getUrl());
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
     *  Make an AsyncTask to Retrieve Metadata from Server
     */
    private void fetch(String serverUrl) {
        if(isOnline()) {
            currentStationStatus = "Loading Stream...";
            notifyUser(MyFlag.PAUSE, currentStationName, currentStationStatus);
        } else {
            currentStationStatus = "No Internet Connection";
            notifyUser(MyFlag.PAUSE, currentStationName, currentStationStatus);
            return;
        }
        //Log.i(LOG_TAG, "Make Asynctask to fetch meta data");
        new FetchMetaDataTask().execute(serverUrl);
    }

    /**
     * Access the XML file of the radio and change info
     * @param index the index of that radio on the old list
     */
    public void editRadio(int index,String newUrl, String newName) {
        radioList.set(index, new Radio(newUrl, newName));
        rewriteXmlSource();
    }

    /**
     * Add new radio station to the list
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

    @Override
    public void onCompletion(MediaPlayer mp) {
        radioPlayer.stop();
    }

    /**
     * Error while streaming
     * @return true if we do handle this
     */
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.i(LOG_TAG, "Stream Error: Not Valid for Progressive Playback");
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                currentStationStatus = "erver is currently unavailable";
                infoReady = true;
                Log.i(LOG_TAG, "Stream Error: Server Died");
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.i(LOG_TAG, "Stream Error: Unknown");
                break;
            default:
                Log.i(LOG_TAG, "Stream Error Non standard (" + what + ")");
        }
        return true;
    }

    /**
     * references
     * https://developer.android.com/reference/android/media/MediaPlayer.html#MEDIA_INFO_METADATA_UPDATE
     * @return true if we do handle this
     */
    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
            // MediaPlayer is temporarily pausing playback internally in order to buffer more data.
            case MediaPlayer.MEDIA_INFO_BUFFERING_START :
                //Log.i(LOG_TAG, "got info from server : BUFFERING_START");
                currentStationStatus = "Buffering....";
                notifyUser(MyFlag.PAUSE, currentStationName, "buffering...");
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END :
                //Log.i(LOG_TAG, "got info from server : BUFFERING_END");
                currentStationStatus = currentBitrate + " Kbps";
                notifyUser(MyFlag.PAUSE, currentStationName, currentTitle + " - " + currentArtist);
                break;
            case MediaPlayer.MEDIA_INFO_METADATA_UPDATE:
                Log.i(LOG_TAG, "got info from server : METADATA_UPDATE"); // Never got this Info
                fetch(radioList.get(currentStation).getUrl());
                break;
            case MediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE:
                Log.i(LOG_TAG, "got info from server : UNSUPPORTED_SUBTITLE");
                break;
            case MediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT:
                Log.i(LOG_TAG, "got info from server : SUBTITLE_TIMED_OUT");
                break;
            case 703: //MediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH
                //Log.i(LOG_TAG, "got info from server : NETWORK_BANDWIDTH");
                notifyUser("slow connection...");
                break;
            default:
                Log.i(LOG_TAG, "got info from server : " + what + ", " + extra);
                break;
        }
        return true;
    }

    /**-----------------------------------------------------------------------------------------
     *                      GETTER and SETTER               */
    public String getFirstTitle(){
        return this.getCurrentStationName();
    }
    public String getSecondTitle(){
        return (getCurrentTitle() + " - " + getCurrentArtist());
    }
    //Retrieve the list of radios
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
    public static void resetAllInfo(){resetAllInfoExceptBitrate();currentBitrate = "";}
    public boolean isInfoReady() {
        return infoReady;
    }
    public void setInfoReady(boolean infoReady) {
        this.infoReady = infoReady;
    }
    public int getAudioSessionId(){return radioPlayer.getAudioSessionId();}
    public void setVolume(float left, float right) {
        radioPlayer.setVolume(left,right);
    }
    public boolean getIsPause(){return isPaused;}

    private static void resetAllInfoExceptBitrate(){
        currentStationName = radioList.get(currentStation).getName();
        currentStationGenre = "";
        currentStationDescription = "";
        currentTitle = "";
        currentArtist = "";
        currentSource = radioList.get(currentStation).getUrl();
    }
    //Check for Wifi or Mobile internet connection
    private boolean isOnline() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) owner.getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    private static void notifyUser(MyFlag flag, String first, String second ) {
        owner.updateNotifBar(flag, first, second);
        infoReady = true;
    }
    private static void notifyUser(String status) {
        currentStationStatus = status;
        infoReady = true;
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
        private String metaError = "No Track Info";

        /**
         * Open a connection to the streaming server (by url)
         * Make request for metadata from the server for that specific stream
         * Extract stream info in the return header, and metadata in middle of stream
         * @param serverUrls is the URL to the server
         */
        protected String doInBackground(String... serverUrls) {

            try {
                //Open connection to streamming server with a specific stream URL
                conn = new URL(serverUrls[0]).openConnection();
                //Request for Icy-MetaData
                conn.addRequestProperty("Icy-MetaData", "1");
                conn.connect();
                // Retrieve and print the metadata just got from streaming server
                //Log.i(LOG_Task, "Connect success, printing metadata in return header from server...");
                hList = conn.getHeaderFields();
                // Reset metadata to make sure it's not holding previous station's data
                resetAllInfo();
                // The headerFields is a Map<String, List<String>>
                for (Map.Entry<String, List<String>> entry : hList.entrySet()) {
                    String key = entry.getKey();
                    List<String> values = entry.getValue();
                    // Get the interested general stream information
                    if (key != null) { // could be null key
                        switch (key) {
                            case "icy-br":
                                //Log.i(LOG_Task, "BiteRate: " + values.get(0));
                                currentBitrate = values.get(0);
                                currentStationStatus = currentBitrate + " Kbps";
                                break;
                            case "icy-description":
                                //Log.i(LOG_Task, "Description: " + values.get(0));
                                currentStationDescription = values.get(0);
                                break;
                            case "icy-name":
                                //Log.i(LOG_Task, "StationName: " + values.get(0));
                                currentStationName = values.get(0);
                                break;
                            case "icy-genre":
                                //Log.i(LOG_Task, "StationGenre: " + values.get(0));
                                currentStationGenre = values.get(0);
                                break;
                            case "icy-url":
                                //Log.i(LOG_Task, "URL: " + values.get(0));
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
            } catch (java.net.UnknownHostException e4) {
                currentStationStatus = connError;
                //e4.printStackTrace();
                return connError;
            }
            catch (java.net.ConnectException e1) {
                //e1.printStackTrace();
                currentStationStatus = connError;
                return connError;
            } catch (IOException e2) {
                //e2.printStackTrace();
                currentTitle = metaError;
                return metaError;
            }
            catch (Exception e3) {
                //e3.printStackTrace();
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
            super.onPostExecute(result);

            //  setup Notif Bar base on preExecute
            if (result.compareTo(connError) == 0) {
                notifyUser(MyFlag.PLAY, radioList.get(currentStation).getName(), connError);
            } else if (result.compareTo(fail) == 0) {
                notifyUser(MyFlag.PLAY, radioList.get(currentStation).getName(), fail);
            } else if (result.compareTo(metaError) == 0 || (currentTitle.compareTo("")!=0)) {
                notifyUser(MyFlag.PAUSE, currentStationName, currentStationGenre);
            } else if (currentArtist.compareTo("") == 0) {
                notifyUser(MyFlag.PAUSE, currentStationName, currentTitle);
            } else {
                notifyUser(MyFlag.PAUSE, currentStationName, currentTitle + " - " + currentArtist);
            }
        }
    }
}
