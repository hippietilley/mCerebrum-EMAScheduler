package org.md2k.ema_scheduler.incentive;

import android.content.Context;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.datatype.DataTypeLong;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.utilities.Report.Log;

import java.util.ArrayList;

/**
 * Created by monowar on 4/2/16.
 */
public class Day {
    private static final String TAG = Day.class.getSimpleName();
    Context context;
    public Day(Context context){
        this.context=context;
    }

    public long readDayStartFromDataKit() {
        long dayStartTime;
        DataKitAPI dataKitAPI=DataKitAPI.getInstance(context);
        Log.d(TAG, "readDayStartFromDataKit()...");
        ArrayList<DataSourceClient> dataSourceClients;
        dayStartTime = -1;
        dataSourceClients = dataKitAPI.find(new DataSourceBuilder().setType(DataSourceType.DAY_START));
        Log.d(TAG,"readDayStartFromDataKit()...find..dataSourceClient.size()="+dataSourceClients.size());
        if (dataSourceClients.size() > 0) {
            ArrayList<DataType> dataTypes = dataKitAPI.query(dataSourceClients.get(0), 1);
            if (dataTypes.size() != 0) {
                DataTypeLong dataTypeLong = (DataTypeLong) dataTypes.get(0);
                dayStartTime = dataTypeLong.getSample();
            }
        }
        return dayStartTime;
    }

    public long readDayEndFromDataKit() {
        long dayEndTime=-1;
        DataKitAPI dataKitAPI=DataKitAPI.getInstance(context);
        ArrayList<DataSourceClient> dataSourceClients;
        dataSourceClients = dataKitAPI.find(new DataSourceBuilder().setType(DataSourceType.DAY_END));
        Log.d(TAG,"readDayEndFromDataKit()...find..dataSourceClient.size()="+dataSourceClients.size());
        if (dataSourceClients.size() > 0) {
            ArrayList<DataType> dataTypes = dataKitAPI.query(dataSourceClients.get(0), 1);
            if (dataTypes.size() != 0) {
                DataTypeLong dataTypeLong = (DataTypeLong) dataTypes.get(0);
                dayEndTime = dataTypeLong.getSample();
                Log.d(TAG,"readDayEndFromDataKit()...dayEndTime="+dayEndTime);
            }
        }
        return dayEndTime;
    }
}
