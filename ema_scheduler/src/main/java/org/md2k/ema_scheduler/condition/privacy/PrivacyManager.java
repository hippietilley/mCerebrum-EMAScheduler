package org.md2k.ema_scheduler.condition.privacy;

import android.content.Context;

import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.datatype.DataTypeDouble;
import org.md2k.datakitapi.source.datasource.DataSource;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.ema_scheduler.condition.Condition;
import org.md2k.ema_scheduler.configuration.ConfigCondition;

import java.util.ArrayList;

/**
 * Created by monowar on 3/26/16.
 */
public class PrivacyManager extends Condition{
    public PrivacyManager(Context context){
        super(context);
    }
    public boolean isValid(ConfigCondition configCondition){
        if(true) return true;
        //TODO: implement privacy validity
        DataSource dataSource = configCondition.getData_source();
        DataSourceBuilder dataSourceBuilder = new DataSourceBuilder(dataSource);
        ArrayList<DataSourceClient> dataSourceClientArrayList = dataKitAPI.find(dataSourceBuilder);
        if (dataSourceClientArrayList.size() != 0) {
            ArrayList<DataType> dataTypes = dataKitAPI.query(dataSourceClientArrayList.get(0), 1);
            if (dataTypes.size() == 0) {
                log(configCondition, "true: datapoint not found");
                return true;
            }
            double sample = ((DataTypeDouble) dataTypes.get(0)).getSample();
            if (sample == 1) {
                log(configCondition, "false: person walking/running");
                return false;
            } else {
                log(configCondition, "true: person not active");
                return true;
            }
        } else {
            log(configCondition, "true: datasource not found");
            return true;
        }
    }
}
