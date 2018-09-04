package com.fmeyer.hackernews;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.fmeyer.hackernews.HackerNewsStoriesQuery.Story;
import com.fmeyer.hackernews.views.listeners.StoryInteractionListener;

import org.jetbrains.annotations.NotNull;

/**
 * A fragment representing a list of Items.
 */
public class StoriesFragment extends Fragment {

    private static final String ARG_FILTER_TYPE = "filter_type";
    private static final int LOAD_MORE_ITEMS_BUFFER_SIZE = 3;

    private String mFilterType = "topstories";
    private StoryInteractionListener mStoryListener;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeContainer;
    private ScrollSpeedLinearLayoutManager mLayoutManager;
    private StoriesAdapter mAdapter;
    private ApolloClient mApolloClient;
    private boolean mHasMorePages = true;
    private Runnable mRefreshingRunnable;
    private boolean mAnimationsEnabled;
    private int mPrevLastVisibleItemLoadMore = -10;

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

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mAnimationsEnabled = prefs.getBoolean("animations_switch", true);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.list);
        mSwipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);

        mRecyclerView.setBackgroundResource(R.color.white);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity()));
        if (!mAnimationsEnabled) {
            mRecyclerView.setItemAnimator(null);
        }

        // Set the adapter
        Context context = mRecyclerView.getContext();
        mLayoutManager = new ScrollSpeedLinearLayoutManager(context);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new StoriesAdapter(mStoryListener);
        mRecyclerView.setAdapter(mAdapter);

        mApolloClient = ApolloClient.builder()
                .serverUrl("https://dungfu-hackernews-graphqlserver.glitch.me")
                .build();

        fetchStories(10);
        setRefreshingState(true);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int lastVisibleItemPosition = mLayoutManager.findLastVisibleItemPosition();

                int lastVisiblePlusBuffer = lastVisibleItemPosition + LOAD_MORE_ITEMS_BUFFER_SIZE;

                if (mPrevLastVisibleItemLoadMore + 5 < lastVisibleItemPosition &&
                    mHasMorePages &&
                    lastVisiblePlusBuffer >= mLayoutManager.getItemCount()) {
                    fetchStories(10);
                    mPrevLastVisibleItemLoadMore = lastVisibleItemPosition;
                }
            }
        });

        mSwipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mAdapter.clear();
                mHasMorePages = true;
                fetchStories(10);
            }
        });

        return view;
    }

    private void setRefreshingState(final boolean isRefreshing) {
        if (mSwipeContainer != null) {
            if (mRefreshingRunnable != null) {
                mSwipeContainer.removeCallbacks(mRefreshingRunnable);
                mRefreshingRunnable = null;
            }
            mRefreshingRunnable = new Runnable() {
                @Override
                public void run() {
                    if (mSwipeContainer != null) {
                        mSwipeContainer.setRefreshing(isRefreshing);
                    }
                    mRefreshingRunnable = null;
                }
            };
            mSwipeContainer.post(mRefreshingRunnable);
        }
    }

    private void fetchStories(int pageSize) {
        HackerNewsStoriesQuery.Builder builder = HackerNewsStoriesQuery.builder()
                .category(mFilterType)
                .first(pageSize);
        String lastId = mAdapter.getLastId();
        if (lastId != null) {
            builder.after(lastId);
        }
        mApolloClient.query(
                builder.build()
        ).enqueue(new ApolloCall.Callback<HackerNewsStoriesQuery.Data>() {

            @Override public void onResponse(@NotNull final Response<HackerNewsStoriesQuery.Data> dataResponse) {
                if (dataResponse.data() != null) {
                    mRecyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.add(dataResponse.data().stories());
                            setRefreshingState(false);
                        }
                    });
                }
            }

            @Override public void onFailure(@NotNull ApolloException e) {
                Log.e("WOW", e.getMessage(), e);
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof StoryInteractionListener) {
            mStoryListener = (StoryInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mStoryListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAdapter.clear();
        if (mSwipeContainer != null) {
            mSwipeContainer.setRefreshing(false);
            if (mRefreshingRunnable != null) {
                mSwipeContainer.removeCallbacks(mRefreshingRunnable);
            }
        }
        mRefreshingRunnable = null;
        mSwipeContainer = null;
    }
}
