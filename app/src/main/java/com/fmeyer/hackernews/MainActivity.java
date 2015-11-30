package com.fmeyer.hackernews;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.firebase.client.Firebase;
import com.fmeyer.hackernews.models.Item;
import com.fmeyer.hackernews.views.listeners.StoryInteractionListener;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, StoryInteractionListener {

    Map<String, StoriesFragment> mStoriesFragment = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Firebase.setAndroidContext(this);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_main_placeholder, createOrGetFragmentFromFilterType("topstories"));
        ft.commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setCheckedItem(R.id.nav_frontpage);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Top Stories");
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        String filterType = "topstories";
        String title = "Top Stories";
        if (id == R.id.nav_frontpage) {
            filterType = "topstories";
            title = "Top Stories";
        } else if (id == R.id.nav_new) {
            filterType = "newstories";
            title = "New Stories";
        } else if (id == R.id.nav_show) {
            filterType = "showstories";
            title = "Show Stories";
        } else if (id == R.id.nav_ask) {
            filterType = "askstories";
            title = "Ask Stories";
        } else if (id == R.id.nav_jobs) {
            filterType = "jobstories";
            title = "Job Stories";
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_main_placeholder, createOrGetFragmentFromFilterType(filterType));
        ft.commit();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private StoriesFragment createOrGetFragmentFromFilterType(String filterType) {
        StoriesFragment fragment;
        if (mStoriesFragment.containsKey(filterType)) {
            fragment = mStoriesFragment.get(filterType);
        } else {
            fragment = StoriesFragment.newInstance(filterType);
            mStoriesFragment.put(filterType, fragment);
        }
        return fragment;
    }

    private void launchUrl(Item item) {
        if (item != null && item.getUrl() != null) {
            Uri uri = Uri.parse(item.getUrl());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } else {
            launchComments(item);
        }
    }

    private void launchComments(Item item) {
        if (item != null) {
            Intent intent = new Intent(getBaseContext(), StoryActivity.class);
            intent.putExtra(Constants.EXTRA_STORY_ITEM, item);
            startActivity(intent);
        }
    }

    @Override
    public void onStoryInteraction(Item item, STORY_CLICK_INTERACTION_TYPE interactionType) {
        switch (interactionType) {
            case URL:
                launchUrl(item);
                break;
            case COMMENTS:
                launchComments(item);
                break;
            default:
                Log.d(MainActivity.class.getName(), "Interaction type not valid!");
                break;
        }
    }
}
