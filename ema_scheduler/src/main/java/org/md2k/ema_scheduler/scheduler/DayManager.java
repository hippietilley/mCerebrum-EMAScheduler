package org.md2k.ema_scheduler.scheduler;

import android.content.Context;

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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by monowar on 3/14/16.
 */
public class DayManager {
    private static DayManager instance;
    Context context;
    long dayStartTime, dayEndTime;
    DataKitAPI dataKitAPI;
    DataSourceBuilder dataSourceBuilderDayStart;
    DataSourceClient dataSourceClientDayStart;
    DataSourceBuilder dataSourceBuilderDayEnd;
    DataSourceClient dataSourceClientDayEnd;
    Callback callback;

    private DayManager(Context context){
        this.context=context;
        dataKitAPI=DataKitAPI.getInstance(context);
        dataSourceBuilderDayStart = createDataSourceBuilderDayStart();
        dataSourceBuilderDayEnd = createDataSourceBuilderDayEnd();
        readDayStartFromDataKit();
        readDayEndFromDataKit();
    }

    public static DayManager getInstance(Context context){
        if(instance==null)
            instance=new DayManager(context);
        return instance;
    }

    public static void clear(){
        instance=null;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
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

    public boolean isValidDay(){
        if(!isToday(dayStartTime)) return false;
        if(dayStartTime<dayEndTime) return false;
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

    DataSourceBuilder createDataSourceBuilderDayEnd() {
        Platform platform = new PlatformBuilder().setType(PlatformType.PHONE).build();
        DataSourceBuilder dataSourceBuilder = new DataSourceBuilder().setType(DataSourceType.DAY_END).setPlatform(platform);
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.NAME, "Day End");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.DESCRIPTION, "Represents when day ended");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.DATA_TYPE, DataTypeLong.class.getName());
        ArrayList<HashMap<String, String>> dataDescriptors = new ArrayList<>();
        HashMap<String, String> dataDescriptor = new HashMap<>();
        dataDescriptor.put(METADATA.NAME, "Day End");
        dataDescriptor.put(METADATA.MIN_VALUE, String.valueOf(0));
        dataDescriptor.put(METADATA.MAX_VALUE, String.valueOf(Long.MAX_VALUE));
        dataDescriptor.put(METADATA.UNIT, "millisecond");
        dataDescriptor.put(METADATA.DESCRIPTION, "Contains day end time in millisecond");
        dataDescriptor.put(METADATA.DATA_TYPE, long.class.getName());
        dataDescriptors.add(dataDescriptor);
        dataSourceBuilder = dataSourceBuilder.setDataDescriptors(dataDescriptors);
        return dataSourceBuilder;
    }

    public void subscribeDayStart(){
        dataKitAPI.subscribe(dataSourceClientDayStart, new OnReceiveListener() {
            @Override
            public void onReceived(DataType dataType) {
                DataTypeLong dataTypeLong = (DataTypeLong) dataType;
                dayStartTime = dataTypeLong.getSample();
                callback.onDayStartChanged();
            }
        });
    }

    public void subscribeDayEnd(){
        dataKitAPI.subscribe(dataSourceClientDayEnd, new OnReceiveListener() {
            @Override
            public void onReceived(DataType dataType) {
                DataTypeLong dataTypeLong = (DataTypeLong) dataType;
                dayEndTime = dataTypeLong.getSample();
                callback.onDayEndChanged();
            }
        });
    }

    private void readDayStartFromDataKit() {
        ArrayList<DataSourceClient> dataSourceClients;
        dayStartTime=-1;
        if (dataKitAPI.isConnected()) {
            dataSourceClients = dataKitAPI.find(dataSourceBuilderDayStart);
            if(dataSourceClients.size()>0) {
                dataSourceClientDayStart = dataSourceClients.get(0);
                ArrayList<DataType> dataTypes = dataKitAPI.query(dataSourceClientDayStart, 1);
                if (dataTypes.size() != 0) {
                    DataTypeLong dataTypeLong = (DataTypeLong) dataTypes.get(0);
                    dayStartTime = dataTypeLong.getSample();
                }
            }
        }
    }

    private void readDayEndFromDataKit() {
        dayEndTime=-1;
        ArrayList<DataSourceClient> dataSourceClients;
        if (dataKitAPI.isConnected()) {
            dataSourceClients = dataKitAPI.find(dataSourceBuilderDayEnd);
            if(dataSourceClients.size()>0) {
                dataSourceClientDayEnd = dataSourceClients.get(0);
                ArrayList<DataType> dataTypes = dataKitAPI.query(dataSourceClientDayEnd, 1);
                if (dataTypes.size() != 0) {
                    DataTypeLong dataTypeLong = (DataTypeLong) dataTypes.get(0);
                    dayEndTime = dataTypeLong.getSample();
                }
            }
        }
    }

    public long getDayStartTime() {
        return dayStartTime;
    }

    public void setDayStartTime(long dayStartTime) {
        this.dayStartTime = dayStartTime;
    }

    public long getDayEndTime() {
        return dayEndTime;
    }

    public void setDayEndTime(long dayEndTime) {
        this.dayEndTime = dayEndTime;
    }
}
