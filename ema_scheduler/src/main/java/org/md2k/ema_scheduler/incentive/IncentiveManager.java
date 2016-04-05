package org.md2k.ema_scheduler.incentive;

import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.datatype.DataTypeString;
import org.md2k.datakitapi.source.METADATA;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.source.platform.Platform;
import org.md2k.datakitapi.source.platform.PlatformBuilder;
import org.md2k.datakitapi.source.platform.PlatformType;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.ema_scheduler.condition.ConditionManager;
import org.md2k.ema_scheduler.configuration.EMAType;
import org.md2k.ema_scheduler.configuration.IncentiveRule;
import org.md2k.utilities.Report.Log;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by monowar on 4/1/16.
 */
public class IncentiveManager {
    private static final String TAG = IncentiveManager.class.getSimpleName();
    Context context;
    DataSourceClient dataSourceClient;
    EMAType emaType;
    protected ConditionManager conditionManager;

    public IncentiveManager(Context context, EMAType emaType) {
        this.context = context;
        this.emaType=emaType;
        conditionManager = ConditionManager.getInstance(context);
        register();
    }
    public void start(){
        if(emaType.getIncentive_rules()==null) return;
        for(int i=0;i<emaType.getIncentive_rules().length;i++){
            if (conditionManager.isValid(emaType.getIncentive_rules()[i].getConditions(), emaType.getType(), emaType.getId())) {
                saveIncentiveToDataKit(emaType, emaType.getIncentive_rules()[i]);
                show(emaType.getIncentive_rules()[i]);
                break;
            }
        }
    }
    private void show(IncentiveRule incentiveRule){
        Intent intent = new Intent(context, ActivityIncentive.class);
        intent.putExtra("messages",incentiveRule.getMessages());
        intent.putExtra("total_incentive",(getLastTotalIncentive()+incentiveRule.getIncentive()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public double getLastTotalIncentive() {
        Gson gson = new Gson();
        ArrayList<DataType> dataTypes = DataKitAPI.getInstance(context).query(dataSourceClient, 1);
        if (dataTypes.size() == 0) return 0;
        DataTypeString dataTypeString = (DataTypeString) dataTypes.get(0);
        Incentive incentive = gson.fromJson(dataTypeString.getSample(), Incentive.class);
        return incentive.getTotalIncentive();
    }

    public void saveIncentiveToDataKit(EMAType emaType, IncentiveRule incentiveRule) {
        Incentive incentive=new Incentive();
        incentive.emaId=emaType.getId();
        incentive.emaType=emaType.getType();
        incentive.timeStamp=DateTime.getDateTime();
        incentive.incentive=incentiveRule.getIncentive();
        incentive.totalIncentive=getLastTotalIncentive()+incentive.getIncentive();
        Gson gson = new Gson();
        String string = gson.toJson(incentive);
        Log.d(TAG, "insert()..." + string);
        DataTypeString dataTypeString = new DataTypeString(DateTime.getDateTime(), string);
        DataKitAPI.getInstance(context).insert(dataSourceClient, dataTypeString);
    }
    private void register() {
        dataSourceClient = DataKitAPI.getInstance(context).register(createDataSourceBuilderLogger());
    }
    DataSourceBuilder createDataSourceBuilderLogger() {
        Platform platform = new PlatformBuilder().setType(PlatformType.PHONE).build();
        DataSourceBuilder dataSourceBuilder = new DataSourceBuilder().setType(DataSourceType.INCENTIVE).setPlatform(platform);
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.NAME, "Incentive");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.DESCRIPTION, "Represents the log of EMA Scheduler");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.DATA_TYPE, DataTypeString.class.getName());
        ArrayList<HashMap<String, String>> dataDescriptors = new ArrayList<>();
        HashMap<String, String> dataDescriptor = new HashMap<>();
        dataDescriptor.put(METADATA.NAME, "Incentive");
        dataDescriptor.put(METADATA.UNIT, "String");
        dataDescriptor.put(METADATA.DESCRIPTION, "Contains incentive");
        dataDescriptor.put(METADATA.DATA_TYPE, Incentive.class.getName());
        dataDescriptors.add(dataDescriptor);
        dataSourceBuilder = dataSourceBuilder.setDataDescriptors(dataDescriptors);
        return dataSourceBuilder;
    }
}