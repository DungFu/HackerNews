package com.fmeyer.hackernews.views.binders;

import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;

import com.fmeyer.hackernews.views.listeners.CommentInteractionListener;
import com.fmeyer.hackernews.R;
import com.fmeyer.hackernews.StoryAdapter;
import com.fmeyer.hackernews.Utils;
import com.fmeyer.hackernews.models.Item;
import com.fmeyer.hackernews.models.ItemCommentWrapper;
import com.fmeyer.hackernews.views.holders.ViewHolderComment;

public class ViewBinderComment {

    public static void bind(
            final StoryAdapter storyAdapter,
            final ViewHolderComment viewHolderComment,
            final CommentInteractionListener listener,
            final ItemCommentWrapper itemWrapper) {
        final Item item = itemWrapper.getItem();
        viewHolderComment.mItem = item;
        viewHolderComment.mAuthorTimeText.setText(item.getBy());
        viewHolderComment.mAuthorTimeText.setText(
                String.format(
                        viewHolderComment.mView.getResources().getString(R.string.comment_author_time),
                        item.getBy(),
                        DateUtils.getRelativeTimeSpanString(
                                (long) item.getTime() * (long) 1000)));
        viewHolderComment.mCommentText.setText(Utils.trim(Html.fromHtml(item.getText())));
        viewHolderComment.mCommentText.setVisibility(
                itemWrapper.isCollapsed() ? View.GONE : View.VISIBLE);
        ViewGroup.LayoutParams layoutParams = viewHolderComment.mView.getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) layoutParams;
            p.setMargins(
                    (int) (viewHolderComment.mView.getResources().getDimension(R.dimen.comment_indent) * itemWrapper.getDepth()),
                    0,
                    0,
                    0);
            viewHolderComment.mView.requestLayout();
        }
        View.OnLongClickListener longClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (listener != null) {
                    boolean isCollapsed = itemWrapper.isCollapsed();
                    int position = storyAdapter.getPositionForWrapper(itemWrapper);
                    int beforeSize = itemWrapper.getDescendants();
                    itemWrapper.setCollapsed(!isCollapsed);
                    int afterSize = itemWrapper.getDescendants();
                    if (position != -1) {
                        if (isCollapsed) {
                            storyAdapter.notifyCommentAdd(position + (itemWrapper.shouldShow() ? 1 : 0), afterSize);
                        } else {
                            storyAdapter.notifyCommentRemove(position + (itemWrapper.shouldShow() ? 1 : 0), beforeSize);
                        }
                        storyAdapter.notifyCommentChange(position);
                    } else {
                        Log.e(ViewBinderComment.class.getName(), "Item not found to un/collapse!");
                    }
                    v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    listener.onCommentInteraction(item);
                    return true;
                }
                return false;
            }
        };
        viewHolderComment.mView.setOnLongClickListener(longClickListener);
        viewHolderComment.mView.setLongClickable(true);
        viewHolderComment.mCommentText.setOnLongClickListener(longClickListener);
        viewHolderComment.mCommentText.setLongClickable(true);
    }
}
