package org.md2k.ema_scheduler.runner;

import android.content.Context;
import android.content.Intent;

import org.md2k.ema_scheduler.configuration.ConfigurationApplication;

/**
 * Created by monowar on 3/10/16.
 */
public class RunnerManager {
    private static RunnerManager instance = null;
    Context context;

    public static RunnerManager getInstance(Context context) {
        if (instance == null)
            instance = new RunnerManager(context);
        return instance;
    }

    private RunnerManager(Context context) {
        this.context = context;
    }

    public void start(ConfigurationApplication configurationApplication) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(configurationApplication.getPackage_name());
        intent.setAction(configurationApplication.getPackage_name());
        intent.putExtra("file_name", configurationApplication.getFile_name());
        intent.putExtra("id", "id");
        intent.putExtra("name", "name");
        intent.putExtra("display_name", "display_name");
        intent.putExtra("timeout", 1000000L);
        //Todo: Set TimeOut
        //intent.putExtra("timeout", notificationConfig.getTimeout().getCompletion_timeout());
        context.startActivity(intent);
    }
}
