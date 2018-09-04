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

import com.fmeyer.hackernews.HackerNewsStoriesQuery.Story;
import com.fmeyer.hackernews.views.listeners.StoryInteractionListener;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, StoryInteractionListener {

    static final String SAVED_TAB_EXTRA = "saved_tab_extra";

    Map<String, StoriesFragment> mStoriesFragment = new HashMap<>();
    String mCurrentTab = "topstories";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState != null) {
            mCurrentTab = savedInstanceState.getString(SAVED_TAB_EXTRA, "topstories");
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_main_placeholder, createOrGetFragmentFromFilterType(mCurrentTab));
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
            getSupportActionBar().setTitle(getTabNameFromType(mCurrentTab));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(SAVED_TAB_EXTRA, mCurrentTab);
        super.onSaveInstanceState(savedInstanceState);
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

    private String getTabNameFromType(String tabType) {
        if (tabType.equals("topstories")) {
            return "Top Stories";
        } else if (tabType.equals("newstories")) {
            return "New Stories";
        } else if (tabType.equals("showstories")) {
            return "Show Stories";
        } else if (tabType.equals("askstories")) {
            return "Show Stories";
        } else if (tabType.equals("jobstories")) {
            return "Job Stories";
        } else {
            return "";
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        mCurrentTab = "topstories";
        if (id == R.id.nav_frontpage) {
            mCurrentTab = "topstories";
        } else if (id == R.id.nav_new) {
            mCurrentTab = "newstories";
        } else if (id == R.id.nav_show) {
            mCurrentTab = "showstories";
        } else if (id == R.id.nav_ask) {
            mCurrentTab = "askstories";
        } else if (id == R.id.nav_jobs) {
            mCurrentTab = "jobstories";
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_main_placeholder, createOrGetFragmentFromFilterType(mCurrentTab));
        ft.commit();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getTabNameFromType(mCurrentTab));
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

    private void launchUrl(Story item) {
        if (item != null && item.url() != null) {
            Uri uri = Uri.parse(item.url());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } else {
            launchComments(item);
        }
    }

    private void launchComments(Story item) {
        if (item != null) {
            Intent intent = new Intent(getBaseContext(), StoryActivity.class);
            intent.putExtra(Constants.EXTRA_STORY_ITEM_ID, item.id());
            startActivity(intent);
        }
    }

    @Override
    public void onStoryInteraction(Story story, STORY_CLICK_INTERACTION_TYPE interactionType) {
        switch (interactionType) {
            case URL:
                launchUrl(story);
                break;
            case COMMENTS:
                launchComments(story);
                break;
            default:
                Log.d(MainActivity.class.getName(), "Interaction type not valid!");
                break;
        }
    }
}
