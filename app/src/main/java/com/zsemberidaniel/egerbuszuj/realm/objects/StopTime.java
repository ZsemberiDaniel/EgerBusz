package com.zsemberidaniel.egerbuszuj.realm.objects;

import io.realm.RealmObject;

/**
 * Created by zsemberi.daniel on 2017. 05. 12..
 */

public class StopTime extends RealmObject {

    public static final String CN_TRIP = "trip";
    public static final String CN_STOP = "stop";
    public static final String CN_HOUR = "hour";
    public static final String CN_MINUTE = "minute";
    public static final String CN_SEQUENCE = "stopSequence";

    private Trip trip;
    private Stop stop;
    private byte hour;
    private byte minute;
    private int stopSequence;

    public Trip getTrip() { return trip; }
    public void setTrip(Trip trip) { this.trip = trip; }

    public Stop getStop() { return stop; }
    public void setStop(Stop stop) { this.stop = stop; }

    public byte getHour() { return hour; }
    public void setHour(byte hour) { this.hour = hour; }

    public byte getMinute() { return minute; }
    public void setMinute(byte minute) { this.minute = minute; }

    public int getStopSequence() { return stopSequence; }
    public void setStopSequence(int stopSequence) { this.stopSequence = stopSequence; }
}
