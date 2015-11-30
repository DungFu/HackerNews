package com.fmeyer.hackernews.views.listeners;

import com.fmeyer.hackernews.models.Item;

public interface StoryInteractionListener {

    enum STORY_CLICK_INTERACTION_TYPE {
        URL,
        COMMENTS
    }

    void onStoryInteraction(Item item, STORY_CLICK_INTERACTION_TYPE interactionType);
}
