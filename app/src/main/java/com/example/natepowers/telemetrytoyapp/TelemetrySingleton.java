package com.example.natepowers.telemetrytoyapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.nearby.messages.Distance;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;

/**
 * Created by:
 * ~~~~~~~~~_  __     __
 * ~~~~~~~~/ |/ ___ _/ /____
 * ~~~~~~~/    / _ `/ __/ -_)
 * ~~~~~~/_/|_/\_,_/\__/\__/
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * ~~~~~~~~~~~~~~~~~~~  at Copia PBC   ~~~~~~~
 */

class TelemetrySingleton extends Application implements LocationListener, SensorEventListener {

    private static final TelemetrySingleton ourInstance = new TelemetrySingleton();

    static TelemetrySingleton getInstance() {
        return ourInstance;
    }

    private TelemetrySingleton() {
    }

    private static final String TAG = TelemetrySingleton.class.getSimpleName();

    double lat, lng, acc, course, alt;
    boolean telemetryLoopBoolean = true;
    boolean socketLoopBoolean = true;
    protected LocationManager locationManager;
    public static final int NORMAL_CLOSE_STATUS = 1000;
    WebSocket webSocket;

    // a queue for holding the packets to be sent off
    Queue<TelemetryPacket> packetQueue = new LinkedList<>();

    // a map for referencing the sent packets, so none get lost
    HashMap<String, TelemetryPacket> packetMap = new HashMap<>();

    Context context = TelemetryApplicationClass.getAppContext();

    int timeout = 1000;

