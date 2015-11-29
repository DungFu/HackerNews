package com.fmeyer.hackernews.db;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.fmeyer.hackernews.models.Item;
import com.orm.SugarRecord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemDb extends SugarRecord {

    int itemid;
    boolean deleted;
    String type;
    String by;
    int time;
    String text;
    boolean dead;
    int parent;
    String kids;
    String url;
    int score;
    String title;
    String parts;
    int descendants;

    public static ItemDb createOrUpdateFromItem(Item item) {
        ItemDb itemDb = getItemDbFromId(item.getId());
        if (itemDb != null) {
            itemDb.updateFromItem(item);
        } else {
            itemDb = new ItemDb(
                    item.getId(),
                    item.isDeleted(),
                    item.getType(),
                    item.getBy(),
                    item.getTime(),
                    item.getText(),
                    item.isDead(),
                    item.getParent(),
                    item.getKids(),
                    item.getUrl(),
                    item.getScore(),
                    item.getTitle(),
                    item.getParts(),
                    item.getDescendants());
        }
        itemDb.save();
        return itemDb;
    }

    public static @Nullable ItemDb getItemDbFromId(int id) {
        List<ItemDb> itemDbList = ItemDb.find(ItemDb.class, "itemid = ?", Integer.toString(id));
        if (itemDbList.size() == 1) {
            return itemDbList.get(0);
        }
        return null;
    }

    public ItemDb() {
    }

    public ItemDb(
            int id,
            boolean deleted,
            String type,
            String by,
            int time,
            String text,
            boolean dead,
            int parent,
            ArrayList<Integer> kids,
            String url,
            int score,
            String title,
            ArrayList<Integer> parts,
            int descendants) {
        this.itemid = id;
        this.deleted = deleted;
        this.type = type;
        this.by = by;
        this.time = time;
        this.text = text;
        this.dead = dead;
        this.parent = parent;
        if (kids != null) {
            this.kids = TextUtils.join(",", kids);
        } else {
            this.kids = null;
        }
        this.url = url;
        this.score = score;
        this.title = title;
        if (parts != null) {
            this.parts = TextUtils.join(",", parts);
        } else {
            this.parts = null;
        }
        this.descendants = descendants;
    }

    public void updateFromItem(Item item) {
        this.itemid = item.getId();
        this.deleted = item.isDeleted();
        this.type = item.getType();
        this.by = item.getBy();
        this.time = item.getTime();
        this.text = item.getText();
        this.dead = item.isDead();
        this.parent = item.getParent();
        if (item.getKids() != null) {
            this.kids = TextUtils.join(",", item.getKids());
        } else {
            this.kids = null;
        }
        this.url = item.getUrl();
        this.score = item.getScore();
        this.title = item.getTitle();
        if (item.getParts() != null) {
            this.parts = TextUtils.join(",", item.getParts());
        } else {
            this.parts = null;
        }
        this.descendants = item.getDescendants();
        save();
    }

    public Item getItem() {
        return new Item(this);
    }

    public int getItemId() {
        return itemid;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public String getType() {
        return type;
    }

    public String getBy() {
        return by;
    }

    public int getTime() {
        return time;
    }

    public String getText() {
        return text;
    }

    public boolean isDead() {
        return dead;
    }

    public int getParent() {
        return parent;
    }

    public ArrayList<Integer> getKids() {
        if (kids != null) {
            ArrayList<String> strList = new ArrayList<String>(Arrays.asList(kids.split(",")));
            ArrayList<Integer> intList = new ArrayList<>();
            for (String s : strList) intList.add(Integer.valueOf(s));
            return intList;
        }
        return null;
    }

    public String getUrl() {
        return url;
    }

    public int getScore() {
        return score;
    }

    public String getTitle() {
        return title;
    }

    public ArrayList<Integer> getParts() {
        if (parts != null) {
            ArrayList<String> strList = new ArrayList<String>(Arrays.asList(parts.split(",")));
            ArrayList<Integer> intList = new ArrayList<>();
            for (String s : strList) intList.add(Integer.valueOf(s));
            return intList;
        }
        return null;
    }

    public int getDescendants() {
        return descendants;
    }
}
