package org.md2k.ema_scheduler.scheduler;

import android.content.Context;

import org.md2k.ema_scheduler.configuration.EMAType;
import org.md2k.ema_scheduler.delivery.DeliveryManager;
import org.md2k.ema_scheduler.logger.LoggerManager;

/**
 * Created by monowar on 3/14/16.
 */
abstract public class Scheduler {
    Context context;
    EMAType emaType;
    LoggerManager loggerManager;
    DeliveryManager deliveryManager;
    WindowManager windowManager;

    public Scheduler(Context context, EMAType emaType){
        this.context=context;
        this.emaType=emaType;
        loggerManager=LoggerManager.getInstance(context);
        deliveryManager = new DeliveryManager(context,emaType);
        windowManager=new WindowManager(context, emaType.getWindows());
    }

    abstract public void start();

    abstract public void stop();

    public void startDeliver(){
        deliveryManager.start();
    }
    public void stopDeliver(){
        deliveryManager.stop();
    }
}
