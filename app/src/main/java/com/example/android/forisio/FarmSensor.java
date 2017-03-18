package com.example.android.forisio;

/**
 * Created by shital on 10/20/16.
 */
public class FarmSensor {
    private int sensorID;
    private double lat;
    private double lang;

    public void FarmSensor(){

    }

    public int getSensorID() {
        return sensorID;
    }

    public void setSensorID(int sensorID) {
        this.sensorID = sensorID;
    }

    public double getLang() {
        return lang;
    }

    public void setLang(double lang) {
        this.lang = lang;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }
}
