package org.md2k.ema_scheduler.day;

import android.content.Context;
import android.os.Handler;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.datatype.DataTypeLong;
import org.md2k.datakitapi.messagehandler.OnReceiveListener;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.datakitapi.source.datasource.DataSourceType;
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
    DataKitAPI dataKitAPI;
    DataSourceClient dataSourceClientDayStart;
    DataSourceClient dataSourceClientDayEnd;
    SchedulerManager schedulerManager;
    Handler handler;
    public DayManager(Context context) {
        Log.d(TAG, "DayManager()...");
        this.context = context;
        schedulerManager=new SchedulerManager(context);
        handler=new Handler();
    }

    public void start(){
        Log.d(TAG, "start()...");
        dataKitAPI = DataKitAPI.getInstance(context);
        handler.post(runnableDay);
    }

    public void stop(){
        Log.d(TAG, "stop()...");
        handler.removeCallbacks(runnableDay);
        schedulerManager.stop();
    }

    Runnable runnableDay =new Runnable() {
        @Override
        public void run() {
            ArrayList<DataSourceClient> dataSourceClients = dataKitAPI.find(new DataSourceBuilder().setType(DataSourceType.DAY_START));
            Log.d(TAG, "runnableListenDayStart()...dataSourceClients.size()="+dataSourceClients.size());
            if(dataSourceClients.size()==0)
                handler.postDelayed(runnableDay,1000);
            else{
                readDayStartFromDataKit();
                readDayEndFromDataKit();
                subscribeDayStart();
                subscribeDayEnd();
                schedulerManager.start(dayStartTime, dayEndTime);
            }
        }
    };

    public void subscribeDayStart() {
        Log.d(TAG, "subscribeDayStart()...");
        dataKitAPI.subscribe(dataSourceClientDayStart, new OnReceiveListener() {
            @Override
            public void onReceived(DataType dataType) {
                DataTypeLong dataTypeLong = (DataTypeLong) dataType;
                dayStartTime = dataTypeLong.getSample();
                Log.d(TAG, "subscribeDayStart()...received..dayStartTime=" + dayStartTime);
                Thread t=new Thread(new Runnable() {
                    @Override
                    public void run() {
                        schedulerManager.setDayStartTimestamp(dayStartTime);
                    }
                });
                t.start();
            }
        });
    }

    public void subscribeDayEnd() {
        Log.d(TAG, "subscribeDayEnd()...");
        dataKitAPI.subscribe(dataSourceClientDayEnd, new OnReceiveListener() {
            @Override
            public void onReceived(DataType dataType) {
                DataTypeLong dataTypeLong = (DataTypeLong) dataType;
                dayEndTime = dataTypeLong.getSample();
                Log.d(TAG, "subscribeDayEnd()...received..dayEndTime=" + dayEndTime);
                Thread t=new Thread(new Runnable() {
                    @Override
                    public void run() {
                        schedulerManager.setDayEndTimestamp(dayEndTime);
                    }
                });
                t.start();
            }
        });
    }

    private void readDayStartFromDataKit() {
        Log.d(TAG,"readDayStartFromDataKit()...");
        ArrayList<DataSourceClient> dataSourceClients;
        dayStartTime = -1;
        dataSourceClients = dataKitAPI.find(new DataSourceBuilder().setType(DataSourceType.DAY_START));
        Log.d(TAG,"readDayStartFromDataKit()...find..dataSourceClient.size()="+dataSourceClients.size());
        if (dataSourceClients.size() > 0) {
            dataSourceClientDayStart = dataSourceClients.get(0);
            ArrayList<DataType> dataTypes = dataKitAPI.query(dataSourceClientDayStart, 1);
            if (dataTypes.size() != 0) {
                DataTypeLong dataTypeLong = (DataTypeLong) dataTypes.get(0);
                dayStartTime = dataTypeLong.getSample();
                Log.d(TAG, "readDayStartFromDataKit()...dayStartTime=" + dayEndTime);
            }
        }
    }

    private void readDayEndFromDataKit() {
        Log.d(TAG,"readDayEndFromDataKit()...");
        dayEndTime = -1;
        ArrayList<DataSourceClient> dataSourceClients;
        dataSourceClients = dataKitAPI.find(new DataSourceBuilder().setType(DataSourceType.DAY_END));
        Log.d(TAG,"readDayEndFromDataKit()...find..dataSourceClient.size()="+dataSourceClients.size());
        if (dataSourceClients.size() > 0) {
            dataSourceClientDayEnd = dataSourceClients.get(0);
            ArrayList<DataType> dataTypes = dataKitAPI.query(dataSourceClientDayEnd, 1);
            if (dataTypes.size() != 0) {
                DataTypeLong dataTypeLong = (DataTypeLong) dataTypes.get(0);
                dayEndTime = dataTypeLong.getSample();
                Log.d(TAG,"readDayEndFromDataKit()...dayEndTime="+dayEndTime);
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
