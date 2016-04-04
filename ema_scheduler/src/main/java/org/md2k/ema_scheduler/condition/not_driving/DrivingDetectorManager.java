package org.md2k.ema_scheduler.condition.not_driving;

import android.content.Context;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.datatype.DataTypeDoubleArray;
import org.md2k.datakitapi.source.application.ApplicationBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.ema_scheduler.condition.Condition;
import org.md2k.ema_scheduler.configuration.ConfigCondition;

import java.util.ArrayList;

/**
 * Created by monowar on 3/26/16.
 */
public class DrivingDetectorManager extends Condition {
    public DrivingDetectorManager(Context context) {
        super(context);
    }

    public boolean isValid(ConfigCondition configCondition) {
        DataKitAPI dataKitAPI = DataKitAPI.getInstance(context);
        long lastXMinute = Long.parseLong(configCondition.getValues().get(0));
        double limitPercentage = Double.parseDouble(configCondition.getValues().get(1));
        int notDriving = 0;
        boolean result = false;
        DrivingDetector drivingDetector = new DrivingDetector();
        long curTime = DateTime.getDateTime();
        ApplicationBuilder applicationBuilder = new ApplicationBuilder().setId("org.md2k.phonesensor");
        DataSourceBuilder dataSourceBuilder = new DataSourceBuilder().setType(DataSourceType.LOCATION).setApplication(applicationBuilder.build());
        ArrayList<DataSourceClient> dataSourceClientArrayList = dataKitAPI.find(dataSourceBuilder);
        if (dataSourceClientArrayList.size() != 0) {
            ArrayList<DataType> dataTypes = dataKitAPI.queryHFlastN(dataSourceClientArrayList.get(0), 30);
            long lastTimestamp=-1;
            double lastSpeed=0.0;
            for (int i = 0; i < dataTypes.size(); i++) {
                double speed = ((DataTypeDoubleArray) dataTypes.get(i)).getSample()[3];
                long timestamp=dataTypes.get(i).getDateTime();
                if(lastTimestamp==-1){
                    lastTimestamp=timestamp;
                    lastSpeed=speed;
                }else {
                    if (!(drivingDetector.getDrivingStatus(lastTimestamp, timestamp, speed) == DrivingDetector.DrivingStatus.DRIVING))
                        notDriving++;
                }
            }
            if (dataTypes.size() == 0) {
                log(configCondition, "true: no data point found");
                return true;
            } else {
                double percentage = 100.0 * ((double) notDriving) / ((double) (dataTypes.size()));
                if (percentage >= limitPercentage) {
                    log(configCondition, "true: not driving = " + percentage + "%% > " + limitPercentage + " %%");
                    return true;
                } else {
                    log(configCondition, "false: not driving = " + percentage + "%% < " + limitPercentage + " %%");
                    return false;
                }
            }
        }
        log(configCondition, "true: no datasource found");
        return false;
    }
}
