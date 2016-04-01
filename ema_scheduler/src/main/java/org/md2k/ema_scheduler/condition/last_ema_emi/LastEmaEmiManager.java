package org.md2k.ema_scheduler.condition.last_ema_emi;

import android.content.Context;

import org.md2k.datakitapi.time.DateTime;
import org.md2k.ema_scheduler.Constants;
import org.md2k.ema_scheduler.condition.Condition;
import org.md2k.ema_scheduler.configuration.ConfigCondition;
import org.md2k.ema_scheduler.logger.LogInfo;
import org.md2k.ema_scheduler.logger.LoggerManager;

/**
 * Created by monowar on 3/26/16.
 */
public class LastEmaEmiManager extends Condition {

    public LastEmaEmiManager(Context context) {
        super(context);
    }

    public boolean isValid(ConfigCondition configCondition) {
        if(Constants.DEBUG) return true;

        LoggerManager loggerManager= LoggerManager.getInstance(context);
        LogInfo logInfo=loggerManager.getLogInfoLast(LogInfo.OP_DELIVER, configCondition.getSource().getType(), configCondition.getSource().getId());
        if(logInfo==null) {
            log(configCondition, "true: not triggered yet");
            return true;
        }else {
            long diff= Long.parseLong(configCondition.getValues().get(0));
            long curTime= DateTime.getDateTime();
            long min=((curTime-logInfo.getTimestamp())/(1000*60));
            if(curTime-logInfo.getTimestamp()>diff){
                log(configCondition, "true: last triggered "+String.valueOf(min)+" (require: "+ String.valueOf(diff/(1000*60))+") minute ago");
                return true;
            }else{
                log(configCondition, "false: last triggered "+String.valueOf(min)+" (require: "+ String.valueOf(diff/(1000*60))+") minute ago");
                return false;
            }
        }
    }

}
