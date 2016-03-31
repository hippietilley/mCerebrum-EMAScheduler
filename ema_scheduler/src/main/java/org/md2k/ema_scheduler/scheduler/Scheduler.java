package org.md2k.ema_scheduler.scheduler;

import android.content.Context;

import org.md2k.datakitapi.time.DateTime;
import org.md2k.ema_scheduler.condition.ConditionManager;
import org.md2k.ema_scheduler.configuration.EMAType;
import org.md2k.ema_scheduler.delivery.DeliveryManager;
import org.md2k.ema_scheduler.logger.LogInfo;
import org.md2k.ema_scheduler.logger.LogSchedule;
import org.md2k.ema_scheduler.logger.LoggerManager;
import org.md2k.utilities.Report.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by monowar on 3/14/16.
 */
abstract public class Scheduler {
    private static final String TAG = Scheduler.class.getSimpleName();
    protected Context context;
    protected EMAType emaType;
    LoggerManager loggerManager;
    DeliveryManager deliveryManager;
    BlockManager blockManager;
    protected ConditionManager conditionManager;
    protected long dayStartTimestamp;
    protected long dayEndTimestamp;

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
        Log.d(TAG, "stopDelivery...");
        deliveryManager.stop();
    }
    protected void sendToLogInfo(long scheduledTime) {
        LogSchedule logSchedule = new LogSchedule();
        logSchedule.setScheduleTimestamp(scheduledTime);
        LogInfo logInfo = new LogInfo();
        logInfo.setId(emaType.getId());
        logInfo.setType(emaType.getType());
        logInfo.setTimestamp(DateTime.getDateTime());
        logInfo.setOperation(LogInfo.OP_SCHEDULE);
        logInfo.setLogSchedule(logSchedule);
        logInfo.setMessage("scheduled at: " + formatTime(scheduledTime));
        loggerManager.insert(logInfo);
    }
    String formatTime(long timestamp) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            Date currenTimeZone = calendar.getTime();
            return sdf.format(currenTimeZone);
        } catch (Exception e) {
        }
        return "";
    }

/*    public void log(String message){
        LogInfo logInfo=new LogInfo();
        logInfo.setOperation(LogInfo.OP_SCHEDULE);
        logInfo.setType(emaType.getType());
        logInfo.setId(emaType.getId());
        logInfo.setTimestamp(DateTime.getDateTime());
        logInfo.setMessage(message);
        loggerManager.insert(logInfo);
    }
    */
}
