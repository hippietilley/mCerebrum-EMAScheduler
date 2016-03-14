package org.md2k.ema_scheduler.scheduler;

import android.content.Context;
import android.os.Handler;

import org.md2k.ema_scheduler.configuration.Configuration;
import org.md2k.ema_scheduler.configuration.EMAType;
import org.md2k.ema_scheduler.delivery.DeliveryManager;
import org.md2k.utilities.Report.Log;

/**
 * Created by monowar on 3/10/16.
 */
public class SchedulerManager {
    private static final String TAG = SchedulerManager.class.getSimpleName();
    Context context;
    DeliveryManager deliveryManager;
    Handler handler;
    Configuration configuration;

    public SchedulerManager(Context context){
        this.context=context;
        deliveryManager=new DeliveryManager(context);
        handler=new Handler();
        configuration=Configuration.getInstance();
    }
    public void start(){
        handler.postDelayed(deliver, 4000);

    }
    public void stop(){
        Log.d(TAG, "Stop()");
        handler.removeCallbacks(deliver);
        deliveryManager.stop();
    }
    Runnable deliver=new Runnable() {
        @Override
        public void run() {
            deliveryManager.start(configuration.getEma_types()[0]);
        }
    };
}