    String UUID = "6fab693e3db5a4b9";
    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    public void onLocationChanged(Location location) {

        if (location != null) {
            TelemetryMethods tel = new TelemetryMethods();

            lat = (location.getLatitude());
            lng = (location.getLongitude());
            alt = Double.parseDouble(tel.getAltutude(location));
            acc = tel.getAccuracy(location);
            course = location.getBearing();

            CurrentLocationSingleton.setLat(lat);
            CurrentLocationSingleton.setLng(lng);

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
        Log.e(TAG, "onSensorChanged: course at onSensorChanged: " + sensorEvent.values[0]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    Looper myLooper = Looper.myLooper();
    Handler handler1 = new Handler(myLooper);

    private final class WebSocketListener extends okhttp3.WebSocketListener {

        TelemetryMethods tel = new TelemetryMethods();


        @Override
        public void onOpen(final WebSocket webSocket, Response response) {

            final Runnable runnable = new Runnable() {
                public void run() {

                    int i = 1;
                    if (socketLoopBoolean) {
                        handler1.postDelayed(this, timeout * i++);
                        if (!packetQueue.isEmpty()) {
                            TelemetryPacket packet = packetQueue.poll();

                            Gson gson = new Gson();
                            String json = gson.toJson(packet);
                            String id = packet.getMessageId();

                            // send packet
                            webSocket.send(json);


                        } else {
                            timeout = 3000;
                        }
                        Log.e(TAG, "main loop: map size: " + packetMap.size());
                        Log.e(TAG, "main loop: queue size: " + packetQueue.size());
                    }
                }
            };

            // loop again
            handler1.post(runnable);

        }

        // remove message from map when we get a response, so we don't
        // mistakenly send it again.
        private void removeFromMap(String uuid) {
            if (packetMap.containsKey(uuid)) {
                Log.e(TAG, "removeFromMap: contained key!");
                packetMap.remove(uuid);
            }
        }

        // if an entry in our map is older than 30 seconds, re-add it to the queue, because
        // the server probably didn't get it
        public void reEnqueueOldPacket(int currentTime) {
            if (!packetMap.isEmpty()) {
                for (Map.Entry<String, TelemetryPacket> entry : packetMap.entrySet()) {
                    int difference = currentTime - Integer.parseInt(String.valueOf(entry.getValue().getPayload().getTs()));
                    if (difference > 30) {
                        packetQueue.add(entry.getValue());
                    }

                }
            }

        }


        // this is triggered when we get a response from the web socket
        @Override
        public void onMessage(WebSocket webSocket, String text) {
            Log.d(TAG, "onMessage: response: " + text);
            Log.d(TAG, "onMessage: Message received! UUID: " + extractUUIDFromResponse(text));
            String uuid = extractUUIDFromResponse(text).replace("\"", ""); // remove quotations from response
            removeFromMap(uuid);
            reEnqueueOldPacket(Integer.parseInt(tel.createTimeStamp()));

        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            Log.e(TAG, "onClosing: " + reason);
            webSocket.close(NORMAL_CLOSE_STATUS, null);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            Log.e(TAG, "onFailure: failed. Response: " + response + ", throwable: " + t);
            socketLoopBoolean = false;
            telemetryLoopBoolean = false;
        }
    }

    // does what the method name says
    public String extractUUIDFromResponse(String response) {
        JsonObject jobj = new Gson().fromJson(response, JsonObject.class);
        return jobj.get("messageId").toString();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        context = getApplicationContext();
        Log.e(TAG, "onCreate: created!");
        refreshLoop();
    }

    final Handler handler = new Handler();

    public void packetGeneratorLoop() {

        final Runnable runnable = new Runnable() {
            public void run() {

                int i = 1;
                if (telemetryLoopBoolean) {
                    handler.postDelayed(this, 5000 * i++); // generate a packet every 5 seconds
                    generateTelemetryPacket();

                    timeout = TimeoutCalculator.setTimeout(packetQueue.size());
                    Log.e(TAG, "run: timeout: " + timeout);
                    Log.e(TAG, "run: generated tel packet");
                }
            }
        };

        // trigger first time
        handler.post(runnable);
    }


    int count = 0;
    Handler refreshHandler = new Handler();

    public void refreshLoop() {

        final Runnable runnable = new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            public void run() {

                if (count++ < 10000) {
                    Log.d(TAG, "restartMethod: RESTART CALLED" + "\nsocket: "
                            + socketLoopBoolean + "\ntel: " + telemetryLoopBoolean);

                    Log.d(TAG, "run: Timeout stats: " + TimeoutCalculator.getDataString());
                    Log.d(TAG, "run: timeout: " + TimeoutCalculator.setTimeout(packetQueue.size()));

                    refreshHandler.postDelayed(this, 30000); // 30 seconds
                    if (!socketLoopBoolean && !telemetryLoopBoolean) {

                        stop();
                        start();

                        Log.d(TAG, "run: REFRESH STARTED! ");
                    }
                }
            }
        };

        // trigger first time
        refreshHandler.post(runnable);
    }


    // generate a telemetry packet.
    public void generateTelemetryPacket() {

        final TelemetryMethods tel = new TelemetryMethods();

        long ts = Long.parseLong(tel.createTimeStamp());
        String id = tel.createUUID();

        float battery = getBatteryPercentage(TelemetryApplicationClass.getAppContext());

        Log.d(TAG, "generateTelemetryPacket: UUID: " + UUID);

        String token = "eyJhbGciOiJIUzI1NiJ9.eyJVU0lEIjoiZjcwOWZjMWQ1YjdiZWM0NDdiM2RmYWQ3NzY1MzEwOGFhYzczYTJmYSIsInRzIjoxNTA1NDE1MjY4fQ.W9e3ANSiE75HR6smc00uFsDAfxCcdB8niEydUlFY_ZY";

        TelemetryPacket packet = new TelemetryPacket();
        TelemetryPacket.PayloadBean data = new TelemetryPacket.PayloadBean();

        packet.setUUID(UUID);
        packet.setToken(token);
        packet.setMessageId(id);
        data.setAlt(alt);
        data.setLng(lng);
        data.setLat(lat);
        data.setHAcc(acc);
        data.setCourse(course);
        data.setBatt(battery);
        data.setTs(ts);
        packet.setMessageId(id);
        packet.setPayload(data);

        Long tsLong = System.currentTimeMillis() / 1000;
        packet.setTs(tsLong);

        // Log.e(TAG, "generateTelemetryPacket: ts: " + tsLong);
        if (!packetQueue.isEmpty()) {
            Log.d(TAG, "generateTelemetryPacket: queue peek ts: " + packetQueue.peek().getTs());
        }

        if (!packetQueue.isEmpty()) {
            if (packetQueue.peek().getTs() != tsLong) {
                packetQueue.add(packet);
            }
        } else {
            packetQueue.add(packet);
        }
        packetMap.put(id, packet);

    }


    // start connecting to web socket
    void start() {

        socketLoopBoolean = true;
        telemetryLoopBoolean = true;

        // ws://ec2-34-210-213-56.us-west-2.compute.amazonaws.com:8080
//
//        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
//        logging.setLevel(HttpLoggingInterceptor.Level.BODY); // request everything

        Request request = new Request.Builder().cacheControl(new CacheControl.Builder().build()).url("wss://telemetry-gateway.gocopia.com:6436").build();
        WebSocketListener listener = new WebSocketListener();
        OkHttpClient newClient = new OkHttpClient.Builder().retryOnConnectionFailure(true).readTimeout(3, TimeUnit.SECONDS).build();
        webSocket = newClient.newWebSocket(request, listener);

        // start reading location data
        doAThing(TelemetryApplicationClass.getAppContext());

        packetGeneratorLoop();

    }


    // start getting sensor data from the phone
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

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0.0001f, this);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_HIGH);
        String bestProvider = locationManager.getBestProvider(criteria, false);
        android.location.Location location = locationManager.getLastKnownLocation(bestProvider);

    }


    // stop talking to the web socket
    public void stop() {
        if (webSocket != null) {
            webSocket.close(NORMAL_CLOSE_STATUS, " tracking not needed");
        }
        socketLoopBoolean = false;
        telemetryLoopBoolean = false;
    }


    // simple battery percentage
    public static float getBatteryPercentage(Context context) {

        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, iFilter);

        int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
        int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;

        return level / (float) scale;
    }



}
