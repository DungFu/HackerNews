package com.fmeyer.hackernews.views.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.fmeyer.hackernews.R;
import com.fmeyer.hackernews.models.Item;

public class ViewHolderComment extends RecyclerView.ViewHolder {
    public final View mView;
    public final TextView mAuthorTimeText;
    public final TextView mCommentText;
    public Item mItem;

    public ViewHolderComment(View view) {
        super(view);
        mView = view;
        mAuthorTimeText = (TextView) view.findViewById(R.id.author_time);
        mCommentText = (TextView) view.findViewById(R.id.comment_text);
    }
}
