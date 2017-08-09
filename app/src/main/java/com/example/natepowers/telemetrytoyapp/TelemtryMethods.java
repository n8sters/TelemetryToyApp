package com.example.natepowers.telemetrytoyapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.UUID;

/**
 * Created by:
 * ~~~~~~_  __     __        ____      ______
 * ~~~~~/ |/ ___ _/ /____   / __/___  /_  _____  ___ __ __
 * ~~~~/    / _ `/ __/ -_)  > _/_ _/   / / / _ \/ _ / // /
 * ~~~/_/|_/\_,_/\__/\__/  |_____/    /_/  \___/_//_\_, /
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~/___/
 * ~~~~~~~~~~~~~~~~~~~  at Copia PBC   ~~~~~~~~~~~~~~~~~~~~~~
 */

public class TelemtryMethods extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "TelemtryMethods";

    TelemtryMethods() {
    }

    // generate a random UUID
    String createUUID() {

        return UUID.randomUUID().toString();

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    // generate a time stamp
    String createTimeStamp() {
        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        return ts;
    }

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            //your code here
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }


    };

    // get accuracy
    public double getAccuracy(Location location) {
        float i = location.getAccuracy();
        return i;
    }

    // gets the altitude if the option exists, otherwise returns -1
    public String getAltutude(Location location) {
        if ( location.hasAltitude() ) {
            return String.valueOf(location.getAltitude());
        } else {
            return "-1";
        }
    }

    public String getLat ( Location location ) {
        return String.valueOf(location.getLatitude());
    }

    public String getLng(Location location) {
        return String.valueOf(location.getLongitude());
    }

    private void updateBatteryData(Intent intent ) {
        boolean present = intent.getBooleanExtra(BatteryManager.EXTRA_PRESENT, false);

        if ( present ) {
            StringBuilder batteryInfo = new StringBuilder();
            int health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);
            batteryInfo.append("Health: " + health).append("\n");
        } else {

        }
    }


    private BroadcastReceiver batteryInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateBatteryData(intent);
        }
    };

    // device sensor manager
    private SensorManager mSensorManager;

    @Override
    protected void onResume() {
        super.onResume();

        // for the system's orientation sensor registered listeners
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);
    }

    float course;

    public String getHeading(SensorEvent event) {
        float degree = Math.round(event.values[0]);
        return Float.toString(degree);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        // get the angle around the z-axis rotated
        float degree = Math.round(event.values[0]);

        Log.e(TAG, "onSensorChanged: " +  Float.toString(degree) + " degrees" );

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not in use
    }

}
