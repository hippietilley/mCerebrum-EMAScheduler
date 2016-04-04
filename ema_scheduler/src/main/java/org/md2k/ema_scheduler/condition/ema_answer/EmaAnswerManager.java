package org.md2k.ema_scheduler.condition.ema_answer;

import android.content.Context;

import org.md2k.ema_scheduler.condition.Condition;
import org.md2k.ema_scheduler.configuration.ConfigCondition;
import org.md2k.ema_scheduler.logger.LogInfo;
import org.md2k.ema_scheduler.logger.LoggerManager;

/**
 * Created by monowar on 3/26/16.
 */
public class EmaAnswerManager extends Condition {
    public EmaAnswerManager(Context context) {
        super(context);
    }

    public boolean isValid(ConfigCondition configCondition) {
        LogInfo logInfoDeliver= LoggerManager.getInstance(context).getLogInfoLast(LogInfo.OP_DELIVER, LogInfo.STATUS_DELIVER_SUCCESS, null, null);
        LogInfo logInfoComplete=LoggerManager.getInstance(context).getLogInfoLast(LogInfo.OP_RUN,LogInfo.STATUS_RUN_COMPLETED, null, null);
        long timeDiff=logInfoComplete.getTimestamp()-logInfoDeliver.getTimestamp();
        long require=Long.parseLong(configCondition.getValues().get(0));
        if(timeDiff<=require) return true;
        else return false;
    }
}
