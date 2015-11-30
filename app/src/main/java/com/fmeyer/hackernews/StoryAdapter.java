package com.fmeyer.hackernews;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fmeyer.hackernews.StoryFragment.OnListFragmentInteractionListener;
import com.fmeyer.hackernews.models.Item;
import com.fmeyer.hackernews.models.ItemCommentWrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Item} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class StoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int STORY_HEADER_VIEW_TYPE = 0;
    private static final int COMMENT_VIEW_TYPE = 1;

    private final List<ItemCommentWrapper> mValues = new ArrayList<>();
    private final OnListFragmentInteractionListener mListener;

    private Item mMainStory;

    public StoryAdapter(OnListFragmentInteractionListener listener) {
        mListener = listener;
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
                        .inflate(R.layout.fragment_stories_white_background, parent, false);
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
                Context context = holderStory.mView.getContext();
                holderStory.mItem = mMainStory;
                holderStory.mCommentsView.setText(Integer.toString(mMainStory.getDescendants()));
                holderStory.mTitleView.setText(mMainStory.getTitle());
                holderStory.mSubtitleView.setText(
                        String.format(
                                holderStory.mView.getResources().getString(R.string.post_subtitle),
                                Integer.toString(mMainStory.getScore()),
                                mMainStory.getBy(),
                                DateUtils.getRelativeTimeSpanString(
                                        (long) mMainStory.getTime() * (long) 1000)));
                holderStory.mView.setBackgroundResource(R.color.white);
                holderStory.mCommentsImageView.getDrawable().setColorFilter(
                        ContextCompat.getColor(context, R.color.mediumGrey),
                        PorterDuff.Mode.SRC_ATOP);
                holderStory.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (null != mListener) {
                            mListener.onListFragmentInteraction(holderStory.mItem);
                        }
                    }
                });
            } else {
                holderStory.mItem = null;
                holderStory.mCommentsView.setText("");
                holderStory.mTitleView.setText("");
                holderStory.mSubtitleView.setText("");
            }
        } else if (holder instanceof ViewHolderComment) {
            final ViewHolderComment holderComment = (ViewHolderComment) holder;
            final ItemCommentWrapper itemWrapper = getValuesItem(position - 1);
            Item item;
            if (itemWrapper != null && itemWrapper.shouldShow()) {
                item = itemWrapper.getItem();
                holderComment.mItem = item;
                holderComment.mAuthorTimeText.setText(item.getBy());
                holderComment.mAuthorTimeText.setText(
                        String.format(
                                holderComment.mView.getResources().getString(R.string.comment_author_time),
                                item.getBy(),
                                DateUtils.getRelativeTimeSpanString(
                                        (long) item.getTime() * (long) 1000)));
                holderComment.mCommentText.setText(Utils.trim(Html.fromHtml(item.getText())));
                holderComment.mCommentText.setVisibility(
                        itemWrapper.isCollapsed() ? View.GONE : View.VISIBLE);
                ViewGroup.LayoutParams layoutParams = holderComment.mView.getLayoutParams();
                if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                    ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) layoutParams;
                    p.setMargins(
                            (int) (holderComment.mView.getResources().getDimension(R.dimen.comment_indent) * itemWrapper.getDepth()),
                            0,
                            0,
                            0);
                    holderComment.mView.requestLayout();
                }
                View.OnLongClickListener listener = new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (null != mListener) {
                            boolean isCollapsed = itemWrapper.isCollapsed();
                            int position = getPositionForWrapper(itemWrapper);
                            int beforeSize = itemWrapper.getDescendants();
                            itemWrapper.setCollapsed(!isCollapsed);
                            int afterSize = itemWrapper.getDescendants();
                            if (position != -1) {
                                if (isCollapsed) {
                                    notifyCommentAdd(position + (itemWrapper.shouldShow() ? 1 : 0), afterSize);
                                } else {
                                    notifyCommentRemove(position + (itemWrapper.shouldShow() ? 1 : 0), beforeSize);
                                }
                                notifyCommentChange(position);
                            } else {
                                Log.e(StoryAdapter.class.getName(), "Item not found to un/collapse!");
                            }
                            v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                            return true;
                        }
                        return false;
                    }
                };
                holderComment.mView.setOnLongClickListener(listener);
                holderComment.mCommentText.setOnLongClickListener(listener);
            } else {
                holderComment.mItem = null;
                holderComment.mAuthorTimeText.setText("");
                holderComment.mCommentText.setText("");
                holderComment.mView.setOnLongClickListener(null);
                holderComment.mCommentText.setOnLongClickListener(null);
                ViewGroup.LayoutParams layoutParams = holderComment.mView.getLayoutParams();
                if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                    ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) layoutParams;
                    p.setMargins(0, 0, 0, 0);
                    holderComment.mView.requestLayout();
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return (mMainStory != null ? 1 : 0) + getValuesSize();
    }

    public class ViewHolderStory extends RecyclerView.ViewHolder {
        public final View mView;
        public final LinearLayout mCommentsContainer;
        public final ImageView mCommentsImageView;
        public final TextView mCommentsView;
        public final TextView mTitleView;
        public final TextView mSubtitleView;
        public Item mItem;

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
}
