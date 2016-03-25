package org.md2k.ema_scheduler.scheduler;

import android.content.Context;
import android.os.Handler;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.ema_scheduler.condition.ConditionManager;
import org.md2k.ema_scheduler.configuration.EMAType;
import org.md2k.ema_scheduler.day.DayManager;
import org.md2k.utilities.Report.Log;

/**
 * Created by monowar on 3/14/16.
 */
public class EMIScheduler extends Scheduler {
    private static final String TAG = EMIScheduler.class.getSimpleName();
    DataKitAPI dataKitAPI;
    Handler handler;

    public EMIScheduler(Context context, EMAType emaType, DayManager dayManager) {
        super(context, emaType, dayManager);
        Log.d(TAG, "EventEMAScheduler()...");
        conditionManager = new ConditionManager();
        handler = new Handler();
    }

    @Override
    public void start() {
        super.start();
        Log.d(TAG, "start()...");
    }

    @Override
    public void stop() {
        Log.d(TAG, "stop()...");
    }
    @Override
    public void reset(){

    }

}
