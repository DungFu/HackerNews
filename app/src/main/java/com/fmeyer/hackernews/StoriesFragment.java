package com.fmeyer.hackernews;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.fmeyer.hackernews.db.ItemDb;
import com.fmeyer.hackernews.db.StoriesDb;
import com.fmeyer.hackernews.models.Item;
import com.fmeyer.hackernews.models.ItemWrapper;
import com.fmeyer.hackernews.views.listeners.StoryInteractionListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A fragment representing a list of Items.
 */
public class StoriesFragment extends Fragment {

    private static final String ARG_FILTER_TYPE = "filter_type";

    private String mFilterType = "topstories";
    private StoryInteractionListener mStoryListener;
    private Firebase mRef;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeContainer;
    private LinearLayoutManager mLayoutManager;
    private StoriesAdapter mAdapter;
    private int mPage = 0;
    private boolean mHasMorePages = true;
    private Runnable mRefreshingRunnable;
    private boolean mFirstPageLoaded = false;
    private boolean mAnimationsEnabled;

    private final Set<Pair<Firebase, ValueEventListener>> mValueEventsListeners = new HashSet<>();
    private final Map<Integer, ItemWrapper> mItemWrappers = new HashMap<>();
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
        mLayoutManager = new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new StoriesAdapter(mStoryListener);
        mRecyclerView.setAdapter(mAdapter);

        mPage = 0;
        mFirstPageLoaded = false;

