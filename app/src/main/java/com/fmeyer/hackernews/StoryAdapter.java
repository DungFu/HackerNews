package com.fmeyer.hackernews;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    public void clear() {
        mValues.clear();
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
                holderStory.mScoreCommentsView.setText(
                        Integer.toString(mMainStory.getScore()) + "\n" + Integer.toString(mMainStory.getDescendants()));
                holderStory.mTitleView.setText(mMainStory.getTitle());
                holderStory.mView.setBackgroundResource(R.color.white);

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
                holderStory.mScoreCommentsView.setText("");
                holderStory.mTitleView.setText("");
            }
        } else if (holder instanceof ViewHolderComment) {
            final ViewHolderComment holderComment = (ViewHolderComment) holder;
            ItemCommentWrapper itemWrapper = getValuesItem(position - 1);
            Item item;
            if (itemWrapper != null && itemWrapper.getItem() != null) {
                item = itemWrapper.getItem();
                holderComment.mItem = item;

                holderComment.mCommentText.setText(
                        item.isDeleted() || item.isDead() ?
                                Html.fromHtml("<i>deleted</i>") :
                                Html.fromHtml("<span>" + item.getText() + "</span>"));
                holderComment.mAuthor.setText(item.getBy());
                holderComment.mAuthor.setVisibility(
                        item.isDeleted() || item.isDead() ?
                                View.GONE :
                                View.VISIBLE);
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
                holderComment.mView.setPadding(20 * itemWrapper.getDepth(), 0, 0, 0);
                holderComment.mView.setVisibility(View.VISIBLE);
                if ((position - 1) % 2 == 1) {
                    holderComment.mView.setBackgroundResource(R.color.lightGrey);
                } else {
                    holderComment.mView.setBackgroundResource(R.color.white);
                }

                holderComment.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (null != mListener) {
                            // Notify the active callbacks interface (the activity, if the
                            // fragment is attached to one) that an item has been selected.
                            mListener.onListFragmentInteraction(holderComment.mItem);
                        }
                    }
                });
            } else {
                holderComment.mItem = null;
                holderComment.mCommentText.setText("");
                holderComment.mView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return (mMainStory != null ? 1 : 0) + getValuesSize();
    }

    public class ViewHolderStory extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mScoreCommentsView;
        public final TextView mTitleView;
        public Item mItem;

        public ViewHolderStory(View view) {
            super(view);
            mView = view;
            mScoreCommentsView = (TextView) view.findViewById(R.id.score_comments);
            mTitleView = (TextView) view.findViewById(R.id.title);
        }
    }

    public class ViewHolderLoading extends RecyclerView.ViewHolder {
        public final View mView;
        public final ProgressBar mProgressBar;

        public ViewHolderLoading(View view) {
            super(view);
            mView = view;
            mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        }
    }

    public class ViewHolderComment extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mCommentText;
        public final TextView mAuthor;
        public Item mItem;

        public ViewHolderComment(View view) {
            super(view);
            mView = view;
            mCommentText = (TextView) view.findViewById(R.id.comment_text);
            mAuthor = (TextView) view.findViewById(R.id.author);
        }
    }
}
