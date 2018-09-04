package com.fmeyer.hackernews.views.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fmeyer.hackernews.HackerNewsStoriesQuery.Story;
import com.fmeyer.hackernews.R;

public class ViewHolderStory extends RecyclerView.ViewHolder {
    public final View mView;
    public final LinearLayout mCommentsContainer;
    public final ImageView mCommentsImageView;
    public final TextView mCommentsView;
    public final TextView mTitleView;
    public final TextView mSubtitleView;
    public Story mItem;

    public ViewHolderStory(View view) {
        super(view);
        mView = view;
        mCommentsContainer = (LinearLayout) view.findViewById(R.id.comments_container);
        mCommentsImageView = (ImageView) view.findViewById(R.id.comments_image);
        mCommentsView = (TextView) view.findViewById(R.id.comments);
        mTitleView = (TextView) view.findViewById(R.id.title);
        mSubtitleView = (TextView) view.findViewById(R.id.subtitle);
    }
}
