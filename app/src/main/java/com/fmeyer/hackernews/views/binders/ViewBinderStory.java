package com.fmeyer.hackernews.views.binders;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.text.format.DateUtils;
import android.view.View;

import com.fmeyer.hackernews.HackerNewsStoriesQuery.Story;
import com.fmeyer.hackernews.R;
import com.fmeyer.hackernews.views.listeners.StoryInteractionListener;
import com.fmeyer.hackernews.views.holders.ViewHolderStory;

public class ViewBinderStory {

    public static void bind(
            final ViewHolderStory viewHolderStory,
            final StoryInteractionListener listener,
            Story item,
            boolean hasCommentsClickListener) {
        if (item != null) {
            Context context = viewHolderStory.mView.getContext();
            viewHolderStory.mItem = item;
            viewHolderStory.mCommentsView.setText(item.descendants() == null ? "0" : Integer.toString(item.descendants()));
            viewHolderStory.mTitleView.setText(item.title());
            viewHolderStory.mSubtitleView.setText(
                    String.format(
                            context.getResources().getString(R.string.post_subtitle),
                            item.score() == null ? "0" : Integer.toString(item.score()),
                            item.by().id(),
                            DateUtils.getRelativeTimeSpanString(
                                    (long) item.time() * (long) 1000)));
            viewHolderStory.mCommentsImageView.getDrawable().setColorFilter(
                    ContextCompat.getColor(context, R.color.mediumGrey),
                    PorterDuff.Mode.SRC_ATOP);
            viewHolderStory.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onStoryInteraction(
                                viewHolderStory.mItem,
                                StoryInteractionListener.STORY_CLICK_INTERACTION_TYPE.URL);
                    }
                }
            });
            viewHolderStory.mView.setClickable(true);
            viewHolderStory.mCommentsContainer.setOnClickListener(
                    hasCommentsClickListener ?
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (listener != null) {
                                        listener.onStoryInteraction(
                                                viewHolderStory.mItem,
                                                StoryInteractionListener.STORY_CLICK_INTERACTION_TYPE.COMMENTS);
                                    }
                                }
                            } :
                            null);
            viewHolderStory.mCommentsContainer.setClickable(hasCommentsClickListener);
        }
    }
}
