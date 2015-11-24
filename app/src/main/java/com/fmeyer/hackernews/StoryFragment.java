package com.fmeyer.hackernews;

import android.content.Context;
import android.os.Bundle;
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

import java.util.HashSet;
import java.util.Set;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class StoryFragment extends Fragment {

    private static final String ARG_STORY_ITEM = "story_item";

    private Item mStoryItem;
    private OnListFragmentInteractionListener mListener;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeContainer;
    private LinearLayoutManager mLayoutManager;
    private StoryAdapter mAdapter;
    private Runnable mRefreshingRunnable;

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

        mRecyclerView = (RecyclerView) view.findViewById(R.id.list);
        mSwipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);

        mRecyclerView.setBackgroundResource(R.color.mediumGrey);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity()));

        // Set the adapter
        Context context = mRecyclerView.getContext();
        mLayoutManager = new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new StoryAdapter(mListener);
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
                        Item item = dataSnapshot.getValue(Item.class);
                        mStoryItem = item;
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
                int descendants = itemCommentWrapper.getDescendants();
                mAdapter.notifyCommentAdd(
                        mAdapter.getPositionForWrapper(itemCommentWrapper),
                        1 + descendants);
            }
            mLoadingComments.add(itemCommentWrapper);
            commentQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Item item = dataSnapshot.getValue(Item.class);
                    if (item != null && !item.isDead() && !item.isDeleted()) {
                        itemCommentWrapper.setItem(item);
                        mAdapter.notifyCommentChange(
                                mAdapter.getPositionForWrapper(itemCommentWrapper));
                        if (item.getKids() != null && !item.getKids().isEmpty()) {
                            fetchComments(itemCommentWrapper, item, depth + 1);
                        }
                    } else {
                        if (parentWrapper == null) {
                            mAdapter.removeComment(itemCommentWrapper);
                        } else {
                            int removePosition = mAdapter.getPositionForWrapper(itemCommentWrapper);
                            int descendants = itemCommentWrapper.getDescendants();
                            parentWrapper.removeKid(itemCommentWrapper);
                            mAdapter.notifyCommentRemove(removePosition, 1 + descendants);
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
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(Item item);
    }
}
