package org.md2k.ema_scheduler.delivery;

import android.content.Context;

import org.md2k.ema_scheduler.configuration.EMAType;
import org.md2k.ema_scheduler.notifier.NotifierManager;
import org.md2k.ema_scheduler.runner.RunnerManager;
import org.md2k.utilities.Report.Log;
import org.md2k.utilities.data_format.NotificationAcknowledge;

/**
 * Created by monowar on 3/10/16.
 */
public class DeliveryManager {
    private static final String TAG = DeliveryManager.class.getSimpleName();
    Context context;
    NotifierManager notifierManager;
    RunnerManager runnerManager;

    public DeliveryManager(Context context) {
        this.context = context;
        runnerManager = new RunnerManager(context);
    }

    public void start(final EMAType emaType) {
        notifierManager = new NotifierManager(context, emaType.getNotifications(), new Callback() {
            @Override
            public void onResponse(String response) {
                switch (response) {
                    case NotificationAcknowledge.OK:
                    case NotificationAcknowledge.CANCEL:
                    case NotificationAcknowledge.TIMEOUT:
                        runnerManager.start(emaType.getApplication(), response);
                        break;
                }
            }
        });
        notifierManager.start();
    }

    public void stop() {
        if (runnerManager != null)
            runnerManager.stop();
        if (notifierManager != null) {
            notifierManager.stop();
            notifierManager.clear();
        }
        Log.d(TAG, "stop()");
    }
}
