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

import java.util.List;

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
    private List<ItemCommentWrapper> mComments;

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

        // Set the adapter
        Context context = mRecyclerView.getContext();
        mLayoutManager = new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new StoryAdapter(mListener);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setMainStory(mStoryItem);
        fetchComments(null, mStoryItem, 0);

        mSwipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mAdapter.clear();
                fetchComments(null, mStoryItem, 0);
            }
        });

        return view;
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
            final Query commentQuery = ref
                    .child("item")
                    .child(Integer.toString(item.getKids().get(i)));
            final ItemCommentWrapper itemCommentWrapper = new ItemCommentWrapper();
            if (parentWrapper == null) {
                mSwipeContainer.setRefreshing(false);
                mAdapter.addComment(itemCommentWrapper);
            } else {
                parentWrapper.addKid(itemCommentWrapper);
            }
            commentQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Item item = dataSnapshot.getValue(Item.class);
                    itemCommentWrapper.setItem(item, depth);
                    if (item.getKids() == null || item.getKids().isEmpty()) {
//                        mAdapter.notifyDataSetChanged();
                    } else {
                        fetchComments(itemCommentWrapper, item, depth + 1);
                    }
                    mAdapter.notifyDataSetChanged();
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
        // TODO: Update argument type and name
        void onListFragmentInteraction(Item item);
    }
}
