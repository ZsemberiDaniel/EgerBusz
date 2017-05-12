package com.zsemberidaniel.egerbuszuj.realm.objects;

import io.realm.RealmObject;

/**
 * Created by zsemberi.daniel on 2017. 05. 12..
 */

public class Trip extends RealmObject {

    public static final String CN_ID = "id";
    public static final String CN_ROUTE_ID = "routeId";
    public static final String CN_DAY_TYPE = "dayType";
    public static final String CN_HEAD_SIGN = "headSign";
    public static final String CN_DIRECTION = "direction";

    private String id;
    private Route route;
    private String headSign;
    private int direction;
    private int dayType;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Route getRoute() { return route; }
    public void setRoute(Route route) { this.route = route; }

    public String getHeadSign() { return headSign; }
    public void setHeadSign(String headSign) { this.headSign = headSign; }

    public int getDirection() { return direction; }
    public void setDirection(int direction) { this.direction = direction; }

    public int getDayType() { return dayType; }

    public void setDayType(int dayType) { this.dayType = dayType; }
}
