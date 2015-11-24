package com.fmeyer.hackernews.models;

import java.util.ArrayList;
import java.util.List;

public class ItemCommentWrapper {

    Item item;
    int depth;
    ItemCommentWrapper parent;
    int descendants = 0;
    List<ItemCommentWrapper> kids = new ArrayList<>();

    public ItemCommentWrapper(ItemCommentWrapper parent, int depth) {
        this.parent = parent;
        this.depth = depth;
    }

    public Item getItem() {
        return item;
    }

    public int getDepth() {
        return depth;
    }

    public int getDescendants() {
        return descendants;
    }

    public ItemCommentWrapper getParent() {
        return parent;
    }

    public List<ItemCommentWrapper> getKids() {
        return kids;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public void addKid(ItemCommentWrapper itemCommentWrapper) {
        kids.add(itemCommentWrapper);
        updateDescendants();
    }

    public void removeKid(ItemCommentWrapper itemCommentWrapper) {
        kids.remove(itemCommentWrapper);
        updateDescendants();
    }

    public void updateDescendants() {
        int descendants = 0;
        for (ItemCommentWrapper itemCommentWrapper : kids) {
            descendants += (1 + itemCommentWrapper.getDescendants());
        }
        this.descendants = descendants;
        if (parent != null) {
            parent.updateDescendants();
        }
    }
}
