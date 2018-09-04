package com.fmeyer.hackernews.views.binders;

import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;

import com.fmeyer.hackernews.R;
import com.fmeyer.hackernews.StoryAdapter;
import com.fmeyer.hackernews.Utils;
import com.fmeyer.hackernews.views.holders.ViewHolderComment;
import com.fmeyer.hackernews.views.holders.ViewHolderStoryText;
import com.fmeyer.hackernews.views.listeners.CommentInteractionListener;
import com.fmeyer.hackernews.views.listeners.StoryTextInteractionListener;

public class ViewBinderStoryText {

    public static void bind(
            final ViewHolderStoryText viewHolderStoryText,
            final StoryTextInteractionListener listener,
            String itemId) {
//        if (item != null && item.getText() != null) {
//            viewHolderStoryText.mItem = item;
//            viewHolderStoryText.mStoryText.setText(Utils.trim(Html.fromHtml(item.getText())));
//        }
    }
}
