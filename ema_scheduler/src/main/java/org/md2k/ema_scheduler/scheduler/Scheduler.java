package org.md2k.ema_scheduler.scheduler;

import android.content.Context;

import org.md2k.datakitapi.time.DateTime;
import org.md2k.ema_scheduler.condition.ConditionManager;
import org.md2k.ema_scheduler.configuration.EMAType;
import org.md2k.ema_scheduler.day.DayManager;
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
    DayManager dayManager;

    public Scheduler(Context context, EMAType emaType, DayManager dayManager){
        this.context=context;
        this.emaType=emaType;
        this.dayManager=dayManager;
        deliveryManager = DeliveryManager.getInstance(context);
        blockManager =new BlockManager(context, emaType.getBlocks(),dayManager);
    }

     public void start(){
         loggerManager=LoggerManager.getInstance(context);
         conditionManager = ConditionManager.getInstance(context);
     }

    abstract public void stop();
    abstract public void reset();

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
