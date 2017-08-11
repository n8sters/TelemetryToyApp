package com.example.natepowers.telemetrytoyapp;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;

/**
 * Created by:
 * ~~~~~~_  __     __        ____      ______
 * ~~~~~/ |/ ___ _/ /____   / __/___  /_  _____  ___ __ __
 * ~~~~/    / _ `/ __/ -_)  > _/_ _/   / / / _ \/ _ / // /
 * ~~~/_/|_/\_,_/\__/\__/  |_____/    /_/  \___/_//_\_, /
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~/___/
 * ~~~~~~~~~~~~~~~~~~~  at Copia PBC   ~~~~~~~~~~~~~~~~~~~~~~
 */

class TelemetrySingleton extends Application implements LocationListener, SensorEventListener {

    private static final TelemetrySingleton ourInstance = new TelemetrySingleton();

    static TelemetrySingleton getInstance() {
        return ourInstance;
    }

    private TelemetrySingleton() {
    }

    private static final String TAG = TelemetryApplicationClass.class.getSimpleName();

    double lat, lng, acc, course, alt;
    boolean stopLoop = false;
    protected LocationManager locationManager;
    public static final int NORMAL_CLOSE_STATUS = 1000;

    Context context;

    @Override
    public void onLocationChanged(Location location) {

        if (location != null) {
            TelemtryMethods tel = new TelemtryMethods();

            lat = (location.getLatitude());
            lng = (location.getLongitude());
            alt = Double.parseDouble(tel.getAltutude(location));
            acc = tel.getAccuracy(location);

            Log.e(TAG, "onCreate: bearing: " + location.getBearing());
            Log.e(TAG, "doAThing: acc: " + tel.getAccuracy(location));
        } else {
            Log.e(TAG, "doAThing: Location was null");
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude", "disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude", "enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude", "status");
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // get the angle around the z-axis rotated
        course = (sensorEvent.values[0]);
        Log.e(TAG, "onSensorChanged: course: " + course);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private final class WebSocketListener extends okhttp3.WebSocketListener {

        TelemtryMethods tel = new TelemtryMethods();

        @Override
        public void onOpen(final WebSocket webSocket, Response response) {
            // set up runnable handler
            Handler handler1 = new Handler(Looper.getMainLooper());

            for (int i = 0; i < 720; i++) {
                // set timeout thread
                handler1.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        long ts = Long.parseLong(tel.createTimeStamp());
                        String id = tel.createUUID();

                        float battery = getBatteryPercentage(TelemetryApplicationClass.getAppContext());

                        String token = "eyJhbGciOiJIUzI1NiJ9.eyJVU0lEIjoiOTFkNTI4NzhjMTgxYWRmNDY4OGU2ODA0ZThkODU0NTA2NzUzMmQ0MyIsInRzIjoxNTAwNTg0ODY4fQ.D5A9WaoA-D3B0XWUAlsFHBs0yRJdd5_5gS_1lcxS-WU";

                        TelemetryPacket packet = new TelemetryPacket();

                        TelemetryPacket.PayloadBean data = new TelemetryPacket.PayloadBean();
                        packet.setToken(token);
                        packet.setMessageId(id);
                        data.setAlt(alt);
                        data.setLat(lat);
                        data.setLng(lng);
                        data.setCourse(course);
                        data.setBatt(battery);
                        data.setTs(ts);
                        packet.setMessageId(id);
                        packet.setPayload(data);

                        Long tsLong = System.currentTimeMillis() / 1000;
                        packet.setTs(tsLong);

                        Gson gson = new Gson();
                        String json = gson.toJson(packet);

                        // send packet
                        webSocket.send(json);

                        Log.e(TAG, "run: Packet Sent: " + json);
                        Log.e("", "\n\n");
                    }

                    //  todo change timeout based on battery, internet, etc
                }, 3000 * i); // currently set to 1 second

            }


        }


        @Override
        public void onMessage(WebSocket webSocket, String text) {
            Log.e(TAG, "onMessage: " + text);

        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            webSocket.close(NORMAL_CLOSE_STATUS, null);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        }
    }

    WebSocket webSocket;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    void start() {

        Request request = new Request.Builder().url("ws://ec2-34-210-213-56.us-west-2.compute.amazonaws.com:8080").build();
        WebSocketListener listener = new WebSocketListener();
        OkHttpClient newClient = new OkHttpClient();
        webSocket = newClient.newWebSocket(request, listener);
        newClient.dispatcher().executorService().shutdown();
        doAThing(TelemetryApplicationClass.getAppContext());

    }

    public void doAThing(Context c) {

        locationManager = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, false);
        android.location.Location location = locationManager.getLastKnownLocation(bestProvider);

    }


    public void stop() {
        stopLoop = true;
        webSocket.close(NORMAL_CLOSE_STATUS, " tracking not needed");
    }


    // simple battery percentage
    public static float getBatteryPercentage(Context context) {

        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, iFilter);

        int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
        int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;

        float batteryPct = level / (float) scale;

        return (batteryPct);
    }
}