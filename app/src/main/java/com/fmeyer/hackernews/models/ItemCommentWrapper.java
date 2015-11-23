package com.fmeyer.hackernews.models;

import java.util.ArrayList;
import java.util.List;

public class ItemCommentWrapper {

    Item item;
    int depth;
    List<ItemCommentWrapper> kids = new ArrayList<>();

    public Item getItem() {
        return item;
    }

    public int getDepth() {
        return depth;
    }

    public int getDescendants() {
        int descendants = 0;
        for (ItemCommentWrapper itemCommentWrapper : kids) {
            descendants += (1 + itemCommentWrapper.getDescendants());
        }
        return descendants;
    }

    public List<ItemCommentWrapper> getKids() {
        return kids;
    }

    public void setItem(Item item, int depth) {
        this.item = item;
        this.depth = depth;
    }

    public void addKid(ItemCommentWrapper itemCommentWrapper) {
        kids.add(itemCommentWrapper);
    }
}
