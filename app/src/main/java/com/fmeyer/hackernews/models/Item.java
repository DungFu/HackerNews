package com.fmeyer.hackernews.models;

import android.databinding.Bindable;
import android.databinding.Observable;
import android.os.Parcelable;

import io.requery.CascadeAction;
import io.requery.Entity;
import io.requery.Key;
import io.requery.ManyToOne;
import io.requery.OneToMany;
import io.requery.Persistable;
import io.requery.query.MutableResult;

@Entity
public interface Item extends Observable, Parcelable, Persistable {

    @Bindable
    @Key
    int getId();

    void setId(int id);

    @Bindable
    boolean getDeleted();

    void setDeleted(boolean deleted);

    @Bindable
    String getType();

    void setType(String type);

    @Bindable
    String getBy();

    void setBy(String by);

    @Bindable
    int getTime();

    void setTime(int time);

    @Bindable
    String getText();

    void setText(String text);

    @Bindable
    boolean getDead();

    void setDead(boolean dead);

    @Bindable
    @ManyToOne(cascade = {CascadeAction.DELETE, CascadeAction.SAVE})
    Item getParent();

    void setParent(Item parent);

    @OneToMany(mappedBy = "parent", cascade = {CascadeAction.DELETE, CascadeAction.SAVE})
    MutableResult<Item> getKids();

    @Bindable
    String getUrl();

    void setUrl(String url);

    @Bindable
    int getScore();

    void setScore(int score);

    @Bindable
    String getTitle();

    void setTitle(String title);

    @OneToMany(mappedBy = "parent", cascade = {CascadeAction.DELETE, CascadeAction.SAVE})
    MutableResult<Item> getParts();

    @Bindable
    int getDescendants();

    void setDescendants(int descendants);

    @Bindable
    boolean getCollapsed();

    void setCollapsed(boolean collapsed);

    @Bindable
    int getDepth();

    void setDepth(int depth);

    default public boolean shouldShow() {
        return !this.getDeleted() && !this.getDead();
    }

    default public int getSize() {
        return (shouldShow() ? 1 : 0) + getInnerSize();
    }

    default public int getInnerSize() {
        return getCollapsed() ? 0 : getDescendants();
    }
}
