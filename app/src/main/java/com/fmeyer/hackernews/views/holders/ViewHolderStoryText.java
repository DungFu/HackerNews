package com.fmeyer.hackernews.views.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.fmeyer.hackernews.R;

public class ViewHolderStoryText extends RecyclerView.ViewHolder {
    public final View mView;
    public final TextView mStoryText;
    public String itemId;

    public ViewHolderStoryText(View view) {
        super(view);
        mView = view;
        mStoryText = (TextView) view.findViewById(R.id.story_text);
    }
}
