package com.fmeyer.hackernews.views.listeners;

import com.fmeyer.hackernews.HackerNewsStoriesQuery.Story;

public interface StoryInteractionListener {

    enum STORY_CLICK_INTERACTION_TYPE {
        URL,
        COMMENTS
    }

    void onStoryInteraction(Story story, STORY_CLICK_INTERACTION_TYPE interactionType);
}
