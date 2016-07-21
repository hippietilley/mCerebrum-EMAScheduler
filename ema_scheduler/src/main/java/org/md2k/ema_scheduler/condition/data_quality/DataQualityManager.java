package org.md2k.ema_scheduler.condition.data_quality;

import android.content.Context;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.datatype.DataTypeInt;
import org.md2k.datakitapi.datatype.DataTypeLong;
import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.source.datasource.DataSource;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.ema_scheduler.condition.Condition;
import org.md2k.ema_scheduler.configuration.ConfigCondition;
import org.md2k.ema_scheduler.logger.LogInfo;
import org.md2k.ema_scheduler.logger.LoggerManager;
import org.md2k.utilities.data_format.DATA_QUALITY;

import java.util.ArrayList;

/**
 * Created by monowar on 3/26/16.
 */
public class DataQualityManager extends Condition {
    public static final String TAG = DataQualityManager.class.getSimpleName();
    public static final String DAY_START = "DAY_START";
    public static final String LAST_EMA = "LAST_EMA";
    public static final int QUALITY_WINDOW = 3000;

    public DataQualityManager(Context context) {
        super(context);
    }

    public boolean isValid(ConfigCondition configCondition) throws DataKitException {
        long lastXTimeStamp = getLastXTimeStamp(configCondition);
        if (lastXTimeStamp == -1) return false;
        double limitPercentage = Double.parseDouble(configCondition.getValues().get(1));
        DataSource dataSource = configCondition.getData_source();
        DataSourceBuilder dataSourceBuilder = new DataSourceBuilder(dataSource);
        double percentage = getDataQuality(dataSourceBuilder, lastXTimeStamp);
        if (percentage >= limitPercentage) {
            log(configCondition, "true: good_quality:" + String.valueOf(percentage));
            return true;
        } else {
            log(configCondition, "false: good_quality:" + String.valueOf(percentage));
            return false;
        }
    }
    boolean isWearing(int value){
        if(value==DATA_QUALITY.GOOD || value==DATA_QUALITY.BAND_LOOSE || value==DATA_QUALITY.NOISE)
            return true;
        return false;
    }


    public double getDataQuality(DataSourceBuilder dataSourceBuilder, long lastXTimeStamp) throws DataKitException {
        long curTime = DateTime.getDateTime();
        int goodQuality = 0;
        DataKitAPI dataKitAPI = DataKitAPI.getInstance(context);
        ArrayList<DataSourceClient> dataSourceClientArrayList = dataKitAPI.find(dataSourceBuilder);
        if (dataSourceClientArrayList.size() != 0) {
            ArrayList<DataType> dataTypes = dataKitAPI.query(dataSourceClientArrayList.get(0), curTime - lastXTimeStamp, curTime);
            for (int i = 0; i < dataTypes.size(); i++) {
                int curQuality = ((DataTypeInt) dataTypes.get(i)).getSample();
                if (isWearing(curQuality))
                    goodQuality++;
            }
        }
        return 100.0 * ((double) goodQuality * (double) QUALITY_WINDOW) / ((double) lastXTimeStamp);

    }

    long getLastXTimeStamp(ConfigCondition configCondition) throws DataKitException {
        long lastXTimeStamp = -1;
        if (configCondition.getValues().get(0).equals(DAY_START)) {
            lastXTimeStamp = getDay(DataSourceType.DAY_START);
        } else if (configCondition.getValues().get(0).equals(LAST_EMA)) {
            lastXTimeStamp=getLastEMA();
            if(lastXTimeStamp==-1)
                lastXTimeStamp=getDay(DataSourceType.DAY_START);
        } else lastXTimeStamp = Long.parseLong(configCondition.getValues().get(0));
        return lastXTimeStamp;
    }
    long getLastEMA(){
        LogInfo logInfo= LoggerManager.getInstance(context).getLogInfoLast(LogInfo.OP_DELIVER, LogInfo.STATUS_DELIVER_SUCCESS, null, null);
        if(logInfo==null) return -1;
        return logInfo.getTimestamp();
    }

    long getDay(String dataSourceType) throws DataKitException {
        long day = -1;
        DataKitAPI dataKitAPI = DataKitAPI.getInstance(context);
        DataSourceBuilder dataSourceBuilder = new DataSourceBuilder().setType(dataSourceType);
        ArrayList<DataSourceClient> dataSourceClients = dataKitAPI.find(dataSourceBuilder);

        if (dataSourceClients.size() == 0) {
            return day;
        }
        ArrayList<DataType> dataTypes = dataKitAPI.query(dataSourceClients.get(0), 1);
        if (dataTypes.size() == 0) return day;
        return ((DataTypeLong) dataTypes.get(0)).getSample();
    }
}
