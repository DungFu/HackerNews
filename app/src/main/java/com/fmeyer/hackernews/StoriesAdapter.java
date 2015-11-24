package com.fmeyer.hackernews;

import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

    private final List<ItemWrapper> mValues = new ArrayList<>();
    private final OnListFragmentInteractionListener mListener;

    public StoriesAdapter(OnListFragmentInteractionListener listener) {
        mListener = listener;
    }

    public void addStory(ItemWrapper itemWrapper) {
        int count = mValues.size();
        mValues.add(itemWrapper);
        notifyItemInserted(count);
    }

    public void notifyUpdateStory(ItemWrapper itemWrapper) {
        int index = mValues.indexOf(itemWrapper);
        if (index != -1) {
            notifyItemChanged(index);
        }
    }

    public void clear() {
        mValues.clear();
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View viewStory = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_stories, parent, false);
        return new ViewHolderStory(viewStory);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final ViewHolderStory holderStory = (ViewHolderStory) holder;
        ItemWrapper itemWrapper = mValues.get(position);
        Item item;
        if (itemWrapper.getItem() != null) {
            item = itemWrapper.getItem();
            holderStory.mItem = item;
            holderStory.mCommentsView.setText(Integer.toString(item.getDescendants()));
            holderStory.mTitleView.setText(item.getTitle());
            holderStory.mSubtitleView.setText(
                    String.format(
                            holderStory.mView.getResources().getString(R.string.post_subtitle),
                            Integer.toString(item.getScore()),
                            item.getBy(),
                            DateUtils.getRelativeTimeSpanString(
                                    (long) item.getTime() * (long) 1000)));
            holderStory.mCommentsImageView.getDrawable().setColorFilter(
                    holderStory.mView.getResources().getColor(R.color.mediumGrey),
                    PorterDuff.Mode.SRC_ATOP);
            holderStory.mView.setVisibility(View.VISIBLE);
            holderStory.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mListener) {
                        mListener.onListFragmentInteraction(
                                holderStory.mItem,
                                StoriesFragment.CLICK_INTERACTION_TYPE.URL);
                    }
                }
            });
            holderStory.mCommentsContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mListener) {
                        mListener.onListFragmentInteraction(
                                holderStory.mItem,
                                StoriesFragment.CLICK_INTERACTION_TYPE.COMMENTS);
                    }
                }
            });
        } else {
            holderStory.mItem = null;
            holderStory.mCommentsView.setText("");
            holderStory.mTitleView.setText("");
            holderStory.mSubtitleView.setText("");
            holderStory.mView.setVisibility(View.GONE);
            holderStory.mView.setOnClickListener(null);
            holderStory.mCommentsContainer.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size();
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
}
