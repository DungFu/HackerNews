package com.fmeyer.hackernews;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.fmeyer.hackernews.HackerNewsStoriesQuery.Story;
import com.fmeyer.hackernews.views.listeners.CommentInteractionListener;
import com.fmeyer.hackernews.views.listeners.StoryInteractionListener;
import com.fmeyer.hackernews.views.listeners.StoryTextInteractionListener;

public class StoryActivity extends AppCompatActivity
        implements StoryInteractionListener, StoryTextInteractionListener, CommentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);
        setupActionBar();

        if (getIntent().getExtras() != null) {
//            mStoryItem = getIntent().getExtras().getParcelable(Constants.EXTRA_STORY_ITEM);
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_story_placeholder, StoryFragment.newInstance());
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

    private void launchUrl(Story story) {
        if (story != null && story.url() != null) {
            Uri uri = Uri.parse(story.url());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    }

    @Override
    public void onStoryInteraction(Story story, STORY_CLICK_INTERACTION_TYPE interactionType) {
        launchUrl(story);
    }

    @Override
    public void onStoryTextInteraction(String itemId) {
        // do nothing
    }

    @Override
    public void onCommentInteraction(String itemId) {
        // do nothing
    }
}
