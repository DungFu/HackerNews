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
import com.fmeyer.hackernews.models.ItemWrapper;

import java.util.HashSet;
import java.util.Set;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class StoriesFragment extends Fragment {

    public static enum CLICK_INTERACTION_TYPE {
        URL,
        COMMENTS
    }

    private static final String ARG_FILTER_TYPE = "filter_type";

    private String mFilterType = "topstories";
    private OnListFragmentInteractionListener mListener;
    private Firebase mRef;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeContainer;
    private LinearLayoutManager mLayoutManager;
    private StoriesAdapter mAdapter;
    private int mPage = 0;
    private boolean mHasMorePages = true;
    private Runnable mRefreshingRunnable;

    private final Set<ItemWrapper> mLoadingStories = new HashSet<>();

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public StoriesFragment() {
    }

    public static StoriesFragment newInstance(String filterType) {
        StoriesFragment fragment = new StoriesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FILTER_TYPE, filterType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mFilterType = getArguments().getString(ARG_FILTER_TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.list);
        mSwipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);

        mRecyclerView.setBackgroundResource(R.color.white);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity()));

        // Set the adapter
        Context context = mRecyclerView.getContext();
        mLayoutManager = new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new StoriesAdapter(mListener);
        mRecyclerView.setAdapter(mAdapter);

        mRef = new Firebase("https://hacker-news.firebaseio.com/v0/");
        fetchStories(10, mPage);
        setRefreshingState(true);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (mHasMorePages &&
                    mLayoutManager.findLastVisibleItemPosition() + 3 >= mLayoutManager.getItemCount()) {
                    mPage++;
                    fetchStories(10, mPage);
                }
            }
        });

        mSwipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mAdapter.clear();
                mPage = 0;
                mHasMorePages = true;
                fetchStories(10, mPage);
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

    private void fetchStories(int pageSize, int pageNum) {
        Firebase ref = new Firebase("https://hacker-news.firebaseio.com/v0/");
        final Query topStoriesQuery = ref
                .child(mFilterType)
                .startAt(null, String.valueOf(pageSize*pageNum))
                .limitToFirst(pageSize);
        topStoriesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean hasData = false;
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    hasData = true;
                    Integer itemId = child.getValue(Integer.class);
                    final ItemWrapper itemWrapper = new ItemWrapper();
                    mAdapter.addStory(itemWrapper);
                    mLoadingStories.add(itemWrapper);
                    mRef.child("item").child(itemId.toString()).addListenerForSingleValueEvent(
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Item item = dataSnapshot.getValue(Item.class);
                                    itemWrapper.setItem(item);
                                    mAdapter.notifyUpdateStory(itemWrapper);
                                    mLoadingStories.remove(itemWrapper);
                                    if (mLoadingStories.isEmpty()) {
                                        setRefreshingState(false);
                                    }
                                }

                                @Override
                                public void onCancelled(FirebaseError firebaseError) {
                                }
                            });
                }
                if (!hasData) {
                    mHasMorePages = false;
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
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
        void onListFragmentInteraction(Item item, CLICK_INTERACTION_TYPE interactionType);
    }
}
