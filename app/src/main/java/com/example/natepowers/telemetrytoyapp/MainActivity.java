package com.example.natepowers.telemetrytoyapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
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
    Context context = this;

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

                showLocationDialog(context);

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

    public void showLocationDialog(final Context context) {
        LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled) {
            // notify user
            android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(context);
            dialog.setMessage(context.getResources().getString(R.string.gps_network_not_enabled));
            dialog.setPositiveButton(context.getResources().getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    context.startActivity(myIntent);
                    //get gps
                }
            });
            dialog.setNegativeButton(context.getString(R.string.Cancel), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    finish();

                }
            });
            dialog.show();
        }
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
