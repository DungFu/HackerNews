package com.fmeyer.hackernews;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fmeyer.hackernews.StoriesFragment.OnListFragmentInteractionListener;
import com.fmeyer.hackernews.models.Item;
import com.fmeyer.hackernews.models.ItemWrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Item} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class StoriesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int STORY_VIEW_TYPE = 0;
    private static final int LOADING_VIEW_TYPE = 1;

    private final List<ItemWrapper> mValues = new ArrayList<>();
    private final OnListFragmentInteractionListener mListener;

    public StoriesAdapter(OnListFragmentInteractionListener listener) {
        mListener = listener;
    }

    public int addStory(ItemWrapper itemWrapper) {
        mValues.add(itemWrapper);
        int position = mValues.size() - 1;
        notifyItemChanged(position);
        return position;
    }

    public void clear() {
        mValues.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mValues.size()) {
            return LOADING_VIEW_TYPE;
        } else {
            return STORY_VIEW_TYPE;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case LOADING_VIEW_TYPE:
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_loading, parent, false);
                return new ViewHolderLoading(view);
            case STORY_VIEW_TYPE:
            default:
                View viewStory = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_stories, parent, false);
                return new ViewHolderStory(viewStory);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof  ViewHolderStory) {
            final ViewHolderStory holderStory = (ViewHolderStory) holder;
            ItemWrapper itemWrapper = mValues.get(position);
            Item item;
            if (itemWrapper.getItem() != null) {
                item = itemWrapper.getItem();
                holderStory.mItem = item;
                holderStory.mScoreCommentsView.setText(
                        Integer.toString(item.getScore()) + "\n" + Integer.toString(item.getDescendants()));
                holderStory.mTitleView.setText(item.getTitle());
                if (position % 2 == 1) {
                    holderStory.mView.setBackgroundResource(R.color.lightGrey);
                } else {
                    holderStory.mView.setBackgroundResource(R.color.white);
                }

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
        } else if (holder instanceof  ViewHolderLoading) {
            // do nothing
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size() + 1;
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
}
