package org.md2k.ema_scheduler.incentive;

import android.content.Context;

import org.md2k.ema_scheduler.logger.LogInfo;
import org.md2k.ema_scheduler.logger.LoggerManager;
import org.md2k.ema_scheduler.runner.RunnerMonitor;

/**
 * Created by monowar on 3/31/16.
 */
public class IncentiveManagerNW {
    Context context;
    IncentiveManagerNW(Context context){
        this.context=context;

    }
    public String getCurrentMessage(){
        LogInfo logInfo= LoggerManager.getInstance(context).getLogInfoLast(LogInfo.OP_RUN, null, null);
        if(logInfo.getMessage().equals(RunnerMonitor.TYPE_COMPLETED)){
            return "Thank you for answering this EMA";
        }
        else{
            return "Sorry, you missed this EMA";
        }

    }
    public String getCurrentIncentiveString() {
        LogInfo logInfo= LoggerManager.getInstance(context).getLogInfoLast(LogInfo.OP_RUN, null, null);
        if(!logInfo.getMessage().equals(RunnerMonitor.TYPE_COMPLETED)){
            return "For this EMA you earned: $0.0";
        }else{
            double curIncentive=getCurrentIncentive(logInfo);
            return "For this EMA you earned: $"+String.format("%1.2f",curIncentive);
        }
    }
    private double getCurrentIncentive(LogInfo logInfo){
        if(logInfo.getId().equals("END_OF_DAY_EMA"))
            return 1.0;
        else{
            LogInfo logInfoStart= LoggerManager.getInstance(context).getLogInfoLast(LogInfo.OP_RUN, null, null, RunnerMonitor.TYPE_START);
            if(logInfo.getTimestamp()-logInfoStart.getTimestamp()<5*60*1000)
                return 0.75;
            else return 0.50;

        }
    }
}
