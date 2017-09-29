package com.example.natepowers.telemetrytoyapp;

/**
 * Created by natepowers on 9/28/17.
 */

class CurrentLocationSingleton {
    private static final CurrentLocationSingleton ourInstance = new CurrentLocationSingleton();

    static CurrentLocationSingleton getInstance() {
        return ourInstance;
    }

    private CurrentLocationSingleton() {
    }

    static double lat = 0.0;
    static double lng = 0.0;

    public static double getLat() {
        return lat;
    }

    public static void setLat(double lat1) {
        lat = lat1;
    }

    public static double getLng() {
        return lng;
    }

    public static void setLng(double lng1) {
        lng = lng1;
    }
}
