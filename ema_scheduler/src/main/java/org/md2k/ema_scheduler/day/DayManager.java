package org.md2k.ema_scheduler.day;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.datatype.DataTypeLong;
import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.messagehandler.OnReceiveListener;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.ema_scheduler.ServiceEMAScheduler;
import org.md2k.ema_scheduler.logger.LoggerManager;
import org.md2k.ema_scheduler.scheduler.SchedulerManager;
import org.md2k.utilities.Report.Log;

import java.util.ArrayList;

/**
 * Copyright (c) 2016, The University of Memphis, MD2K Center
 * - Syed Monowar Hossain <monowar.hossain@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
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
public class DayManager {
    private static final String TAG = DayManager.class.getSimpleName();
    Context context;
    long dayStartTime, dayEndTime;
    DataSourceClient dataSourceClientDayStart;
    DataSourceClient dataSourceClientDayEnd;
    SchedulerManager schedulerManager;
    Handler handler;
    Runnable runnableDay = new Runnable() {
        @Override
        public void run() {
            ArrayList<DataSourceClient> dataSourceClients = null;
            try {
                dataSourceClients = DataKitAPI.getInstance(context).find(new DataSourceBuilder().setType(DataSourceType.DAY_START));
                Log.d(TAG, "runnableListenDayStart()...dataSourceClients.size()=" + dataSourceClients.size());
                if (dataSourceClients.size() == 0)
                    handler.postDelayed(runnableDay, 1000);
                else {
                    readDayStartFromDataKit();
                    readDayEndFromDataKit();
                    subscribeDayStart();
                    subscribeDayEnd();
                    LoggerManager.getInstance(context).reset(dayStartTime);

                    schedulerManager.start(dayStartTime, dayEndTime);
                }
            } catch (DataKitException e) {
                Log.w(TAG,"DataKitException...runnableDay");
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ServiceEMAScheduler.BROADCAST_MSG));
            }
        }
    };

    public DayManager(Context context) throws DataKitException {
        Log.d(TAG, "DayManager()...");
        this.context = context;
        schedulerManager = new SchedulerManager(context);
        handler = new Handler();
    }

    public void start() {
        Log.d(TAG, "start()...");
        handler.post(runnableDay);
    }

    public void stop() {
        Log.d(TAG, "stop()...");
        handler.removeCallbacks(runnableDay);
        try {
            DataKitAPI.getInstance(context).unsubscribe(dataSourceClientDayStart);
            DataKitAPI.getInstance(context).unsubscribe(dataSourceClientDayEnd);
        } catch (DataKitException ignored) {
        }
        schedulerManager.stop();
    }

    public void subscribeDayStart() throws DataKitException {
        Log.d(TAG, "subscribeDayStart()...");
        DataKitAPI.getInstance(context).subscribe(dataSourceClientDayStart, new OnReceiveListener() {
            @Override
            public void onReceived(DataType dataType) {
                DataTypeLong dataTypeLong = (DataTypeLong) dataType;
                dayStartTime = dataTypeLong.getSample();
                Log.d(TAG, "subscribeDayStart()...received..dayStartTime=" + dayStartTime);
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            LoggerManager.getInstance(context).reset(dayStartTime);
                            schedulerManager.setDayStartTimestamp(dayStartTime);
                        } catch (DataKitException e) {
                            Log.w(TAG,"DataKitException...schedulerManager.setDayStartTimestamp");
                            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ServiceEMAScheduler.BROADCAST_MSG));

                        }
                    }
                });
                t.start();
            }
        });
    }

    public void subscribeDayEnd() throws DataKitException {
        Log.d(TAG, "subscribeDayEnd()...");
        DataKitAPI.getInstance(context).subscribe(dataSourceClientDayEnd, new OnReceiveListener() {
            @Override
            public void onReceived(DataType dataType) {
                DataTypeLong dataTypeLong = (DataTypeLong) dataType;
                dayEndTime = dataTypeLong.getSample();
                Log.d(TAG, "subscribeDayEnd()...received..dayEndTime=" + dayEndTime);
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            schedulerManager.setDayEndTimestamp(dayEndTime);
                        } catch (DataKitException e) {
                            Log.w(TAG,"DataKitException...schedulerManager...setDayEndTimestamp");
                            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ServiceEMAScheduler.BROADCAST_MSG));

                        }
                    }
                });
                t.start();
            }
        });
    }

    private void readDayStartFromDataKit() throws DataKitException {
        Log.d(TAG, "readDayStartFromDataKit()...");
        ArrayList<DataSourceClient> dataSourceClients;
        dayStartTime = -1;
        dataSourceClients = DataKitAPI.getInstance(context).find(new DataSourceBuilder().setType(DataSourceType.DAY_START));
        Log.d(TAG, "readDayStartFromDataKit()...find..dataSourceClient.size()=" + dataSourceClients.size());
        if (dataSourceClients.size() > 0) {
            dataSourceClientDayStart = dataSourceClients.get(0);
            ArrayList<DataType> dataTypes = DataKitAPI.getInstance(context).query(dataSourceClientDayStart, 1);
            if (dataTypes.size() != 0) {
                DataTypeLong dataTypeLong = (DataTypeLong) dataTypes.get(0);
                dayStartTime = dataTypeLong.getSample();
                Log.d(TAG, "readDayStartFromDataKit()...dayStartTime=" + dayStartTime);
            }
        }
    }

    private void readDayEndFromDataKit() throws DataKitException {
        Log.d(TAG, "readDayEndFromDataKit()...");
        dayEndTime = -1;
        ArrayList<DataSourceClient> dataSourceClients;
        dataSourceClients = DataKitAPI.getInstance(context).find(new DataSourceBuilder().setType(DataSourceType.DAY_END));
        Log.d(TAG, "readDayEndFromDataKit()...find..dataSourceClient.size()=" + dataSourceClients.size());
        if (dataSourceClients.size() > 0) {
            dataSourceClientDayEnd = dataSourceClients.get(0);
            ArrayList<DataType> dataTypes = DataKitAPI.getInstance(context).query(dataSourceClientDayEnd, 1);
            if (dataTypes.size() != 0) {
                DataTypeLong dataTypeLong = (DataTypeLong) dataTypes.get(0);
                dayEndTime = dataTypeLong.getSample();
                Log.d(TAG, "readDayEndFromDataKit()...dayEndTime=" + dayEndTime);
            }
        }
    }

    public long getDayStartTime() {
        return dayStartTime;
    }

    public long getDayEndTime() {
        return dayEndTime;
    }
}
