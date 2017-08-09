package com.example.natepowers.telemetrytoyapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.support.v7.app.AppCompatActivity;

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

public class TelemtryMethods extends AppCompatActivity {

    private static final String TAG = "TelemtryMethods";

    TelemtryMethods() {
    }

    // generate a random UUID
    String createUUID() {

        return UUID.randomUUID().toString();

    }

    // generate a time stamp
    String createTimeStamp() {
        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        return ts;
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

}
