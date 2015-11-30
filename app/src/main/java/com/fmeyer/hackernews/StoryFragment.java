package com.fmeyer.hackernews;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.fmeyer.hackernews.models.Item;
import com.fmeyer.hackernews.models.ItemCommentWrapper;
import com.fmeyer.hackernews.views.listeners.CommentInteractionListener;
import com.fmeyer.hackernews.views.listeners.StoryInteractionListener;

import java.util.HashSet;
import java.util.Set;

public class StoryFragment extends Fragment {

    private static final String ARG_STORY_ITEM = "story_item";

    private Item mStoryItem;
    private StoryInteractionListener mStoryListener;
    private CommentInteractionListener mCommentListener;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeContainer;
    private LinearLayoutManager mLayoutManager;
    private StoryAdapter mAdapter;
    private Runnable mRefreshingRunnable;
    private boolean mAnimationsEnabled;

    private final Set<ItemCommentWrapper> mLoadingComments = new HashSet<>();

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public StoryFragment() {
    }

    public static StoryFragment newInstance(Item item) {
        StoryFragment fragment = new StoryFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_STORY_ITEM, item);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mStoryItem = getArguments().getParcelable(ARG_STORY_ITEM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mAnimationsEnabled = prefs.getBoolean("animations_switch", true);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.list);
        mSwipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);

        mRecyclerView.setBackgroundResource(R.color.mediumGrey);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity()));
        if (!mAnimationsEnabled) {
            mRecyclerView.setItemAnimator(null);
        }

        // Set the adapter
        Context context = mRecyclerView.getContext();
        mLayoutManager = new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new StoryAdapter(mStoryListener, mCommentListener);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setMainStory(mStoryItem);
        fetchComments(null, mStoryItem, 0);
        if (mStoryItem.getDescendants() > 0) {
            setRefreshingState(true);
        }

        mSwipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mAdapter.clear();
                fetchStory(Integer.toString(mStoryItem.getId()));
            }
        });

        return view;
    }

    private void setRefreshingState(final boolean isRefreshing) {
        if (mRefreshingRunnable != null) {
            mSwipeContainer.removeCallbacks(mRefreshingRunnable);
            mRefreshingRunnable = null;
        }
        mRefreshingRunnable = new Runnable() {
            @Override
            public void run() {
                mSwipeContainer.setRefreshing(isRefreshing);
                mRefreshingRunnable = null;
            }
        };
        mSwipeContainer.post(mRefreshingRunnable);
    }

    private void fetchStory(String id) {
        Firebase ref = new Firebase("https://hacker-news.firebaseio.com/v0/");
        ref.child("item").child(id).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mStoryItem = dataSnapshot.getValue(Item.class);
                        mAdapter.setMainStory(mStoryItem);
                        if (mStoryItem.getDescendants() <= 0) {
                            setRefreshingState(false);
                        }
                        fetchComments(null, mStoryItem, 0);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                    }
                });
    }

    private void fetchComments(
            final ItemCommentWrapper parentWrapper,
            Item item,
            final int depth) {
        Firebase ref = new Firebase("https://hacker-news.firebaseio.com/v0/");
        if (item == null || item.getKids() == null || item.getKids().isEmpty()) {
            return;
        }
        for (int i = 0; i < item.getKids().size(); i++) {
            final String itemId = Integer.toString(item.getKids().get(i));
            final Query commentQuery = ref
                    .child("item")
                    .child(itemId);
            final ItemCommentWrapper itemCommentWrapper =
                    new ItemCommentWrapper(parentWrapper, depth);
            if (parentWrapper == null) {
                mAdapter.addComment(itemCommentWrapper);
            } else {
                parentWrapper.addKid(itemCommentWrapper);
            }
            mLoadingComments.add(itemCommentWrapper);
            commentQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Item item = dataSnapshot.getValue(Item.class);
                    boolean wasEmpty = itemCommentWrapper.getItem() == null;
                    if (item != null && !item.isDead() && !item.isDeleted()) {
                        itemCommentWrapper.setItem(item);
                        int position = mAdapter.getPositionForWrapper(itemCommentWrapper);
                        if (position != -1) {
                            if (wasEmpty) {
                                mAdapter.notifyCommentAdd(position, 1);
                            } else {
                                mAdapter.notifyCommentChange(position);
                            }
                        }
                        if (item.getKids() != null && !item.getKids().isEmpty()) {
                            fetchComments(itemCommentWrapper, item, depth + 1);
                        }
                    } else {
                        int removePosition = mAdapter.getPositionForWrapper(itemCommentWrapper);
                        itemCommentWrapper.setItem(item);
                        if (removePosition != -1) {
                            mAdapter.notifyCommentRemove(removePosition, 1);
                        }
                    }
                    mLoadingComments.remove(itemCommentWrapper);
                    if (mLoadingComments.isEmpty()) {
                        setRefreshingState(false);
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                }
            });
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof StoryInteractionListener && context instanceof CommentInteractionListener) {
            mStoryListener = (StoryInteractionListener) context;
            mCommentListener = (CommentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement StoryInteractionListener and CommentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mStoryListener = null;
        mCommentListener = null;
    }
}
