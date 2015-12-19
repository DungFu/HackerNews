package com.fmeyer.hackernews;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fmeyer.hackernews.models.Item;
import com.fmeyer.hackernews.models.ItemCommentWrapper;
import com.fmeyer.hackernews.views.binders.ViewBinderComment;
import com.fmeyer.hackernews.views.binders.ViewBinderStory;
import com.fmeyer.hackernews.views.holders.ViewHolderComment;
import com.fmeyer.hackernews.views.holders.ViewHolderStory;
import com.fmeyer.hackernews.views.listeners.CommentInteractionListener;
import com.fmeyer.hackernews.views.listeners.StoryInteractionListener;

import java.util.ArrayList;
import java.util.List;

public class StoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int STORY_HEADER_VIEW_TYPE = 0;
    private static final int COMMENT_VIEW_TYPE = 1;

    private final List<ItemCommentWrapper> mValues = new ArrayList<>();
    private final StoryInteractionListener mStoryListener;
    private final CommentInteractionListener mCommentListener;

    private Item mMainStory;

    public StoryAdapter(
            StoryInteractionListener storyListener,
            CommentInteractionListener commentListener) {
        mStoryListener = storyListener;
        mCommentListener = commentListener;
    }

    public void setMainStory(Item item) {
        mMainStory = item;
        notifyItemChanged(0);
    }

    public void addComment(ItemCommentWrapper itemCommentWrapper) {
        mValues.add(itemCommentWrapper);
    }

    public int getPositionForWrapper(ItemCommentWrapper itemCommentWrapper) {
        int retVal = -1;
        if (itemCommentWrapper.shouldShow()) {
            ItemCommentWrapper parent = itemCommentWrapper.getParent();
            if (parent != null) {
                if (parent.getKids() != null) {
                    int index = parent.getKids().indexOf(itemCommentWrapper);
                    if (index != -1) {
                        int progress = 0;
                        for (int i = 0; i < parent.getKids().size(); i++) {
                            if (itemCommentWrapper == parent.getKids().get(i)) {
                                break;
                            } else {
                                progress += parent.getKids().get(i).getSize();
                            }
                        }
                        int position = getPositionForWrapper(parent);
                        if (position != -1) {
                            retVal = position + (parent.shouldShow() ? 1 : 0) + progress;
                        } else {
                            Log.e(StoryAdapter.class.getName(), "Position not found for item in getPositionForWrapper()");
                        }
                    } else {
                        Log.e(StoryAdapter.class.getName(), "Kid is not in parent kid list!");
                    }
                }
            } else {
                int progress = 0;
                for (int i = 0; i < mValues.size(); i++) {
                    if (itemCommentWrapper == mValues.get(i)) {
                        retVal = progress;
                        break;
                    } else {
                        progress += mValues.get(i).getSize();
                    }
                }
            }
        }
        return retVal;
    }

    public void notifyCommentAdd(int position, int size) {
        if (size > 0) {
            notifyItemRangeInserted(
                    (mMainStory != null ? 1 : 0) + position,
                    size);
        }
    }

    public void notifyCommentRemove(int position, int size) {
        if (size > 0) {
            notifyItemRangeRemoved(
                    (mMainStory != null ? 1 : 0) + position,
                    size);
        }
    }

    public void notifyCommentChange(int position) {
        notifyItemChanged((mMainStory != null ? 1 : 0) + position);
    }

    public void clear() {
        mValues.clear();
        mMainStory = null;
        notifyDataSetChanged();
    }

    private int getValuesSize() {
        int size = 0;
        for (ItemCommentWrapper itemCommentWrapper : mValues) {
            size += itemCommentWrapper.getSize();
        }
        return size;
    }

    private @Nullable ItemCommentWrapper getValuesItem(int position) {
        return getValuesItemHelper(mValues, position);
    }

    private @Nullable ItemCommentWrapper getValuesItemHelper(
            List<ItemCommentWrapper> values,
            int position) {
        int progress = 0;
        if (values != null) {
            for (int i = 0; i < values.size(); i++) {
                ItemCommentWrapper itemCommentWrapper = values.get(i);
                if (itemCommentWrapper.shouldShow()) {
                    int size = itemCommentWrapper.getSize();
                    if (position == progress) {
                        return itemCommentWrapper;
                    } else if (
                            position >= progress + (itemCommentWrapper.shouldShow() ? 1 : 0) &&
                            position < progress + size) {
                        return getValuesItemHelper(
                                itemCommentWrapper.getKids(),
                                position - (progress + (itemCommentWrapper.shouldShow() ? 1 : 0)));
                    } else {
                        progress += itemCommentWrapper.getSize();
                    }
                } else {
                    progress += itemCommentWrapper.getSize();
                }
            }
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        if (mMainStory != null && position <= 0) {
            return STORY_HEADER_VIEW_TYPE;
        } else {
            return COMMENT_VIEW_TYPE;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case STORY_HEADER_VIEW_TYPE:
                View viewStory = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_story_above_comments, parent, false);
                return new ViewHolderStory(viewStory);
            case COMMENT_VIEW_TYPE:
            default:
                View viewComment = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_comment, parent, false);
                return new ViewHolderComment(viewComment);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolderStory) {
            final ViewHolderStory holderStory = (ViewHolderStory) holder;
            if (mMainStory != null) {
                ViewBinderStory.bind(holderStory, mStoryListener, mMainStory, false);
            }
        } else if (holder instanceof ViewHolderComment) {
            final ViewHolderComment holderComment = (ViewHolderComment) holder;
            final ItemCommentWrapper itemWrapper = getValuesItem(position - 1);
            if (itemWrapper != null && itemWrapper.shouldShow()) {
                ViewBinderComment.bind(this, holderComment, mCommentListener, itemWrapper);
            }
        }
    }

    @Override
    public int getItemCount() {
        return (mMainStory != null ? 1 : 0) + getValuesSize();
    }
}
