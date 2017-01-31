package com.tinkersstudio.musiccloud;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainScreen extends AppCompatActivity {

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle drawerToggle;
    NavigationView navigation;
    Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        initInstances();
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
                        Toast.makeText(context, "Open the Music Player Activity", Toast.LENGTH_SHORT).show();
                        break;
                    /**Offline service group*/
                    case R.id.navigation_view_music_library:
                        Toast.makeText(context, "Open thee Music Library", Toast.LENGTH_SHORT).show();
                        //Do some thing here
                        // add navigation drawer item onclick method here
                        break;
                    case R.id.navigation_view_music_playlist:
                        Toast.makeText(context, "Open the Music Playlist", Toast.LENGTH_SHORT).show();
                        //Do some thing here
                        // add navigation drawer item onclick method here
                        break;
                    case R.id.navigation_view_favorite_list:
                        Toast.makeText(context,"Open Favorite List", Toast.LENGTH_SHORT).show();
                        break;
                    /** Online service group*/
                    /** Setting group*/
                    case R.id.navigation_view_user_info:
                        Toast.makeText(context, "User Info", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.navigation_view_equalizer:
                        Toast.makeText(context, "Open the equalize", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.navigation_view_user_stat:
                        Toast.makeText(context, "Open the achievement page", Toast.LENGTH_SHORT).show();
                        //Do some thing here
                        // add navigation drawer item onclick method here
                        break;
                    case R.id.navigation_view_customize:
                        Toast.makeText(context, "Open the customize page for the player",Toast.LENGTH_SHORT).show();
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
    //TODO: Implement menu
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
}