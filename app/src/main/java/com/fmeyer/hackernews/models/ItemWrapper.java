package com.fmeyer.hackernews.models;

public class ItemWrapper {

    Item item;

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public boolean shouldShow() {
        return item != null && !item.getDeleted() && !item.getDead();
    }
}