        mRef = new Firebase("https://hacker-news.firebaseio.com/v0/");
        fetchStoriesFromDb();
        fetchStoriesFromFirebase(10, mPage);
        setRefreshingState(true);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (mHasMorePages &&
                    mLayoutManager.findLastVisibleItemPosition() + 3 >= mLayoutManager.getItemCount() &&
                    mFirstPageLoaded) {
                    mPage++;
                    fetchStoriesFromFirebase(10, mPage);
                }
            }
        });

        mSwipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mItemWrappers.clear();
                mLoadingStories.clear();
                mAdapter.clear();
                mPage = 0;
                mFirstPageLoaded = false;
                mHasMorePages = true;
                destroyEventListeners();
                fetchStoriesFromDb();
                fetchStoriesFromFirebase(10, mPage);
            }
        });

        return view;
    }

    private void destroyEventListeners() {
        removeEventListeners();
        mValueEventsListeners.clear();
    }

    private void removeEventListeners() {
        for (Pair<Firebase, ValueEventListener> pair : mValueEventsListeners) {
            pair.first.removeEventListener(pair.second);
        }
    }

    private void reAddEventListeners() {
        for (Pair<Firebase, ValueEventListener> pair : mValueEventsListeners) {
            pair.first.addValueEventListener(pair.second);
        }
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

    private void fetchStoriesFromDb() {
        final StoriesDb storiesDb = StoriesDb.getStoriesDbFromFilterType(mFilterType);
        if (storiesDb != null) {
            for (Integer storyId : storiesDb.getStoryIds()) {
                fetchStoryFromDb(storyId);
            }
        }
    }

    private void fetchStoriesFromFirebase(int pageSize, final int pageNum) {
        final Query topStoriesQuery = mRef
                .child(mFilterType)
                .startAt(null, String.valueOf(pageSize*pageNum))
                .limitToFirst(pageSize);
        topStoriesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean isFirstPage = pageNum == 0;
                if (isFirstPage) {
                    mFirstPageLoaded = true;
                }
                boolean hasData = false;
                ArrayList<Integer> storyIdsDb = new ArrayList<>();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    storyIdsDb.add(child.getValue(Integer.class));
                }
                StoriesDb storiesDb = StoriesDb.getStoriesDbFromFilterType(mFilterType);
                if (isFirstPage) {

                    // Add new stories
                    for (int i = 0; i < storyIdsDb.size(); i++) {
                        getItemWrapperFromId(storyIdsDb.get(i), i);
                    }

                    // collect list of stories that are removed in sparse array
                    SparseArray<ItemWrapper> itemsToRemove = new SparseArray<>();
                    Iterator itRemove = mItemWrappers.entrySet().iterator();
                    while (itRemove.hasNext()) {
                        Map.Entry pair = (Map.Entry) itRemove.next();
                        ItemWrapper itemWrapper = (ItemWrapper) pair.getValue();
                        if (itemWrapper.getItem() != null &&
                                !storyIdsDb.contains(itemWrapper.getItem().getId())) {
                            itemsToRemove.put((int) pair.getKey(), itemWrapper);
                        }
                    }

                    // remove stories that are in sparse array
                    for (int i = 0; i < itemsToRemove.size(); i++) {
                        Integer itemKey = itemsToRemove.keyAt(i);
                        ItemWrapper itemWrapper = itemsToRemove.get(itemKey);
                        mItemWrappers.remove(itemKey);
                        mLoadingStories.remove(itemWrapper);
                        mAdapter.removeStory(itemWrapper);
                    }

                    // Move stories are neither new nor removed
                    Iterator itMove = mItemWrappers.entrySet().iterator();
                    while (itMove.hasNext()) {
                        Map.Entry pair = (Map.Entry) itMove.next();
                        ItemWrapper itemWrapper = (ItemWrapper) pair.getValue();
                        if (itemWrapper.getItem() != null &&
                                storyIdsDb.contains(itemWrapper.getItem().getId())) {
                            int newPosition = storyIdsDb.indexOf(pair.getKey());
                            mAdapter.moveStory(itemWrapper, newPosition);
                        }
                    }

                    // Update the database entry for this filter type
                    if (storiesDb != null) {
                        storiesDb.setStoryIds(storyIdsDb);
                        storiesDb.save();
                    } else {
                        storiesDb = new StoriesDb(mFilterType, storyIdsDb);
                        storiesDb.save();
                    }
                }
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    hasData = true;
                    Integer storyId = child.getValue(Integer.class);
                    fetchStoryFromFirebase(storyId, isFirstPage /* shouldStoreInDb */);
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

    private boolean needsUiUpdate(Item oldItem, Item newItem) {
        return oldItem == null ||
                newItem == null ||
                !oldItem.getTitle().equals(newItem.getTitle()) ||
                oldItem.getDescendants() != newItem.getDescendants() ||
                oldItem.getScore() != newItem.getScore() ||
                !oldItem.getBy().equals(newItem.getBy()) ||
                oldItem.getTime() != newItem.getTime();
    }

    private void updateAdapterWithNewItem(ItemWrapper itemWrapper, Item item) {
        boolean oldShouldShow = itemWrapper.shouldShow();
        int beforePosition = mAdapter.getPositionForItemWrapper(itemWrapper);
        Item oldItem = itemWrapper.getItem();
        itemWrapper.setItem(item);
        boolean newShouldShow = itemWrapper.shouldShow();
        if (!oldShouldShow && newShouldShow) {
            mAdapter.notifyAddStory(itemWrapper);
        } else if (oldShouldShow && newShouldShow) {
            if (needsUiUpdate(oldItem, itemWrapper.getItem())) {
                mAdapter.notifyUpdateStory(itemWrapper);
            }
        } else if (oldShouldShow && !newShouldShow) {
            mAdapter.notifyRemoveStory(beforePosition);
        }
    }

    private void fetchStoryFromDb(Integer itemId) {
        ItemDb itemDb = ItemDb.getItemDbFromId(itemId);
        if (itemDb != null) {
            ItemWrapper itemWrapper = getItemWrapperFromId(itemId, -1);
            Item item = new Item(itemDb);
            updateAdapterWithNewItem(itemWrapper, item);
        }
    }

    private ItemWrapper getItemWrapperFromId(Integer itemId, int position) {
        if (mItemWrappers.containsKey(itemId)) {
            return mItemWrappers.get(itemId);
        } else {
            ItemWrapper itemWrapper = new ItemWrapper();
            mItemWrappers.put(itemId, itemWrapper);
            if (position > -1) {
                mAdapter.addStory(itemWrapper, position);
            } else {
                mAdapter.addStory(itemWrapper);
            }
            mLoadingStories.add(itemWrapper);
            return itemWrapper;
        }
    }

    private void fetchStoryFromFirebase(Integer itemId, final boolean shouldStoreInDb) {
        final ItemWrapper itemWrapper = getItemWrapperFromId(itemId, -1);
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Item item = dataSnapshot.getValue(Item.class);
                if (item != null && shouldStoreInDb) {
                    ItemDb.createOrUpdateFromItem(item);
                }
                updateAdapterWithNewItem(itemWrapper, item);
                mLoadingStories.remove(itemWrapper);
                if (mLoadingStories.isEmpty()) {
                    setRefreshingState(false);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        };
        Firebase ref = mRef.child("item").child(itemId.toString());
        ref.addValueEventListener(listener);
        mValueEventsListeners.add(Pair.create(ref, listener));
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
    public void onPause() {
        super.onPause();
        removeEventListeners();
    }

    @Override
    public void onResume() {
        super.onResume();
        reAddEventListeners();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyEventListeners();
        mAdapter.clear();
        mItemWrappers.clear();
        mLoadingStories.clear();
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
