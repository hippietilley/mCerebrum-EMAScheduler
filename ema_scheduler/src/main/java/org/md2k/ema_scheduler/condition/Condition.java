package org.md2k.ema_scheduler.condition;

import android.content.Context;

import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.ema_scheduler.configuration.ConfigCondition;
import org.md2k.ema_scheduler.logger.LogInfo;
import org.md2k.ema_scheduler.logger.LoggerManager;

/**
 * Created by monowar on 3/27/16.
 */
public abstract class Condition {
    private static final String TAG = Condition.class.getSimpleName();
    protected Context context;
    public abstract boolean isValid(ConfigCondition configCondition) throws DataKitException;
    protected Condition(Context context){
        this.context=context;
    }
    protected void log(ConfigCondition configCondition, String message) throws DataKitException {
        LogInfo logInfo=new LogInfo();
        logInfo.setOperation(LogInfo.OP_CONDITION);
        logInfo.setId(configCondition.getId());
        logInfo.setType(configCondition.getType());
        logInfo.setTimestamp(DateTime.getDateTime());
        logInfo.setMessage(message);
        LoggerManager.getInstance(context).insert(logInfo);
    }
}
