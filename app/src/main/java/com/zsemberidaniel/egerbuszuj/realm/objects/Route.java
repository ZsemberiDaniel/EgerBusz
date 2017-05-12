package com.zsemberidaniel.egerbuszuj.realm.objects;

import io.realm.RealmObject;

/**
 * Created by zsemberi.daniel on 2017. 05. 12..
 */

public class Route extends RealmObject {

    public static final String CN_ID = "id";

    private String id;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}
