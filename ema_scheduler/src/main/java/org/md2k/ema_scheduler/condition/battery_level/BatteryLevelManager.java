package org.md2k.ema_scheduler.condition.battery_level;

import android.content.Context;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.datatype.DataTypeDoubleArray;
import org.md2k.datakitapi.source.datasource.DataSource;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.ema_scheduler.Constants;
import org.md2k.ema_scheduler.condition.Condition;
import org.md2k.ema_scheduler.configuration.ConfigCondition;

import java.util.ArrayList;

/**
 * Created by monowar on 3/26/16.
 */
public class BatteryLevelManager extends Condition {
    public BatteryLevelManager(Context context) {
        super(context);
    }

    public boolean isValid(ConfigCondition configCondition) {
        if(Constants.DEBUG) return true;
        DataKitAPI dataKitAPI=DataKitAPI.getInstance(context);
        double limitPercentage = Double.parseDouble(configCondition.getValues().get(0));
        DataSource dataSource = configCondition.getData_source();
        DataSourceBuilder dataSourceBuilder = new DataSourceBuilder(dataSource);
        ArrayList<DataSourceClient> dataSourceClientArrayList = dataKitAPI.find(dataSourceBuilder);
        if (dataSourceClientArrayList.size() != 0) {
            ArrayList<DataType> dataTypes = dataKitAPI.queryHFlastN(dataSourceClientArrayList.get(0), 1);
            if (dataTypes.size() == 0) {
                log(configCondition, "true: datapoint not found");
                return true;
            }
            double[] samples = ((DataTypeDoubleArray) dataTypes.get(0)).getSample();
            if (samples[0] > limitPercentage) {
                log(configCondition, "true: " + samples[0] + " > " + limitPercentage);
                return true;
            } else {
                log(configCondition, "false: " + samples[0] + " < " + limitPercentage);
                return false;
            }
        } else {
            log(configCondition, "true: datasource not found");
            return true;
        }
    }
}
