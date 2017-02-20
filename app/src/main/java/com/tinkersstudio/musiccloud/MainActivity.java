package com.tinkersstudio.musiccloud;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import controller.MusicService;
import controller.MyFlag;
import es.dmoral.toasty.Toasty;

/**
 * Created by Owner on 2/19/2017.
 */

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String LOG_TAG = "MainActivity";

    /* Intent use for binding with service */
    private Intent bindingIntent;

    /* The Service associate with this activity */
    private MusicService myService;

    /* A flag indicate the state of the Service */
    private MyFlag serviceBound;

    DrawerLayout drawer;
    ActionBarDrawerToggle drawerToggle;
    NavigationView navigationView;
    Context context;
    Toolbar toolbar = null;

    /*Fragment control*/
    FragmentTransaction fragmentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        /*Fragment control*/
        fragmentTransaction = getSupportFragmentManager().beginTransaction();

        //Set the fragment initially
        FragmentHome fragment = new FragmentHome();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        checkingPermission();



        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);

        //How to change elements in the header programatically
        //View headerView = navigationView.getHeaderView(0);
        //TextView emailText = (TextView) headerView.findViewById(R.id.email);
        //emailText.setText("newemail@email.com");

        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    
    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();
        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        //initialize in case of failure
        //FIXME Use this to avoid the case of cloud service
        Fragment fragment = new FragmentHome();

        switch (id) {
            case R.id.navigation_view_home:
                fragment = new FragmentHome();
                break;
            case R.id.navigation_view_now_playing:
                //Toasty.info(context, "Open the Music Player Activity", Toast.LENGTH_SHORT, true).show();
                fragment = new FragmentMusicPlayer();
                break;
            /**Offline service group*/
            case R.id.navigation_view_music_library:
                //Toasty.info(context, "Open thee Music Library", Toast.LENGTH_SHORT, true).show();
                fragment = new FragmentSongList();
                break;
            case R.id.navigation_view_music_playlist:
                Toasty.info(context, "Open the Music Playlist", Toast.LENGTH_SHORT, true).show();
                break;
            case R.id.navigation_view_favorite_list:
                fragment = new FragmentFavoriteList();
                break;
            /** Online service group*/
            case R.id.navigation_view_spotify:
                Toasty.info(context, "Open Spotify Service", Toast.LENGTH_SHORT, true).show();
                break;
            case R.id.navigation_view_soundcloud:
                Toasty.info(context, "Open Soundcloud", Toast.LENGTH_SHORT, true).show();
                break;
            /** FragmentSetting group*/
            case R.id.navigation_view_user_info:
                //Toasty.info(context, "User Info", Toast.LENGTH_SHORT, true).show();
                fragment = new FragmentProfile();
                break;
            case R.id.navigation_view_equalizer:
                //Toasty.info(context, "Open the equalize", Toast.LENGTH_SHORT, true).show();
                fragment = new FragmentEqualizer();
                break;
            case R.id.navigation_view_customize:
                //Toasty.info(context, "Open the customize page for the player", Toast.LENGTH_SHORT, true).show();
                fragment = new FragmentSetting();
                break;
        }
        //fragmentTransaction.replace(R.id.main_screen_content_frame,fragment);
        fragmentTransaction.replace(R.id.fragment_container,fragment);
        fragmentTransaction.commit();
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Everytime we start this activity, bind it to the Service
     */
    @Override
    protected void onStart() {
        super.onStart();


         Log.i(LOG_TAG, "musicConnection at: " + myMusicConnection);
         Log.i(LOG_TAG, "musicService at: " + myService);
         if(bindingIntent==null){
         bindingIntent = new Intent(this, MusicService.class);

         startService(bindingIntent);
         Log.i(LOG_TAG, "Service Started by Main Screen");

         bindService(bindingIntent, myMusicConnection, Context.BIND_AUTO_CREATE);
         Log.i(LOG_TAG, "Service Binded to Main Screen");
         }

         Log.i(LOG_TAG, "musicConnection at: " + myMusicConnection);
         Log.i(LOG_TAG, "musicService at: " + myService);

    }

    /* This variable is the binding connection with the MusicService */
    private ServiceConnection myMusicConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(LOG_TAG, "myMusicConnection Connecting...");
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;

            //get the reference of the service
            myService = binder.getService();
            serviceBound = MyFlag.IS_ON;
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = MyFlag.IS_OFF;
        }
    };

    public void checkingPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED &&
                    (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED)) {
                Log.v(LOG_TAG,"Permission is granted");
            } else {
                Log.v(LOG_TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v("TAG","Permission is granted");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.v("TAG","Permission: "+permissions[0]+ "was "+grantResults[0]);
            //resume tasks needing this permission
        }
    }
}
