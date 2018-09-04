package com.fmeyer.hackernews;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fmeyer.hackernews.HackerNewsStoriesQuery.Story;
import com.fmeyer.hackernews.views.binders.ViewBinderStory;
import com.fmeyer.hackernews.views.holders.ViewHolderStory;
import com.fmeyer.hackernews.views.listeners.StoryInteractionListener;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class StoriesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<Story> mValues = new ArrayList<>();
    private final StoryInteractionListener mStoryListener;

    public StoriesAdapter(StoryInteractionListener storyListener) {
        mStoryListener = storyListener;
    }

    public void add(List<Story> stories) {
        mValues.addAll(stories);
        notifyDataSetChanged();
    }

    public void add(Story story) {
        mValues.add(story);
        notifyDataSetChanged();
    }

    public @Nullable String getLastId() {
        if (mValues.isEmpty()) {
            return null;
        }
        return mValues.get(mValues.size() - 1).id();
    }

    public void clear() {
        mValues.clear();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View viewStory = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_story, parent, false);
        return new ViewHolderStory(viewStory);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final ViewHolderStory holderStory = (ViewHolderStory) holder;
        ViewBinderStory.bind(holderStory, mStoryListener, mValues.get(position), true);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }
}
