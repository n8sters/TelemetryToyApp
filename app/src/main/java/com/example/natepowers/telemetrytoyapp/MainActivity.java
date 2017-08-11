package com.example.natepowers.telemetrytoyapp;

import android.Manifest;
import android.content.BroadcastReceiver;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;

public class MainActivity extends AppCompatActivity implements LocationListener, SensorEventListener {

    private static final String TAG = "MainActivity";

    private Button start;
    TextView output;
    OkHttpClient client;
    double lat, lng, acc, course, alt;
    boolean stopLoop = false;
    protected LocationManager locationManager;
    public static final int NORMAL_CLOSE_STATUS = 1000;


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

                        float battery = getBatteryPercentage(getApplicationContext());

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
            output("Receiving bytes: " + text + "Lat: " + lat + ", Lng: " + lng);
            Log.e(TAG, "onMessage: " + text);

        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            webSocket.close(NORMAL_CLOSE_STATUS, null);
            output("closing: " + code + " / " + reason);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            output("Error: " + t.getMessage());
        }
    }

    boolean startButtonClicked = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        start = (Button) findViewById(R.id.start);
        output = (TextView) findViewById(R.id.output);
        client = new OkHttpClient();


        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (startButtonClicked) {

                    Log.e(TAG, "onCreate: stopped!");
                    startButtonClicked = false;
                    start.setText("START");
                    TelemetrySingleton single = TelemetrySingleton.getInstance();
                    single.stop();

                } else {
                    TelemetrySingleton single = TelemetrySingleton.getInstance();
                    single.start();
                    start.setText("STOP");
                    startButtonClicked = true;
                    Log.e(TAG, "onClick: start clicked, boolean: " + startButtonClicked);
                }


            }
        });


    }

    public void doAThing() {

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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



    WebSocket webSocket;

    public void start() {
        Request request = new Request.Builder().url("ws://ec2-34-210-213-56.us-west-2.compute.amazonaws.com:8080").build();
        WebSocketListener listener = new WebSocketListener();
        webSocket = client.newWebSocket(request, listener);
//        client.dispatcher().executorService().shutdown();
    }

    public void stop() {
        stopLoop = true;
        webSocket.close(NORMAL_CLOSE_STATUS, " tracking not needed");
    }


    private void output(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                output.setText(output.getText().toString() + "\n\n" + text);
            }
        });
    }


    private void updateBatteryData(Intent intent) {
        boolean present = intent.getBooleanExtra(BatteryManager.EXTRA_PRESENT, false);

        if (present) {

            // Calculate Battery Pourcentage ...
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            if (level != -1 && scale != -1) {
                int batteryPct = (int) ((level / (float) scale) * 100f);
                //batteryPctTv.setText("Battery Pct : " + batteryPct + " %"));
            }

            int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
            int pluggedLbl = R.string.battery_plugged_none;

            switch (plugged) {
                case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                    pluggedLbl = R.string.battery_plugged_wireless;
                    break;

                case BatteryManager.BATTERY_PLUGGED_USB:
                    pluggedLbl = R.string.battery_plugged_usb;
                    break;

                case BatteryManager.BATTERY_PLUGGED_AC:
                    pluggedLbl = R.string.battery_plugged_ac;
                    break;

                default:
                    pluggedLbl = R.string.battery_plugged_none;
                    break;
            }

            // display plugged status ...
            //pluggedTv.setText("Plugged : " + getString(pluggedLbl));

            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            int statusLbl = R.string.battery_status_discharging;

            switch (status) {
                case BatteryManager.BATTERY_STATUS_CHARGING:
                    statusLbl = R.string.battery_status_charging;
                    break;

                case BatteryManager.BATTERY_STATUS_DISCHARGING:
                    statusLbl = R.string.battery_status_discharging;
                    break;

                case BatteryManager.BATTERY_STATUS_FULL:
                    statusLbl = R.string.battery_status_full;
                    break;

                case BatteryManager.BATTERY_STATUS_UNKNOWN:
                    statusLbl = -1;
                    break;

                case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                default:
                    statusLbl = R.string.battery_status_discharging;
                    break;
            }

            if (statusLbl != -1) {
                //chargingStatusTv.setText("Battery Charging Status : " + getString(statusLbl));
            }

            if (intent.getExtras() != null) {
                String technology = intent.getExtras().getString(BatteryManager.EXTRA_TECHNOLOGY);

                if (!"".equals(technology)) {
                    //technologyTv.setText("Technology : " + technology);
                }
            }

            int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);

            if (temperature > 0) {
                float temp = ((float) temperature) / 10f;
                //tempTv.setText("Temperature : " + temp + "°C);
            }

            int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);

            if (voltage > 0) {
                //voltageTv.setText("Voltage : " + voltage + " mV);
            }

            long capacity = getBatteryCapacity(this);

            if (capacity > 0) {
                //capacityTv.setText("Capacity : " + capacity + " mAh");
            }

        } else {
            Toast.makeText(this, "No Battery present", Toast.LENGTH_SHORT).show();
        }

    }

    private BroadcastReceiver batteryInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateBatteryData(intent);
        }
    };

    public long getBatteryCapacity(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BatteryManager mBatteryManager = (BatteryManager) ctx.getSystemService(Context.BATTERY_SERVICE);
            Long chargeCounter = mBatteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER);
            Long capacity = mBatteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);

            if (chargeCounter != null && capacity != null) {
                long value = (long) (((float) chargeCounter / (float) capacity) * 100f);
                return value;
            }
        }

        return 0;
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
