package com.fmeyer.hackernews;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.firebase.client.Firebase;
import com.fmeyer.hackernews.models.Item;

public class StoryActivity extends AppCompatActivity implements StoryFragment.OnListFragmentInteractionListener {

    private Item mStoryItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);
        setupActionBar();

        Firebase.setAndroidContext(this);

        if (getIntent().getExtras() != null) {
            mStoryItem = getIntent().getExtras().getParcelable(Constants.EXTRA_STORY_ITEM);
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_story_placeholder, StoryFragment.newInstance(mStoryItem));
        ft.commit();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.comments_titlebar));
        }
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void launchUrl(Item item) {
        if (item != null && item.getUrl() != null) {
            Uri uri = Uri.parse(item.getUrl());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    }

    @Override
    public void onListFragmentInteraction(Item item) {
        launchUrl(item);
    }
}
