package com.example.natepowers.telemetrytoyapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.EventLogTags;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


/**
 * Created by:
 * ~~~~~~~~~_  __     __
 * ~~~~~~~~/ |/ ___ _/ /____
 * ~~~~~~~/    / _ `/ __/ -_)
 * ~~~~~~/_/|_/\_,_/\__/\__/
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * ~~~~~~~~~~~~~~~~~~~  at Copia PBC   ~~~~~~~
 */

public class MainActivity extends AppCompatActivity  {

    private static final String TAG = "MainActivity";

    private Button start;
    static TextView output;
    boolean startButtonClicked = false;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        start = (Button) findViewById(R.id.start);
        output = (TextView) findViewById(R.id.output);

        TelemetrySingleton fresh = TelemetrySingleton.getInstance();
        fresh.refreshLoop();


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


        Log.e(TAG, "onCreate: online: " + isOnline() );
    }

    public  static boolean returnOnline() {
        return online;
    }

    public static boolean online = false;

    public static void setOutput(String message) {
        output.setText(message);
    }

    public boolean isOnline() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        Log.e(TAG, "isOnline: online: " + isAvailable );
        online = isAvailable;
        return isAvailable;
    }



}
