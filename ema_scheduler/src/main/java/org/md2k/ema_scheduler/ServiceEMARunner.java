package org.md2k.ema_scheduler;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.messagehandler.OnConnectionListener;
import org.md2k.datakitapi.messagehandler.OnExceptionListener;
import org.md2k.datakitapi.status.Status;
import org.md2k.ema_scheduler.configuration.ConfigurationManager;
import org.md2k.utilities.Report.Log;

/**
 * Copyright (c) 2015, The University of Memphis, MD2K Center
 * - Syed Monowar Hossain <monowar.hossain@gmail.com>
 * All rights reserved.
 * <p/>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p/>
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * <p/>
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p/>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

public class ServiceEMARunner extends Service {
    private static final String TAG = ServiceEMARunner.class.getSimpleName();
    DataKitAPI dataKitAPI;
    ConfigurationManager configurationManager;
    private MyBroadcastReceiver myReceiver;
    IntentFilter intentFilter;
    Handler handler;

    public void onCreate() {
        super.onCreate();
        myReceiver = new MyBroadcastReceiver();
        intentFilter = new IntentFilter("org.md2k.ema_scheduler.response");
        if (intentFilter != null) {
            registerReceiver(myReceiver, intentFilter);
        }
        configurationManager = ConfigurationManager.getInstance(getApplicationContext());

        if (configurationManager.getConfiguration() == null) {
            Toast.makeText(getApplicationContext(), "!!!Error: EMA Configuration file not available...", Toast.LENGTH_LONG).show();
            stopSelf();
        } else {
            connectDataKit();
        }
        handler = new Handler();
        handler.postDelayed(runnableTimeOut, 10000);
    }

    Runnable runnableTimeOut = new Runnable() {
        @Override
        public void run() {
            sendData();
        }
    };

    void sendData() {
        Intent intent = new Intent();
        intent.setAction("org.md2k.ema.operation");
        intent.putExtra("type", "missed");
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        sendBroadcast(intent);
    }


    private void connectDataKit() {
        Log.d(TAG, "connectDataKit()...");
        dataKitAPI = DataKitAPI.getInstance(getApplicationContext());
        dataKitAPI.connect(new OnConnectionListener() {
            @Override
            public void onConnected() {
                Toast.makeText(getApplicationContext(), "EMA Scheduler started Successfully", Toast.LENGTH_LONG).show();
                startScheduler();
            }
        }, new OnExceptionListener() {
            @Override
            public void onException(Status status) {
                android.util.Log.d(TAG, "onException...");
                Toast.makeText(ServiceEMARunner.this, "Notification Managr.. Stopped. Error: " + status.getStatusMessage(), Toast.LENGTH_LONG).show();
                stopSelf();
            }
        });
    }

    void startScheduler() {

    }

    void stopScheduler() {

    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()...");
        if (dataKitAPI != null && dataKitAPI.isConnected()) dataKitAPI.disconnect();
        if (dataKitAPI != null)
            dataKitAPI.close();
        stopScheduler();
        configurationManager.clear();
        if (myReceiver != null)
            unregisterReceiver(myReceiver);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
