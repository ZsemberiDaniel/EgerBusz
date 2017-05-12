package com.zsemberidaniel.egerbuszuj.realm.objects;

import io.realm.RealmObject;
/**
 * Created by zsemberi.daniel on 2017. 05. 12..
 */

public class Stop extends RealmObject {

    public static final String CN_ID = "id";
    public static final String CN_NAME = "name";
    public static final String CN_STARRED = "starred";

    private String id;
    private String name;
    private boolean starred;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isStarred() { return starred; }
    public void setStarred(boolean starred) { this.starred = starred; }
}
