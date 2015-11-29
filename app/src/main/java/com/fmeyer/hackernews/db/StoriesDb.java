package com.fmeyer.hackernews.db;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.orm.SugarRecord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StoriesDb extends SugarRecord {

    String filtertype;
    String storyids;

    public static @Nullable StoriesDb getStoriesDbFromFilterType(String filterType) {
        List<StoriesDb> storiesDbList =
                StoriesDb.find(StoriesDb.class, "filtertype = ?", filterType);
        if (storiesDbList.size() == 1) {
            return storiesDbList.get(0);
        }
        return null;
    }

    public StoriesDb() {
    }

    public StoriesDb(String filtertype, ArrayList<Integer> storyids) {
        this.filtertype = filtertype;
        if (storyids != null) {
            this.storyids = TextUtils.join(",", storyids);
        } else {
            this.storyids = null;
        }
    }

    public void setStoryIds(ArrayList<Integer> storyIds) {
        this.storyids = TextUtils.join(",", storyIds);
    }

    public String getFilterType() {
        return filtertype;
    }

    public ArrayList<Integer> getStoryIds() {
        if (storyids != null && !storyids.equals("")) {
            ArrayList<String> strList = new ArrayList<String>(Arrays.asList(storyids.split(",")));
            ArrayList<Integer> intList = new ArrayList<>();
            for (String s : strList) intList.add(Integer.valueOf(s));
            return intList;
        }
        return null;
    }
}
