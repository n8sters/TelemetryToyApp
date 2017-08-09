package com.example.natepowers.telemetrytoyapp;

import android.app.Application;
import android.util.Log;

/**
 * Created by:
 * ~~~~~~_  __     __        ____      ______
 * ~~~~~/ |/ ___ _/ /____   / __/___  /_  _____  ___ __ __
 * ~~~~/    / _ `/ __/ -_)  > _/_ _/   / / / _ \/ _ / // /
 * ~~~/_/|_/\_,_/\__/\__/  |_____/    /_/  \___/_//_\_, /
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~/___/
 * ~~~~~~~~~~~~~~~~~~~  at Copia PBC   ~~~~~~~~~~~~~~~~~~~~~~
 */

public class TelemetryApplicationClass extends Application {

    private static final String TAG = "TelemetryApplicationCla";

    @Override
    public void onCreate() {
        super.onCreate();

        Log.e(TAG, "onCreate: called" );
        MainActivity main = new MainActivity();
        //main.start();
    }
}
