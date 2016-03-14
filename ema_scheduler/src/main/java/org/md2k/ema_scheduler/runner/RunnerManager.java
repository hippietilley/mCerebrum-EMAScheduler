package org.md2k.ema_scheduler.runner;

import android.content.Context;
import android.content.Intent;

import org.md2k.ema_scheduler.configuration.Application;
import org.md2k.ema_scheduler.configuration.Notification;
import org.md2k.utilities.data_format.NotificationAcknowledge;

/**
 * Created by monowar on 3/10/16.
 */
public class RunnerManager {
    Context context;
    RunnerMonitor runnerMonitor;

    public RunnerManager(Context context) {
        this.context = context;
        runnerMonitor=new RunnerMonitor(context);
    }

    public void start(Application application, String status) {
            runnerMonitor.start(application.getTimeout());
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(application.getPackage_name());
            intent.setAction(application.getPackage_name());
            intent.putExtra("file_name", application.getFile_name());
            intent.putExtra("id", application.getId());
            intent.putExtra("name", application.getName());
            intent.putExtra("timeout", application.getTimeout());
            context.startActivity(intent);
    }
    public void stop(){
        runnerMonitor.clear();
    }
}

