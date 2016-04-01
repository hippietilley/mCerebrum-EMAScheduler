package org.md2k.ema_scheduler.runner;

import android.content.Context;

import org.md2k.ema_scheduler.configuration.Application;
import org.md2k.ema_scheduler.configuration.EMAType;
import org.md2k.ema_scheduler.delivery.Callback;
import org.md2k.utilities.Report.Log;

/**
 * Created by monowar on 3/10/16.
 */
public class RunnerManager {
    private static final String TAG = RunnerManager.class.getSimpleName();
    Context context;
    RunnerMonitor runnerMonitor;
    Application application;

    public RunnerManager(Context context, Callback callback) {
        this.context = context;
        runnerMonitor=new RunnerMonitor(context, callback);
    }
    public void set(Application application){
        this.application=application;
    }

    public void start(EMAType emaType, String status, String type) {
        Log.d(TAG, "start()...status=" + status + " filename=" + application.getId());
            runnerMonitor.start(emaType, status, application, type);
    }
    public void stop(){
        runnerMonitor.clear();
    }
}

