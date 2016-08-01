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
 * Created by monowar on 3/14/16.
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
                Log.d(TAG,"DataKitException...runnableDay");
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
                            Log.d(TAG,"DataKitException...schedulerManager.setDayStartTimestamp");
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
                            Log.d(TAG,"DataKitException...schedulerManager...setDayEndTimestamp");
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
