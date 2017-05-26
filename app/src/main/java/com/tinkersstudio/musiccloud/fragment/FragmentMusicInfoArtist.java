package com.tinkersstudio.musiccloud.fragment;

import android.graphics.Path;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.View;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.tinkersstudio.musiccloud.R;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.tinkersstudio.musiccloud.model.*;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.hdodenhof.circleimageview.CircleImageView;
import java.net.URL;
import java.net.URLEncoder;
import java.io.InputStream;
/**
 * Created by anhnguyen on 5/25/17.
 */
public class FragmentMusicInfoArtist extends Fragment {
    private Song currentSong ;
    TextView bio, stats, artist;
    CircleImageView image;


    public void setCurrentSong(Song currentSong) {this.currentSong = currentSong;}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_song_info_artist, container, false);
        image = (CircleImageView) rootView.findViewById(R.id.fm_lastFM_image);
        bio = (TextView) rootView.findViewById(R.id.fm_lastFM_content);
        stats = (TextView) rootView.findViewById(R.id.fm_lastFM_stats);
        artist = (TextView) rootView.findViewById(R.id.fm_lastFM_artist);

        try {
            StringBuilder stringBuilder = new StringBuilder("http://ws.audioscrobbler.com/2.0/");
            stringBuilder.append("?method=artist.getinfo");
            stringBuilder.append("&api_key=");
            stringBuilder.append("26bb93382a31914fa465b76a6a24f624");
            stringBuilder.append("&artist=" + URLEncoder.encode(currentSong.getArtist(), "UTF-8"));
            new retrieveInfoLastFM().execute(stringBuilder.toString()).get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public class retrieveInfoLastFM extends AsyncTask<String, Void, String> {
        private String LOG_Task = "retrieveInfoLastFM";
        private String listener = "";
        private String playcount = "";
        private String bibliogaphy = "";
        private String imUrl = "";
        Bitmap bitmap = null;
        @Override
        protected String doInBackground(String... urls) {
            Document doc = null;
            try {
                URL lastFm_url = new URL(urls[0]);
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                doc = dBuilder.parse(lastFm_url.openStream());

                if (doc.hasChildNodes()) {
                    printNote(doc.getChildNodes());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "Success";
        }


        private void printNote(NodeList nodeList) {
            for (int count = 0; count < nodeList.getLength(); count++) {
                Node tempNode = nodeList.item(count);
                // make sure it's element node.
                if (tempNode.getNodeType() == Node.ELEMENT_NODE &&
                        tempNode.getNodeName().compareTo("similar") != 0){
                    //System.out.print( "<" + tempNode.getNodeName());
                    if (tempNode.hasAttributes()) {
                        // get attributes names and values
                        NamedNodeMap nodeMap = tempNode.getAttributes();
                        for (int i = 0; i < nodeMap.getLength(); i++) {
                            Node node = nodeMap.item(i);
                            //System.out.print( " " + node.getNodeName());
                            //System.out.print( "=\"" + node.getNodeValue() +"\"");
                        }
                    }
                    //System.out.print( ">");

                    // Still have child node, recursively getting nodes
                    if (tempNode.hasChildNodes() && tempNode.getChildNodes().getLength() > 1) {
                        // loop again if has child nodes
                        //System.out.print("\n");
                        printNote(tempNode.getChildNodes());
                    }
                    //We are at leaf, get the info we want
                    else {
                        //System.out.print(tempNode.getTextContent());
                        switch (tempNode.getNodeName()) {
                            case "content":
                                bibliogaphy = tempNode.getTextContent();
                                break;
                            case "listeners":
                                listener = tempNode.getTextContent();
                                break;
                            case "playcount":
                                playcount = tempNode.getTextContent();
                                break;
                            case "image":
                                //priotize to get the medium image, if medium not available, just get whatever size
                                if (tempNode.hasAttributes() && tempNode.getAttributes().item(0).getNodeValue() != null &&
                                        tempNode.getAttributes().item(0).getNodeValue().compareTo("medium") == 0) {
                                    imUrl = tempNode.getTextContent();
                                } else if (imUrl.compareTo("") == 0) {
                                    imUrl = tempNode.getTextContent();
                                }
                                break;
                        }
                    }
                    //System.out.print( "</" + tempNode.getNodeName() + ">\n");
                }
            }
            //Get image
            try {
                if (imUrl.compareTo("")!=0) {
                    InputStream in = new java.net.URL(imUrl).openStream();
                    bitmap = BitmapFactory.decodeStream(in);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        @Override
        protected void onPreExecute() {super.onPreExecute();}
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            artist.setText(currentSong.getArtist());
            stats.setText("Listeners: " + listener + "\nPlay Count: " + playcount);
            if (bibliogaphy.compareTo("") != 0) {
                bio.setText(bibliogaphy);
            }
            else {
                bio.setText("No Information About\n" + currentSong.getArtist() + "\nAvailable on Last.FM");
                bio.setGravity(Gravity.CENTER);
            }
            try {
                if (bitmap != null) {
                    image.setImageBitmap(bitmap);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
