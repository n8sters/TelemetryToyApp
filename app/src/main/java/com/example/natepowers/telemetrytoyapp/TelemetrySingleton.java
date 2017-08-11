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
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

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
    boolean stopLoop = false;
    boolean shouldGetTelemetryData = true;
    protected LocationManager locationManager;
    public static final int NORMAL_CLOSE_STATUS = 1000;
    WebSocket webSocket;

    // a queue for holding the packets to be sent off
    Queue<TelemetryPacket> packetQueue = new LinkedList<>();

    // a map for referencing the sent packets, so none get lost
    HashMap<String, TelemetryPacket> packetMap = new HashMap<>();

    Context context;

    @Override
    public void onLocationChanged(Location location) {

        if (location != null) {
            TelemetryMethods tel = new TelemetryMethods();

            lat = (location.getLatitude());
            lng = (location.getLongitude());
            alt = Double.parseDouble(tel.getAltutude(location));
            acc = tel.getAccuracy(location);
            course = location.getBearing();

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



    private final class WebSocketListener extends okhttp3.WebSocketListener {

        TelemetryMethods tel = new TelemetryMethods();


        @Override
        public void onOpen(final WebSocket webSocket, Response response) {
            // set up runnable handler
            Handler handler1 = new Handler(Looper.getMainLooper());

            for (int i = 0; i < 720; i++) {
                // set timeout thread
                handler1.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        String id = tel.createUUID();

                        TelemetryPacket packet = generateTelemetryPacket();

                        packetQueue.add(packet);
                        packetMap.put(id, packet);

                        Gson gson = new Gson();
                        String json = gson.toJson(packet);

                        // send packet
                        webSocket.send(json);

                        Log.e(TAG, "run: id: " + id );
                        Log.e(TAG, "loop: map size: " + packetMap.size() );
                        Log.e(TAG, "loop: queue size: " + packetQueue.size() );

                        // Log.e(TAG, "run: Packet Sent: " + json);
                        // Log.e("", "\n\n");
                    }

                    //  todo change timeout based on battery, internet, etc
                }, 3000 * i);
            }


        }

        // remove message from map when we get a response, so we don't
        // mistakenly send it again.
        private void removeFromMap( String uuid ) {
            if ( packetMap.containsKey(uuid)) {
                Log.e(TAG, "removeFromMap: contained key!");
                packetMap.remove(uuid);
            }
        }

        // if an entry in our map is older than 30 seconds, re-add it to the queue, because
        // the server probably didn't get it
        public void reEnqueueOldPacket(int currentTime ) {
            if ( !packetMap.isEmpty() ) {
                for (Map.Entry<String, TelemetryPacket> entry : packetMap.entrySet())  {
                    int difference = currentTime - Integer.parseInt(String.valueOf(entry.getValue().getPayload().getTs()));
                    if ( difference > 30 ) {
                        packetQueue.add(entry.getValue());
                    }

                }
            }

        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            Log.e(TAG, "onMessage: " + extractUUIDFromResponse(text) );
            String uuid = extractUUIDFromResponse(text).replace("\"", ""); // remove quotations from response

            removeFromMap(uuid);
            //reEnqueueOldPacket(Integer.parseInt(tel.createTimeStamp()));

        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            webSocket.close(NORMAL_CLOSE_STATUS, null);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            Log.e(TAG, "onFailure: failed. Response: " + t );
        }
    }



    // does what the method name says
    public String extractUUIDFromResponse(String response){
        JsonObject jobj = new Gson().fromJson(response, JsonObject.class);
        return jobj.get("messageId").toString();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();


    }

    // generate a telemetry packet.
    public TelemetryPacket generateTelemetryPacket() {

        final TelemetryMethods tel = new TelemetryMethods();

        long ts = Long.parseLong(tel.createTimeStamp());
        String id = tel.createUUID();

        float battery = getBatteryPercentage(TelemetryApplicationClass.getAppContext());

        String token = "eyJhbGciOiJIUzI1NiJ9.eyJVU0lEIjoiOTFkNTI4NzhjMTgxYWRmNDY4OGU2ODA0ZThkODU0NTA2NzUzMmQ0MyIsInRzIjoxNTAwNTg0ODY4fQ.D5A9WaoA-D3B0XWUAlsFHBs0yRJdd5_5gS_1lcxS-WU"; // todo set token based on user session

        TelemetryPacket packet = new TelemetryPacket();

        TelemetryPacket.PayloadBean data = new TelemetryPacket.PayloadBean();
        packet.setToken(token);
        packet.setMessageId(id);
        data.setAlt(alt);
        data.setLat(lat);
        data.setLng(lng);
        data.setHAcc(acc);
        data.setCourse(course);
        data.setBatt(battery);
        data.setTs(ts);
        packet.setMessageId(id);
        packet.setPayload(data);

        Long tsLong = System.currentTimeMillis() / 1000;
        packet.setTs(tsLong);

        return packet;
    }

    public void enqueuePackets(final TelemetryPacket packet) {
        // set up runnable handler
        Handler queueHandler = new Handler(Looper.getMainLooper());

        int handlerTimeoutMiltiplier = 0;
        for (int i = 0; i < 720; i++) {
            // set timeout thread
            queueHandler.postDelayed(new Runnable() {

                @Override
                public void run() {

                    packetQueue.add(packet);
                    packetMap.put(packet.getMessageId(), packet);

                }


                //  todo change timeout based on battery, internet, etc
            }, 3000 * i); // currently set to 1 second

        }


    }

    // getter for loop control boolean
    public boolean isShouldGetTelemetryData() {
        return shouldGetTelemetryData;
    }

    // setter for loop control boolean
    public void setShouldGetTelemetryData(boolean shouldGetTelemetryData) {
        this.shouldGetTelemetryData = shouldGetTelemetryData;
    }


    // start connecting to web socket
    void start() {

        Request request = new Request.Builder().url("ws://ec2-34-210-213-56.us-west-2.compute.amazonaws.com:8080").build();
        WebSocketListener listener = new WebSocketListener();
        OkHttpClient newClient = new OkHttpClient();
        webSocket = newClient.newWebSocket(request, listener);
        newClient.dispatcher().executorService().shutdown();
        doAThing(TelemetryApplicationClass.getAppContext());

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
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, false);
        android.location.Location location = locationManager.getLastKnownLocation(bestProvider);

    }


    // stop talking to the web socket
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
