package com.example.natepowers.telemetrytoyapp;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

/**
 * Created by:
 * ~~~~~~_  __     __        ____      ______
 * ~~~~~/ |/ ___ _/ /____   / __/___  /_  _____  ___ __ __
 * ~~~~/    / _ `/ __/ -_)  > _/_ _/   / / / _ \/ _ / // /
 * ~~~/_/|_/\_,_/\__/\__/  |_____/    /_/  \___/_//_\_, /
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~/___/
 * ~~~~~~~~~~~~~~~~~~~  at Copia PBC   ~~~~~~~~~~~~~~~~~~~~~~
 */

public class TelemetryService extends IntentService {



    public TelemetryService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }
}
