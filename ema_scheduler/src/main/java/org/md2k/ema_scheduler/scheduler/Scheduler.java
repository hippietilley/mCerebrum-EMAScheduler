package org.md2k.ema_scheduler.scheduler;

import android.content.Context;

import org.md2k.datakitapi.time.DateTime;
import org.md2k.ema_scheduler.condition.ConditionManager;
import org.md2k.ema_scheduler.configuration.EMAType;
import org.md2k.ema_scheduler.delivery.DeliveryManager;
import org.md2k.ema_scheduler.logger.LogInfo;
import org.md2k.ema_scheduler.logger.LoggerManager;
import org.md2k.utilities.Report.Log;

/**
 * Created by monowar on 3/14/16.
 */
abstract public class Scheduler {
    private static final String TAG = Scheduler.class.getSimpleName();
    Context context;
    EMAType emaType;
    LoggerManager loggerManager;
    DeliveryManager deliveryManager;
    BlockManager blockManager;
    ConditionManager conditionManager;
    long dayStartTimestamp;
    long dayEndTimestamp;

    public Scheduler(Context context, EMAType emaType){
        this.context=context;
        this.emaType=emaType;
        deliveryManager = DeliveryManager.getInstance(context);
        blockManager =new BlockManager(context, emaType.getBlocks());
    }

     public void start(long dayStartTimestamp, long dayEndTimestamp){
         loggerManager=LoggerManager.getInstance(context);
         conditionManager = ConditionManager.getInstance(context);
         this.dayStartTimestamp=dayStartTimestamp;
         this.dayEndTimestamp=dayEndTimestamp;
     }

    abstract public void stop();
    abstract public void setDayStartTimestamp(long dayStartTimestamp);
    abstract public void setDayEndTimestamp(long dayEndTimestamp);

    public void startDelivery(){
        Log.d(TAG, "startDelivery...emaType="+emaType.getType()+" emaId="+emaType.getId());
        deliveryManager.start(emaType, true, "SYSTEM");
    }
    public void stopDelivery(){
        Log.d(TAG,"stopDelivery...");
        deliveryManager.stop();
    }
    public void log(String message){
        LogInfo logInfo=new LogInfo();
        logInfo.setOperation(LogInfo.OP_SCHEDULE);
        logInfo.setType(emaType.getType());
        logInfo.setId(emaType.getId());
        logInfo.setTimestamp(DateTime.getDateTime());
        logInfo.setMessage(message);
        loggerManager.insert(logInfo);
    }
}
