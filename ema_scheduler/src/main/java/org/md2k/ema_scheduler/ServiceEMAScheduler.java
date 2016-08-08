package org.md2k.ema_scheduler;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.messagehandler.OnConnectionListener;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.ema_scheduler.condition.ConditionManager;
import org.md2k.ema_scheduler.configuration.Configuration;
import org.md2k.ema_scheduler.day.DayManager;
import org.md2k.ema_scheduler.logger.LoggerManager;
import org.md2k.utilities.Report.Log;
import org.md2k.utilities.Report.LogStorage;

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

public class ServiceEMAScheduler extends Service {
    public static final String BROADCAST_MSG = ServiceEMAScheduler.class.getSimpleName();
    private static final String TAG = ServiceEMAScheduler.class.getSimpleName();
    DataKitAPI dataKitAPI;
    Configuration configuration;
    DayManager dayManager;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "stopSelf()...received broadcastReceiver msg");
            Log.w(TAG, "time=" + DateTime.convertTimeStampToDateTime(DateTime.getDateTime()) + ",timestamp=" + DateTime.getDateTime() + ",broadcast_receiver_stop_service");
            stopSelf();
        }
    };

    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");
        LogStorage.startLogFileStorageProcess(getApplicationContext().getPackageName());
        Log.w(TAG, "time=" + DateTime.convertTimeStampToDateTime(DateTime.getDateTime()) + ",timestamp=" + DateTime.getDateTime() + ",service_start");
        configuration = Configuration.getInstance();
        LoggerManager.clear();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(BROADCAST_MSG));

        if (configuration.getEma_types() == null) {
            Toast.makeText(ServiceEMAScheduler.this, "!!!Error: EMA Configuration file not available...", Toast.LENGTH_LONG).show();
            Log.d(TAG, "stopSelf()... EMA Configuration file not available...");
            stopSelf();
        } else {
            try {
                connectDataKit();
            } catch (DataKitException e) {
                Log.d(TAG, "stopSelf()... DataKitException...in connection");
                stopSelf();
            }
        }
    }

    private void connectDataKit() throws DataKitException {
        Log.d(TAG, "connectDataKit()...");
        dataKitAPI = DataKitAPI.getInstance(ServiceEMAScheduler.this);
        dataKitAPI.connect(new OnConnectionListener() {
            @Override
            public void onConnected() {
//                Toast.makeText(ServiceEMAScheduler.this, "In EMAScheduler .. DataKit connected...", Toast.LENGTH_LONG).show();
                Log.d(TAG, "datakit connected...");
                Configuration.clear();
                LoggerManager.clear();
                configuration = Configuration.getInstance();
                LoggerManager.getInstance(getApplicationContext());
                try {
                    dayManager = new DayManager(getApplicationContext());
                    dayManager.start();
                } catch (DataKitException e) {
                    Log.d(TAG, "stopSelf()... DataKitException ... dayManager ...error...");
                    stopSelf();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        Log.w(TAG, "time=" + DateTime.convertTimeStampToDateTime(DateTime.getDateTime()) + ",timestamp=" + DateTime.getDateTime() + ",service_stop");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        Log.d(TAG, "onDestroy()...");
        if (dayManager != null)
            dayManager.stop();
        Configuration.clear();
        LoggerManager.clear();
        ConditionManager.clear();
        Log.d(TAG, "...stopScheduler()");
        if (dataKitAPI != null && dataKitAPI.isConnected()) dataKitAPI.disconnect();
        Log.d(TAG, "...DataKit disconnect()");
        super.onDestroy();
    }


    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
