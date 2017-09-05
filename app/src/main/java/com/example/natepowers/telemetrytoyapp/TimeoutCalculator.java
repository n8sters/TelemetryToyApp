package com.example.natepowers.telemetrytoyapp;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.telephony.CellInfoGsm;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.TelephonyManager;
import android.util.Log;

// for determining the packet sending timeout length
public class TimeoutCalculator {

    private static final String TAG = "TimeoutCalculator";

    /**         Things to consider:
     *
     *      1. Battery life
     *      2. charging state / type ( car/wall/etc )
     *      3. internet connected
     *      4. GPS method, strength
     *      5. location changed delta
     *
     * */

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("HardwareIds")
    public static String getLocationMode(Context context) {
        return Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.LOCATION_MODE);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("HardwareIds")
    public static String getLocationBatterySavings(Context context) {
        return Settings.Secure.getString(context.getContentResolver(),
                String.valueOf(Settings.Secure.LOCATION_MODE_BATTERY_SAVING));
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("HardwareIds")
    public static String getLocationHighAccuracy(Context context) {
        return Settings.Secure.getString(context.getContentResolver(),
                String.valueOf(Settings.Secure.LOCATION_MODE_HIGH_ACCURACY));
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("HardwareIds")
    public static String getLocationModeOff(Context context) {
        return Settings.Secure.getString(context.getContentResolver(),
                String.valueOf(Settings.Secure.LOCATION_MODE_OFF));
    }

    // check to see if the phone has internet
    public static boolean isConnected() {
        ConnectivityManager cm =
                (ConnectivityManager)TelemetryApplicationClass.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

    }

    public static String typeOfInternetConnected() {

        ConnectivityManager cm =
                (ConnectivityManager)TelemetryApplicationClass.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
        boolean isMobile = activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;
        boolean noNetwork = activeNetwork.getExtraInfo() == ConnectivityManager.EXTRA_NO_CONNECTIVITY;

        if ( isWiFi) {
            return "wifi";
        }else if ( isMobile) {
            return "mobile";
        } else if ( noNetwork ) {
            return "noNetwork";
        }
        return "Is wifi: " + isWiFi + "\nIs Mobile: " + isMobile + "\nno network: " + noNetwork;
    }



    // check is phone is charging, or battery is at 100%
    public static boolean isCharging() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = TelemetryApplicationClass.getAppContext().registerReceiver(null, ifilter);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

        return (status == BatteryManager.BATTERY_STATUS_CHARGING) ||
                (status == BatteryManager.BATTERY_STATUS_FULL);

    }


    // get battery percentage as float ( 1.0 == 100%, .5 = 50% )
    public static float getBatteryPercentage(Context context) {

        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, iFilter);

        int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
        int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;

        return level / (float) scale;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String getDataString() {

        Context c = TelemetryApplicationClass.getAppContext();

        return ("Timeout calculator stats: " +
        "\nBattery percent: " + getBatteryPercentage(c) +
        "\nis charging: " + isCharging() + "\ntype of internet connected: " + typeOfInternetConnected() +
        "\nIs connected: " + isConnected() + "\nGetLocationMode: " + getLocationMode(c) +
        "\nGet location mode off: " + getLocationModeOff(c) + "\nGet location high accuracy: " +
        getLocationHighAccuracy(c) + "\nGet location battery saving: " + getLocationBatterySavings(c));


    }

    private static int battery = (int) (getBatteryPercentage(TelemetryApplicationClass.getAppContext()) * 100);


    // set the timeout of the packet sending loop
    public static int setTimeout() {

        Log.e(TAG, "setTimeout: battery: " + battery );

        if ( battery > 90 && isCharging() && typeOfInternetConnected().equals("wifi") ) {
            return 100;
        } else if ( battery > 90 && isCharging() ){
            return 200;
        } else if ( battery > 60 && isCharging() || battery > 80 ) {
            return 300;
        } else if ( battery < 20 ) {
            return 5000;
        } else if (typeOfInternetConnected().equals("noNetwork") ) {
            return 10000;
        }

        return 1000;
    }



}
