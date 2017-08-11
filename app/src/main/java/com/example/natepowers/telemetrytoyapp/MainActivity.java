package com.example.natepowers.telemetrytoyapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
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
    TextView output;

    boolean startButtonClicked = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        start = (Button) findViewById(R.id.start);
        output = (TextView) findViewById(R.id.output);

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

}
