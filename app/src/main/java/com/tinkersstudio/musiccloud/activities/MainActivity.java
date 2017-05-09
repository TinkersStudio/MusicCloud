package com.tinkersstudio.musiccloud.activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crash.FirebaseCrash;
import com.tinkersstudio.musiccloud.R;
import com.tinkersstudio.musiccloud.controller.MyPlayer;
import com.tinkersstudio.musiccloud.fragment.FragmentEqualizer;
import com.tinkersstudio.musiccloud.fragment.FragmentFavoriteList;
import com.tinkersstudio.musiccloud.fragment.FragmentHome;
import com.tinkersstudio.musiccloud.fragment.FragmentMusicPlayer;
import com.tinkersstudio.musiccloud.fragment.FragmentProfile;
import com.tinkersstudio.musiccloud.fragment.FragmentRadio;
import com.tinkersstudio.musiccloud.fragment.FragmentSetting;
import com.tinkersstudio.musiccloud.fragment.FragmentSongList;

import com.tinkersstudio.musiccloud.controller.MusicService;
import com.tinkersstudio.musiccloud.util.MyFlag;
import es.dmoral.toasty.Toasty;

/**
 * Created by Owner on 2/19/2017.
 */

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String LOG_TAG = "MainActivity";
    private static FirebaseAnalytics mFirebaseAnalytics;

    /* Intent use for binding with service */
    private static Intent bindingIntent;

    /* The Service associate with this activity */
    public static MusicService myService;

    /* A flag indicate the state of the Service */
    private static MyFlag serviceBound;

    private DrawerLayout drawer;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;
    private Context context;
    private Toolbar toolbar = null;
    private FirebaseAuth mAuth;

    private boolean permissionGranted = false;

    /*Fragment control*/
    FragmentTransaction fragmentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        if(savedInstanceState!=null)
            Log.i(LOG_TAG, "there was some saved stated " + myService);
        else
            Log.i(LOG_TAG, "there was NOT some saved stated");

        //check the user permission
        new CheckPermission().execute();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this.getApplicationContext());
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

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //FIXME: could be in here
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();
        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Fragment fragment = new FragmentHome();

        switch (id) {
            case R.id.navigation_view_home:
                this.setTitle("Music Cloud");
                fragment = new FragmentHome();
                break;
            case R.id.navigation_view_now_playing:
                this.setTitle("Now Playing");
                fragment = new FragmentMusicPlayer();
                ((FragmentMusicPlayer)fragment).setMusicService(myService);
                break;
            /**Offline service group*/
            case R.id.navigation_view_music_library:
                this.setTitle("Music Library");
                fragment = new FragmentSongList();
                break;
            case R.id.navigation_view_music_playlist:
                this.setTitle("Music Playlist");
                Toasty.info(context, "Open the Music Playlist", Toast.LENGTH_SHORT, true).show();
                break;
            case R.id.navigation_view_favorite_list:
                this.setTitle("Favorite Music");
                Toasty.info(context, "Open the Favorite list", Toast.LENGTH_SHORT, true).show();
                fragment = new FragmentFavoriteList();
                break;
            /** Online service group*/
            case R.id.navigation_view_spotify:
                this.setTitle("Spotify");
                Toasty.info(context, "Open Spotify Service", Toast.LENGTH_SHORT, true).show();
                break;
            case R.id.navigation_view_radio:
                this.setTitle("Online Radio");
                fragment = new FragmentRadio();
                break;
            //case R.id.navigation_view_soundcloud:
            //    Toasty.info(context, "Open Soundcloud", Toast.LENGTH_SHORT, true).show();
            //    break;

            /** FragmentSetting group*/
            case R.id.navigation_view_user_info:
                this.setTitle("User Info");
                Toasty.info(context, "User Info", Toast.LENGTH_SHORT, true).show();
                fragment = new FragmentProfile();
                break;
            case R.id.navigation_view_equalizer:
                this.setTitle("Equalizer");
                fragment = new FragmentEqualizer();
                ((FragmentEqualizer)fragment).setMusicPlayer(myService.getPlayer(myService.getMode()));
                break;
            case R.id.navigation_view_customize:
                this.setTitle("Customize Player");
                Toasty.info(context, "Open the customize page for the player", Toast.LENGTH_SHORT, true).show();
                fragment = new FragmentSetting();
                break;
            case R.id.navigation_view_quit:
                myService.releasePlayer();
                myService.stopForeground(true);
                finishAffinity();
                System.exit(0);
                break;
        }
        //fragmentTransaction.replace(R.id.main_screen_content_frame,fragment);
        fragmentTransaction.replace(R.id.fragment_container,fragment);

        //Add the fragment to stack and commit
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * The function is called if the authentication is made. Update the UI
     * @param currentUser
     */
    protected void updateUI(FirebaseUser currentUser)
    {

    }
    /**
     * Everytime we start this activity, bind it to the Service
     */
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null)
        {
            updateUI(currentUser);
        }
        else {
            //TODO: Set the default UI
        }

        if (bindingIntent == null) {
            bindingIntent = new Intent(this, MusicService.class);
            startService(bindingIntent);
            bindService(bindingIntent, myMusicConnection, Context.BIND_AUTO_CREATE);
            serviceBound = MyFlag.IS_ON;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (bindingIntent == null) {
            Log.i(LOG_TAG, "No previous Service found");
        } else {
            Log.i(LOG_TAG, "Has found Previous Service ");
            //FIXME When user click on Notification bar => Show the Music Player
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (serviceBound == MyFlag.IS_ON) {
            unbindService(myMusicConnection);
            serviceBound = MyFlag.IS_OFF;
        }
    }
    /**
     * Appropriate way to unbind the MusicService when this activity get killed
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);;
    }


    /* This variable is the binding connection with the MusicService */
    private ServiceConnection myMusicConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.i("TAG","Permission: "+permissions[0]+ "was "+grantResults[0]);
            permissionGranted = true;
            myService.reLoadData();
        }
    }
    public boolean getPermissionStatus(){return permissionGranted;}
    /**
     * This class handle checking permission
     */
    public class CheckPermission extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            //upload the event in here
            try { checkingPermission();}
            catch (Exception e) {
                FirebaseCrash.logcat(Log.ERROR, MainActivity.this.LOG_TAG, "Exception in user case");
                FirebaseCrash.report(e);
                Log.e(LOG_TAG, "Error getting permission");
                return "fail";
            }
            return "Success";
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected void onPostExecute(String s) {super.onPostExecute(s);}

        public void checkingPermission() {
            if (Build.VERSION.SDK_INT >= 23) {
                if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED &&
                        (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                == PackageManager.PERMISSION_GRANTED)) {
                    permissionGranted = true;
                } else {
                    permissionGranted = false;
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                }
            }
            else {
                permissionGranted = true;
                //permission is automatically granted on sdk<23 upon installation
                Log.i("TAG","Permission is granted");
            }
        }
    }
}
