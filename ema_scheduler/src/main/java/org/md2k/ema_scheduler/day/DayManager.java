package org.md2k.ema_scheduler.day;

import android.content.Context;
import android.os.Handler;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.datatype.DataTypeLong;
import org.md2k.datakitapi.messagehandler.OnReceiveListener;
import org.md2k.datakitapi.source.METADATA;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.source.platform.Platform;
import org.md2k.datakitapi.source.platform.PlatformBuilder;
import org.md2k.datakitapi.source.platform.PlatformType;
import org.md2k.ema_scheduler.scheduler.SchedulerManager;
import org.md2k.utilities.Report.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

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
    Runnable runnableListenDayStart =new Runnable() {
        @Override
        public void run() {
            ArrayList<DataSourceClient> dataSourceClients = dataKitAPI.find(new DataSourceBuilder().setType(DataSourceType.DAY_START));
            Log.d(TAG, "runnableListenDayStart()...dataSourceClients.size()="+dataSourceClients.size());
            if(dataSourceClients.size()==0)
                handler.postDelayed(runnableListenDayStart,1000);
            else{
                readDayStartFromDataKit();
                subscribeDayStart();
            }
        }
    };
    Runnable runnableListenDayEnd =new Runnable() {
        @Override
        public void run() {
            ArrayList<DataSourceClient> dataSourceClients = dataKitAPI.find(new DataSourceBuilder().setType(DataSourceType.DAY_END));
            Log.d(TAG, "runnableListenDayEnd()...dataSourceCLients.size()="+dataSourceClients.size());
            if(dataSourceClients.size()==0)
                handler.postDelayed(runnableListenDayStart,1000);
            else{
                readDayEndFromDataKit();
                subscribeDayEnd();
            }
        }
    };
    public DayManager(Context context) {
        Log.d(TAG, "DayManager()...");
        this.context = context;
        schedulerManager=new SchedulerManager(context, this);
        handler=new Handler();
    }

    public void start(){
        Log.d(TAG, "start()...");
        dataKitAPI = DataKitAPI.getInstance(context);
        handler.post(runnableListenDayStart);
        handler.post(runnableListenDayEnd);
        schedulerManager.start();
    }

    public void stop(){
        Log.d(TAG, "stop()...");
        handler.removeCallbacks(runnableListenDayStart);
        handler.removeCallbacks(runnableListenDayEnd);
        schedulerManager.stop();
    }

    boolean isToday(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        Calendar calendarNow = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        if (calendar.get(Calendar.YEAR) != calendarNow.get(Calendar.YEAR)) return false;
        if (calendar.get(Calendar.MONTH) != calendarNow.get(Calendar.MONTH)) return false;
        if (calendar.get(Calendar.DAY_OF_MONTH) != calendarNow.get(Calendar.DAY_OF_MONTH))
            return false;
        return true;
    }

    DataSourceBuilder createDataSourceBuilderDayStart() {
        Platform platform = new PlatformBuilder().setType(PlatformType.PHONE).build();
        DataSourceBuilder dataSourceBuilder = new DataSourceBuilder().setType(DataSourceType.DAY_START).setPlatform(platform);
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.NAME, "Day Start");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.DESCRIPTION, "Represents when day started");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.DATA_TYPE, DataTypeLong.class.getName());
        ArrayList<HashMap<String, String>> dataDescriptors = new ArrayList<>();
        HashMap<String, String> dataDescriptor = new HashMap<>();
        dataDescriptor.put(METADATA.NAME, "Day Start");
        dataDescriptor.put(METADATA.MIN_VALUE, String.valueOf(0));
        dataDescriptor.put(METADATA.MAX_VALUE, String.valueOf(Long.MAX_VALUE));
        dataDescriptor.put(METADATA.UNIT, "millisecond");
        dataDescriptor.put(METADATA.DESCRIPTION, "Contains day start time in millisecond");
        dataDescriptor.put(METADATA.DATA_TYPE, long.class.getName());
        dataDescriptors.add(dataDescriptor);
        dataSourceBuilder = dataSourceBuilder.setDataDescriptors(dataDescriptors);
        return dataSourceBuilder;
    }


    public void subscribeDayStart() {
        Log.d(TAG, "subscribeDayStart()...");
        dataKitAPI.subscribe(dataSourceClientDayStart, new OnReceiveListener() {
            @Override
            public void onReceived(DataType dataType) {
                DataTypeLong dataTypeLong = (DataTypeLong) dataType;
                dayStartTime = dataTypeLong.getSample();
                Log.d(TAG, "subscribeDayStart()...received..dayStartTime=" + dayStartTime);
                resetScheduler();
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
            }
        });
    }
    void resetScheduler(){
//        long curTime= DateTime.getDateTime();
///        Log.d(TAG,"resetScheduler()...dayStartTime="+dayStartTime+" curTime="+curTime+" dayEndTime="+dayEndTime);
//        if(dayStartTime!=-1 && dayStartTime<curTime && isToday(dayStartTime) && (dayStartTime>dayEndTime || curTime<dayEndTime))
//            schedulerManager.start();
        schedulerManager.reset();
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
                Log.d(TAG,"readDayStartFromDataKit()...dayStartTime="+dayEndTime);
                resetScheduler();
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
