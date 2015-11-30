package com.fmeyer.hackernews;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fmeyer.hackernews.models.ItemWrapper;
import com.fmeyer.hackernews.views.binders.ViewBinderStory;
import com.fmeyer.hackernews.views.holders.ViewHolderStory;
import com.fmeyer.hackernews.views.listeners.StoryInteractionListener;

import java.util.ArrayList;
import java.util.List;

public class StoriesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ItemWrapper> mValues = new ArrayList<>();
    private final StoryInteractionListener mStoryListener;

    public StoriesAdapter(StoryInteractionListener storyListener) {
        mStoryListener = storyListener;
    }

    public void addStory(ItemWrapper itemWrapper) {
        mValues.add(itemWrapper);
        if (itemWrapper.shouldShow()) {
            notifyItemInserted(getPositionForItemWrapper(itemWrapper));
        }
    }

    public void addStory(ItemWrapper itemWrapper, int newPosition) {
        mValues.add(newPosition, itemWrapper);
        if (itemWrapper.shouldShow()) {
            notifyItemInserted(getPositionForItemWrapper(itemWrapper));
        }
    }

    public void moveStory(ItemWrapper itemWrapper, int newPosition) {
        if (mValues.contains(itemWrapper)) {
            int oldPosition = getPositionForItemWrapper(itemWrapper);
            mValues.remove(itemWrapper);
            mValues.add(newPosition, itemWrapper);
            notifyMoveStory(oldPosition, itemWrapper);
        } else {
            Log.e(StoriesAdapter.class.getName(), "Tried to move story that is not in adapter!");
        }
    }

    public void removeStory(ItemWrapper itemWrapper) {
        int oldPosition = getPositionForItemWrapper(itemWrapper);
        mValues.remove(itemWrapper);
        notifyRemoveStory(oldPosition);
    }

    public int getPositionForItemWrapper(ItemWrapper itemWrapper) {
        int retVal = -1;
        int progress = 0;
        if (itemWrapper.shouldShow()) {
            for (int i = 0; i < mValues.size(); i++) {
                if (itemWrapper == mValues.get(i)) {
                    retVal = progress;
                } else {
                    progress += mValues.get(i).shouldShow() ? 1 : 0;
                }
            }
        }
        return retVal;
    }

    private ItemWrapper getItemWrapperFromPosition(int position) {
        int progress = 0;
        for (int i = 0; i < mValues.size(); i++) {
            if (progress == position && mValues.get(i).shouldShow()) {
                return mValues.get(i);
            } else {
                progress += mValues.get(i).shouldShow() ? 1 : 0;
            }
        }
        return null;
    }

    public void notifyAddStory(ItemWrapper itemWrapper) {
        int index = getPositionForItemWrapper(itemWrapper);
        if (index != -1) {
            notifyItemInserted(index);
        }
    }

    public void notifyRemoveStory(int position) {
        if (position != -1) {
            notifyItemRemoved(position);
        }
    }

    public void notifyUpdateStory(ItemWrapper itemWrapper) {
        int index = getPositionForItemWrapper(itemWrapper);
        if (index != -1) {
            notifyItemChanged(index);
        }
    }

    public void notifyMoveStory(int oldPosition, ItemWrapper itemWrapper) {
        int index = getPositionForItemWrapper(itemWrapper);
        if (oldPosition != -1 && index != -1 && oldPosition != index) {
            if (index > oldPosition) {
                // only actually move the item when it is moving to a later position
                notifyItemMoved(oldPosition, index);
            } else {
                // remove the item and re-add it when it is moving to an earlier position
                notifyItemRemoved(oldPosition);
                notifyItemInserted(index);
            }
        }
    }

    public void clear() {
        mValues.clear();
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View viewStory = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_stories, parent, false);
        return new ViewHolderStory(viewStory);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final ViewHolderStory holderStory = (ViewHolderStory) holder;
        ItemWrapper itemWrapper = getItemWrapperFromPosition(position);
        if (itemWrapper != null && itemWrapper.shouldShow()) {
            ViewBinderStory.bind(holderStory, mStoryListener, itemWrapper.getItem(), true);
        }
    }

    @Override
    public int getItemCount() {
        int size = 0;
        for (int i = 0; i < mValues.size(); i++) {
            size += mValues.get(i).shouldShow() ? 1 : 0;
        }
        return size;
    }
}
