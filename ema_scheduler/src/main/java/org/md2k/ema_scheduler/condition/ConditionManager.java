package org.md2k.ema_scheduler.condition;

import android.content.Context;

import org.md2k.datakitapi.time.DateTime;
import org.md2k.ema_scheduler.condition.battery_level.BatteryLevelManager;
import org.md2k.ema_scheduler.condition.data_quality.DataQualityManager;
import org.md2k.ema_scheduler.condition.last_ema_emi.LastEmaEmiManager;
import org.md2k.ema_scheduler.condition.not_active.NotActiveManager;
import org.md2k.ema_scheduler.condition.not_driving.DrivingDetectorManager;
import org.md2k.ema_scheduler.condition.privacy.PrivacyManager;
import org.md2k.ema_scheduler.condition.valid_block.ValidBlockManager;
import org.md2k.ema_scheduler.configuration.ConfigCondition;
import org.md2k.ema_scheduler.configuration.Configuration;
import org.md2k.ema_scheduler.logger.LogInfo;
import org.md2k.ema_scheduler.logger.LoggerManager;
import org.md2k.utilities.Report.Log;

import java.util.HashMap;

/**
 * Created by monowar on 3/20/16.
 */
public class ConditionManager {
    public static final String TYPE_PHONE_BATTERY="PHONE_BATTERY";
    public static final String TYPE_VALID_BLOCK="VALID_BLOCK";
    public static final String TYPE_DATA_QUALITY="DATA_QUALITY";
    public static final String TYPE_LAST_EMA_EMI="LAST_EMA_EMI";
    public static final String TYPE_NOT_ACTIVE="NOT_ACTIVE";
    public static final String TYPE_NOT_DRIVING="NOT_DRIVING";
    public static final String TYPE_PRIVACY="PRIVACY";
    private static final String TAG = ConditionManager.class.getSimpleName();
    HashMap<String, Condition> conditionHashMap;
    Configuration configuration;
    Context context;
    private static ConditionManager instance=null;
    public static ConditionManager getInstance(Context context){
        if(instance==null) instance=new ConditionManager(context);
        return instance;
    }
    private ConditionManager(Context context){
        this.context=context;
        conditionHashMap=new HashMap<>();
        configuration=Configuration.getInstance();
        ConfigCondition[] configConditions=configuration.getConditions();
        for (ConfigCondition configCondition : configConditions) {
            conditionHashMap.put(configCondition.getId(), createCondition(context, configCondition.getType()));
        }
    }
    Condition createCondition(Context context, String type){
        switch(type){
            case TYPE_PHONE_BATTERY:
                return new BatteryLevelManager(context);
            case TYPE_DATA_QUALITY:
                return new DataQualityManager(context);
            case TYPE_NOT_DRIVING:
                return new DrivingDetectorManager(context);
            case TYPE_LAST_EMA_EMI:
                return new LastEmaEmiManager(context);
            case TYPE_NOT_ACTIVE:
                return new NotActiveManager(context);
            case TYPE_VALID_BLOCK:
                return new ValidBlockManager(context);
            case TYPE_PRIVACY:
                return new PrivacyManager(context);
        }
        return null;
    }
    public boolean isValid(String[] conditions, String type, String id){
        if(conditions==null) return true;
        for (String condition : conditions) {
            Log.d(TAG, "condition=" + condition);
            ConfigCondition configCondition = configuration.getConditions(condition);
            if (!conditionHashMap.get(condition).isValid(configCondition)) {
                Log.d(TAG,"condition="+condition+" false");
                return false;
            }else{
                Log.d(TAG,"condition="+condition+" true");
            }
        }
        log(type,id);
        return true;
    }
    protected void log(String type, String id){
        LogInfo logInfo=new LogInfo();
        logInfo.setOperation(LogInfo.OP_CONDITION);
        logInfo.setId(id);
        logInfo.setType(type);
        logInfo.setTimestamp(DateTime.getDateTime());
        logInfo.setMessage("true: all conditions okay");
        LoggerManager.getInstance(context).insert(logInfo);
    }

}
