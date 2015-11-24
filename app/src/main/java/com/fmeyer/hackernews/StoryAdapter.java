package com.fmeyer.hackernews;

import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
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
        int position = getPositionForWrapper(itemCommentWrapper);
        notifyItemRangeInserted(
                (mMainStory != null ? 1 : 0) + position,
                1 + itemCommentWrapper.getDescendants());
    }

    public void removeComment(ItemCommentWrapper itemCommentWrapper) {
        int position = getPositionForWrapper(itemCommentWrapper);
        int size = itemCommentWrapper.getDescendants();
        if (position != -1) {
            mValues.remove(itemCommentWrapper);
            notifyItemRangeRemoved(
                    (mMainStory != null ? 1 : 0) + position,
                    1 + size);
        }
    }

    public int getPositionForWrapper(ItemCommentWrapper itemCommentWrapper) {
        int retVal = -1;
        ItemCommentWrapper parent = itemCommentWrapper.getParent();
        if (parent != null) {
            int index = parent.getKids().indexOf(itemCommentWrapper);
            if (index != -1) {
                int progress = 0;
                for (int i = 0; i < parent.getKids().size(); i++) {
                    if (itemCommentWrapper == parent.getKids().get(i)) {
                        break;
                    } else {
                        progress += 1 + parent.getKids().get(i).getDescendants();
                    }
                }
                retVal = 1 + progress + getPositionForWrapper(parent);
            } else {
                Log.e(StoryAdapter.class.getName(), "Kid is not in parent kid list!");
            }
        } else {
            int progress = 0;
            for (int i = 0; i < mValues.size(); i++) {
                if (itemCommentWrapper == mValues.get(i)) {
                    retVal = progress;
                    break;
                } else {
                    progress += 1 + mValues.get(i).getDescendants();
                }
            }
        }
        return retVal;
    }

    public void notifyCommentAdd(int position, int size) {
        if (size > 1) {
            notifyItemRangeInserted(
                    (mMainStory != null ? 1 : 0) + position,
                    size);
        } else {
            notifyItemInserted((mMainStory != null ? 1 : 0) + position);
        }
    }

    public void notifyCommentRemove(int position, int size) {
        if (size > 1) {
            notifyItemRangeRemoved(
                    (mMainStory != null ? 1 : 0) + position,
                    size);
        } else {
            notifyItemRemoved((mMainStory != null ? 1 : 0) + position);
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
            size += (1 + itemCommentWrapper.getDescendants());
        }
        return size;
    }

    private ItemCommentWrapper getValuesItem(int position) {
        return getValuesItemHelper(mValues, position);
    }

    private ItemCommentWrapper getValuesItemHelper(List<ItemCommentWrapper> values, int position) {
        int progress = 0;
        for (int i = 0; i < values.size(); i++) {
            ItemCommentWrapper itemCommentWrapper = values.get(i);
            int subDescendants = itemCommentWrapper.getDescendants();
            if (position == progress) {
                return itemCommentWrapper;
            } else if (position >= progress + 1 && position < progress + 1 + subDescendants) {
                return getValuesItemHelper(itemCommentWrapper.getKids(), position - (progress + 1));
            } else {
                progress += (1 + subDescendants);
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
                        .inflate(R.layout.fragment_stories, parent, false);
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
                        holderStory.mView.getResources().getColor(R.color.mediumGrey),
                        PorterDuff.Mode.SRC_ATOP);

                holderStory.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (null != mListener) {
                            // Notify the active callbacks interface (the activity, if the
                            // fragment is attached to one) that an item has been selected.
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
            ItemCommentWrapper itemWrapper = getValuesItem(position - 1);
            Item item;
            if (itemWrapper != null && itemWrapper.getItem() != null) {
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
                holderComment.mView.setVisibility(View.VISIBLE);
                holderComment.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (null != mListener) {
                            mListener.onListFragmentInteraction(holderComment.mItem);
                        }
                    }
                });
            } else {
                holderComment.mItem = null;
                holderComment.mAuthorTimeText.setText("");
                holderComment.mCommentText.setText("");
                holderComment.mView.setVisibility(View.GONE);
                holderComment.mView.setOnClickListener(null);
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
