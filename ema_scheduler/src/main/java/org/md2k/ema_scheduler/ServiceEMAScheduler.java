package org.md2k.ema_scheduler;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.messagehandler.OnConnectionListener;
import org.md2k.datakitapi.messagehandler.ResultCallback;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.ema_scheduler.condition.ConditionManager;
import org.md2k.ema_scheduler.configuration.Configuration;
import org.md2k.ema_scheduler.day.DayManager;
import org.md2k.ema_scheduler.logger.LoggerDataQuality;
import org.md2k.ema_scheduler.logger.LoggerManager;
import org.md2k.utilities.Report.Log;
import org.md2k.utilities.Report.LogStorage;
import org.md2k.utilities.permission.PermissionInfo;

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
    private DataKitAPI dataKitAPI;
    private Configuration configuration;
    private DayManager dayManager;
    private boolean isStopping;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "stopSelf()...received broadcastReceiver msg");
            Log.w(TAG, "time=" + DateTime.convertTimeStampToDateTime(DateTime.getDateTime()) + ",timestamp=" + DateTime.getDateTime() + ",broadcast_receiver_stop_service");
            clear();
            stopSelf();
        }
    };

    public void onCreate() {
        super.onCreate();
        isStopping = false;
        Log.d(TAG, "onCreate()");
        PermissionInfo permissionInfo = new PermissionInfo();
        permissionInfo.getPermissions(this, new ResultCallback<Boolean>() {
            @Override
            public void onResult(Boolean result) {
                if (!result) {
                    Toast.makeText(getApplicationContext(), "!PERMISSION DENIED !!! Could not continue...", Toast.LENGTH_SHORT).show();
                    stopSelf();
                } else {
                    load();
                }
            }
        });
    }

    private void load() {

        LogStorage.startLogFileStorageProcess(getApplicationContext().getPackageName());
        Log.w(TAG, "time=" + DateTime.convertTimeStampToDateTime(DateTime.getDateTime()) + ",timestamp=" + DateTime.getDateTime() + ",service_start");
        configuration = Configuration.getInstance();
        LoggerManager.clear();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(BROADCAST_MSG));

        if (configuration.getEma_types() == null) {
            Toast.makeText(ServiceEMAScheduler.this, "!!!Error: EMA Configuration file not available...", Toast.LENGTH_LONG).show();
            Log.d(TAG, "stopSelf()... EMA Configuration file not available...");
            clear();
            stopSelf();
        } else {
            try {
                connectDataKit();
            } catch (DataKitException e) {
                Log.d(TAG, "stopSelf()... DataKitException...in connection");
                clear();
                stopSelf();
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.w(TAG, "onStartCommand()...");
        startForeground(98763, getCompatNotification());
        return START_STICKY;
    }

    private synchronized void connectDataKit() throws DataKitException {
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
                LoggerDataQuality.getInstance(getApplicationContext()).start();
                try {
                    dayManager = new DayManager(getApplicationContext());
                    dayManager.start();
                } catch (DataKitException e) {
                    Log.d(TAG, "stopSelf()... DataKitException ... dayManager ...error...");
                    clear();
                    stopSelf();
                }
            }
        });
    }

    private synchronized void clear() {
        if (isStopping) return;
        stopForeground(true);
        LoggerDataQuality.getInstance(getApplicationContext()).stop();
        isStopping = true;
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
    }

    @Override
    public void onDestroy() {
        clear();
        super.onDestroy();
    }


    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private Notification getCompatNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher).setContentTitle(getResources().getString(R.string.app_name));
        return builder.build();
    }

}
