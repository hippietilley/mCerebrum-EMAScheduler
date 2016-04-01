package org.md2k.ema_scheduler.condition.data_quality;

import android.content.Context;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.datatype.DataTypeInt;
import org.md2k.datakitapi.source.datasource.DataSource;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.ema_scheduler.Constants;
import org.md2k.ema_scheduler.condition.Condition;
import org.md2k.ema_scheduler.configuration.ConfigCondition;
import org.md2k.utilities.data_format.DATA_QUALITY;

import java.util.ArrayList;

/**
 * Created by monowar on 3/26/16.
 */
public class DataQualityManager extends Condition {
    public static final String TAG=DataQualityManager.class.getSimpleName();
    public static final int QUALITY_WINDOW = 5000;

    public DataQualityManager(Context context) {
        super(context);
    }

    public boolean isValid(ConfigCondition configCondition) {
        if(Constants.DEBUG) return true;
        long lastXMinute = Long.parseLong(configCondition.getValues().get(0));
        double limitPercentage = Double.parseDouble(configCondition.getValues().get(1));
        DataSource dataSource = configCondition.getData_source();
        DataSourceBuilder dataSourceBuilder = new DataSourceBuilder(dataSource);
        double percentage=getDataQuality(dataSourceBuilder,lastXMinute);
        if (percentage >= limitPercentage) {
            log(configCondition,"true: good_quality:"+String.valueOf(percentage));
            return true;
        }
        else {
            log(configCondition,"false: good_quality:"+String.valueOf(percentage));
            return false;
        }
    }
    public double getDataQuality(DataSourceBuilder dataSourceBuilder, long lastXMinute){
        long curTime = DateTime.getDateTime();
        int goodQuality = 0;
        DataKitAPI dataKitAPI=DataKitAPI.getInstance(context);
        ArrayList<DataSourceClient> dataSourceClientArrayList = dataKitAPI.find(dataSourceBuilder);
        if (dataSourceClientArrayList.size() != 0) {
            ArrayList<DataType> dataTypes = dataKitAPI.query(dataSourceClientArrayList.get(0), curTime - lastXMinute, curTime);
            for (int i = 0; i < dataTypes.size(); i++) {
                int curQuality = ((DataTypeInt) dataTypes.get(i)).getSample();
                if (curQuality == DATA_QUALITY.GOOD)
                    goodQuality++;
            }
        }
        return 100.0*((double)goodQuality*(double)QUALITY_WINDOW)/((double)lastXMinute);

    }
}
