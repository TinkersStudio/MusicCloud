package com.tinkersstudio.musiccloud;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import controller.MusicService.MusicBinder;
import controller.MusicService;

import controller.MyFlag;
import es.dmoral.toasty.Toasty;

public class MainScreen extends AppCompatActivity {

    private static final String LOG_TAG = "MainScreen";

    /* Intent use for binding with service */
    private Intent bindingIntent;

    /* The Service associate with this activity */
    private MusicService myService;

    /* A flag indicate the state of the Service */
    private MyFlag serviceBound;

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle drawerToggle;
    NavigationView navigation;
    Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        initInstances();
        checkingPermission();
        context = getApplicationContext();
    }

    private void initInstances() {
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawerToggle = new ActionBarDrawerToggle(MainScreen.this, drawerLayout, R.string.hello_world, R.string.hello_world);
        drawerLayout.setDrawerListener(drawerToggle);

        navigation = (NavigationView) findViewById(R.id.navigation_view);
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        navigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch (id) {
                    case R.id.navigation_view_now_playing:
                        //Toasty.info(context, "Open the Music Player Activity", Toast.LENGTH_SHORT, true).show();
                        Intent musicPlayer = new Intent(MainScreen.this, MusicPlayer.class);
                        startActivity(musicPlayer);
                        break;
                    /**Offline service group*/
                    case R.id.navigation_view_music_library:
                        //Toasty.info(context, "Open thee Music Library", Toast.LENGTH_SHORT, true).show();
                        Intent songList = new Intent(MainScreen.this, SongList.class);
                        startActivity(songList);
                        break;
                    case R.id.navigation_view_music_playlist:
                        Toasty.info(context, "Open the Music Playlist", Toast.LENGTH_SHORT, true).show();
                        break;
                    case R.id.navigation_view_favorite_list:
                        //Toasty.info(context, "Open Favorite List", Toast.LENGTH_SHORT, true).show();
                        Intent favoriteList= new Intent(MainScreen.this, FavoriteList.class);
                        startActivity(favoriteList);
                        break;
                    /** Online service group*/
                    case R.id.navigation_view_spotify:
                        Toasty.info(context, "Open Spotify Service", Toast.LENGTH_SHORT, true).show();
                        break;
                    case R.id.navigation_view_soundcloud:
                        Toasty.info(context, "Open Soundcloud", Toast.LENGTH_SHORT, true).show();
                        break;
                    /** Setting group*/
                    case R.id.navigation_view_user_info:
                        //Toasty.info(context, "User Info", Toast.LENGTH_SHORT, true).show();
                        Intent profile= new Intent(MainScreen.this, Profile.class);
                        startActivity(profile);
                        break;
                    case R.id.navigation_view_equalizer:
                        //Toasty.info(context, "Open the equalize", Toast.LENGTH_SHORT, true).show();
                        Intent equalizer= new Intent(MainScreen.this, Equalizer.class);
                        startActivity(equalizer);
                        break;
                    case R.id.navigation_view_customize:
                        //Toasty.info(context, "Open the customize page for the player", Toast.LENGTH_SHORT, true).show();
                        Intent setting = new Intent(MainScreen.this, Setting.class);
                        startActivity(setting);
                        break;
                }
                return false;
            }
        });

    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item))
            return true;

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
        //    return true;
        //}

        //return super.onOptionsItemSelected(item);
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
            MusicBinder binder = (MusicBinder)service;

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