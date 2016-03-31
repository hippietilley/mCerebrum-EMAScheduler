package org.md2k.ema_scheduler.condition.data_quality;

import android.content.Context;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.datatype.DataTypeIntArray;
import org.md2k.datakitapi.source.datasource.DataSource;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.time.DateTime;
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
        if(true) return true;
        DataKitAPI dataKitAPI=DataKitAPI.getInstance(context);

        long lastXMinute = Long.parseLong(configCondition.getValues().get(0));
        double limitPercentage = Double.parseDouble(configCondition.getValues().get(1));
        DataSource dataSource = configCondition.getData_source();
        long curTime = DateTime.getDateTime();
        int goodQuality = 0;
        int curQuality = -1;
        DataSourceBuilder dataSourceBuilder = new DataSourceBuilder(dataSource);
        dataSourceBuilder.setType(DataSourceType.STATUS);
        ArrayList<DataSourceClient> dataSourceClientArrayList = dataKitAPI.find(dataSourceBuilder);
        if (dataSourceClientArrayList.size() != 0) {
            ArrayList<DataType> dataTypes = dataKitAPI.query(dataSourceClientArrayList.get(0), curTime - lastXMinute, curTime);
            for (int i = 0; i < dataTypes.size(); i++) {
                int[] samples = ((DataTypeIntArray) dataTypes.get(i)).getSample();
                if (samples.length == 2) {
                    if (dataSource.getType().equals(DataSourceType.RESPIRATION)) {
                        curQuality = samples[0];
                    } else if (dataSource.getType().equals(DataSourceType.ECG)) {
                        curQuality = samples[1];
                    }
                } else curQuality = samples[0];
                if (curQuality == DATA_QUALITY.GOOD)
                    goodQuality++;
            }
        }
        double percentage=100.0*((double)goodQuality*(double)QUALITY_WINDOW)/((double)lastXMinute);
        if (percentage >= limitPercentage) {
            log(configCondition,"true: good_quality:"+String.valueOf(percentage));
            return true;
        }
        else {
            log(configCondition,"false: good_quality:"+String.valueOf(percentage));
            return false;
        }
    }
}
