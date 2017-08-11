package com.example.natepowers.telemetrytoyapp;

import android.app.Application;
import android.content.Context;

/**
 * Created by:
 * ~~~~~~_  __     __
 * ~~~~~/ |/ ___ _/ /____
 * ~~~~/    / _ `/ __/ -_)
 * ~~~/_/|_/\_,_/\__/\__/
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * ~~~~~~~~~~~~~~~~~~~  at Copia PBC   ~~~~~~~~~~~~
 */

public class TelemetryApplicationClass extends Application {

    private static Context context;

    public void onCreate() {
        super.onCreate();
        TelemetryApplicationClass.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return TelemetryApplicationClass.context;
    }


}
