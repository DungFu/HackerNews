package com.fmeyer.hackernews.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.fmeyer.hackernews.db.ItemDb;

import java.util.ArrayList;

public class Item implements Parcelable {

    int id;
    boolean deleted;
    String type;
    String by;
    int time;
    String text;
    boolean dead;
    int parent;
    ArrayList<Integer> kids;
    String url;
    int score;
    String title;
    ArrayList<Integer> parts;
    int descendants;

    public Item() {
    }

    public Item(ItemDb itemDb) {
        this.id = itemDb.getItemId();
        this.deleted = itemDb.isDeleted();
        this.type = itemDb.getType();
        this.by = itemDb.getBy();
        this.time = itemDb.getTime();
        this.text = itemDb.getText();
        this.dead = itemDb.isDead();
        this.parent = itemDb.getParent();
        this.kids = itemDb.getKids();
        this.url = itemDb.getUrl();
        this.score = itemDb.getScore();
        this.title = itemDb.getTitle();
        this.parts = itemDb.getParts();
        this.descendants = itemDb.getDescendants();
    }

    @Override
    public String toString() {
        return "id:" + id + "\n" +
                "type:" + type + "\n" +
                "by:" + by + "\n" +
                "time:" + time + "\n" +
                "url:" + url + "\n" +
                "score:" + score + "\n" +
                "title:" + title;
    }

    public int getId() {
        return id;
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
        return kids;
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
        return parts;
    }

    public int getDescendants() {
        return descendants;
    }

    protected Item(Parcel in) {
        id = in.readInt();
        deleted = in.readByte() != 0x00;
        type = in.readString();
        by = in.readString();
        time = in.readInt();
        text = in.readString();
        dead = in.readByte() != 0x00;
        parent = in.readInt();
        if (in.readByte() == 0x01) {
            kids = new ArrayList<Integer>();
            in.readList(kids, Integer.class.getClassLoader());
        } else {
            kids = null;
        }
        url = in.readString();
        score = in.readInt();
        title = in.readString();
        if (in.readByte() == 0x01) {
            parts = new ArrayList<Integer>();
            in.readList(parts, Integer.class.getClassLoader());
        } else {
            parts = null;
        }
        descendants = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeByte((byte) (deleted ? 0x01 : 0x00));
        dest.writeString(type);
        dest.writeString(by);
        dest.writeInt(time);
        dest.writeString(text);
        dest.writeByte((byte) (dead ? 0x01 : 0x00));
        dest.writeInt(parent);
        if (kids == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(kids);
        }
        dest.writeString(url);
        dest.writeInt(score);
        dest.writeString(title);
        if (parts == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(parts);
        }
        dest.writeInt(descendants);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Item> CREATOR = new Parcelable.Creator<Item>() {
        @Override
        public Item createFromParcel(Parcel in) {
            return new Item(in);
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };
}
