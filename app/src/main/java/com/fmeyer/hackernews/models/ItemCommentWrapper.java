package com.fmeyer.hackernews.models;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ItemCommentWrapper {

    Item item;
    int depth;
    ItemCommentWrapper parent;
    int descendants = 0;
    boolean collapsed = false;
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
        return collapsed ? 0 : descendants;
    }

    public boolean shouldShow() {
        return item != null && !item.isDeleted() && !item.isDead();
    }

    public int getSize() {
        return (shouldShow() ? 1 : 0) + getDescendants();
    }

    public ItemCommentWrapper getParent() {
        return parent;
    }

    public @Nullable List<ItemCommentWrapper> getKids() {
        return collapsed ? null : kids;
    }

    public void setItem(Item item) {
        this.item = item;
        if (parent != null) {
            parent.updateDescendants();
        }
    }

    public boolean isCollapsed() {
        return collapsed;
    }

    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
        if (this.parent != null) {
            parent.updateDescendants();
        }
    }

    public void addKid(ItemCommentWrapper itemCommentWrapper) {
        kids.add(itemCommentWrapper);
        updateDescendants();
    }

    public void updateDescendants() {
        int descendants = 0;
        for (ItemCommentWrapper itemCommentWrapper : kids) {
            descendants += itemCommentWrapper.getSize();
        }
        this.descendants = descendants;
        if (parent != null) {
            parent.updateDescendants();
        }
    }
}
