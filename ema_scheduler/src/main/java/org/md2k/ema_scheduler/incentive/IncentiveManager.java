package org.md2k.ema_scheduler.incentive;

import android.content.Context;

/**
 * Created by monowar on 4/1/16.
 */
public class IncentiveManager {
    private static final String TAG = IncentiveManager.class.getSimpleName();
    Context context;
    public IncentiveManager(Context context){
        this.context=context;
    }

/*    public void readDayStart() {
        DataKitAPI dataKitAPI=DataKitAPI.getInstance(context);
        ArrayList<DataType> dataTypes=dataKitAPI.query(dataSourceClientLogger, startTimestamp, endTimestamp);
        for(int i=0;i<dataTypes.size();i++){
            DataTypeString dataTypeString= (DataTypeString) dataTypes.get(i);
            LogInfo logInfo=gson.fromJson(dataTypeString.getSample(),LogInfo.class);
            logInfos.add(logInfo);
        }
        Log.d(TAG, "readLogInfosFromDataKit...size=" + logInfos.size());
    }

    DataSourceBuilder createDataSourceBuilderLogger() {
        Platform platform = new PlatformBuilder().setType(PlatformType.PHONE).build();
        DataSourceBuilder dataSourceBuilder = new DataSourceBuilder().setType(DataSourceType.LOG).setPlatform(platform);
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.NAME, "Log");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.DESCRIPTION, "Represents the log of EMA Scheduler");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.DATA_TYPE, DataTypeString.class.getName());
        ArrayList<HashMap<String, String>> dataDescriptors = new ArrayList<>();
        HashMap<String, String> dataDescriptor = new HashMap<>();
        dataDescriptor.put(METADATA.NAME, "Log");
        dataDescriptor.put(METADATA.UNIT, "string");
        dataDescriptor.put(METADATA.DESCRIPTION, "Contains log");
        dataDescriptor.put(METADATA.DATA_TYPE, String.class.getName());
        dataDescriptors.add(dataDescriptor);
        dataSourceBuilder = dataSourceBuilder.setDataDescriptors(dataDescriptors);
        return dataSourceBuilder;
    }
*/
}
