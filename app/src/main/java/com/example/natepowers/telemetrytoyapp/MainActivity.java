package com.example.natepowers.telemetrytoyapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Button start;
    TextView output;
    OkHttpClient client;

    private final class WebSocketListener extends okhttp3.WebSocketListener {
        public static final int NORMAL_CLOSE_STATUS = 1000;

        TelemtryMethods tel = new TelemtryMethods();

        @Override
        public void onOpen(final WebSocket webSocket, Response response) {
            // set up runnable handler
            Handler handler1 = new Handler(Looper.getMainLooper());

            for (int i = 0; i < 10; i++) {
                // set timeout thread
                handler1.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        String ts = tel.createTimeStamp();
                        String id = tel.createUUID();
                        // send packet
                        webSocket.send("{\n" +
                                "\t\"token\":\"eyJhbGciOiJIUzI1NiJ9.eyJVU0lEIjoiOTFkNTI4NzhjMTgxYWRmNDY4OGU2ODA0ZThkODU0NTA2NzUzMmQ0MyIsInRzIjoxNTAwNTg0ODY4fQ.D5A9WaoA-D3B0XWUAlsFHBs0yRJdd5_5gS_1lcxS-WU\",\n" +
                                "\t\"messageId\":\" " + id + " \",\n" +
                                "\t\"payload\":[\n" +
                                "\t\t\t{\n" +
                                "\t\t\t\t\"lat\": 1.1,\n" +
                                "\t\t\t\t\"lng\": 1.1,\n" +
                                "\t\t\t\t\"hAcc\": 1.1,\n" +
                                "\t\t\t\t\"alt\": 1.1,\n" +
                                "\t\t\t\t\"vAcc\": 1.1,\n" +
                                "\t\t\t\t\"speed\": 1.1,\n" +
                                "\t\t\t\t\"course\": 1.1,\n" +
                                "\t\t\t\t\"batt\": 1.00001,\n" +
                                "\t\t\t\t\"ts\": 1502236470\n" +
                                "\t\t\t}\n" +
                                "\t\t],\n" +
                                "\t\"ts\":\"" + ts + "\"\n" +
                                "}");

                        Log.e(TAG, "run: UUID: " + id );
                        Log.e(TAG, "run: timestamp: " + ts );
                        Log.e(TAG, "run: battery: " + getBatteryPercentage(getApplicationContext()) );
                        Log.e("", "\n\n" );
                    }

                    // todo change timeout based on battery, internet, etc
                }, 1000 * i); // currently set to 1 second

            }


        }


        @Override
        public void onMessage(WebSocket webSocket, String text) {
            output("Receiving bytes: " + text);
            Log.e(TAG, "onMessage: " + text );

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
                start();
            }
        });

    }

    public void start() {
        Request request = new Request.Builder().url("ws://ec2-34-210-213-56.us-west-2.compute.amazonaws.com:8080").build();
        WebSocketListener listener = new WebSocketListener();
        WebSocket webSocket = client.newWebSocket(request, listener);
        client.dispatcher().executorService().shutdown();
    }


    private void output(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                output.setText(output.getText().toString() + "\n\n" + text);
            }
        });
    }


    public void loadBatteryInfo( ) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);

        registerReceiver(batteryInfoReceiver, filter);
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

    // simple battery percentage
    public static float getBatteryPercentage(Context context) {

        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, iFilter);

        int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
        int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;

        float batteryPct = level / (float) scale;

        return  (batteryPct * 100);
    }

}
