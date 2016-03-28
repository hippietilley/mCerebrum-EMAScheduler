package org.md2k.ema_scheduler.delivery;

import android.content.Context;

import org.md2k.datakitapi.time.DateTime;
import org.md2k.ema_scheduler.configuration.EMAType;
import org.md2k.ema_scheduler.logger.LogInfo;
import org.md2k.ema_scheduler.logger.LoggerManager;
import org.md2k.ema_scheduler.notifier.NotifierManager;
import org.md2k.ema_scheduler.runner.RunnerManager;
import org.md2k.utilities.Report.Log;
import org.md2k.utilities.data_format.NotificationAcknowledge;

/**
 * Created by monowar on 3/10/16.
 */
public class DeliveryManager {
    private static final String TAG = DeliveryManager.class.getSimpleName();
    private static DeliveryManager instance=null;
    Context context;
    NotifierManager notifierManager;
    RunnerManager runnerManager;
    private DeliveryManager(Context context) {
        this.context = context;
        runnerManager = new RunnerManager(context);
        notifierManager=new NotifierManager(context);
    }

    public static DeliveryManager getInstance(Context context){
        if(instance==null) instance=new DeliveryManager(context);
        return instance;
    }

    public void start(final EMAType emaType, boolean isNotifyRequired, final String type){
        Log.d(TAG, "start()...emaType=" + emaType.getType() + " id=" + emaType.getId());
        log(emaType,type);
        runnerManager.set(emaType.getApplication());
        Log.d(TAG,"runner="+runnerManager);
        notifierManager.set(emaType, new Callback() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "callback received...response="+response);
                switch (response) {
                    case NotificationAcknowledge.OK:
                    case NotificationAcknowledge.CANCEL:
                    case NotificationAcknowledge.TIMEOUT:
                    case NotificationAcknowledge.DELAY_CANCEL:
                        Log.d(TAG, "matched...runner=" + runnerManager+" response="+response);
                        notifierManager.stop();
                        runnerManager.start(response,type);
                        notifierManager.clear();
                        break;
                }
            }
        });
        if(isNotifyRequired){
            notifierManager.start();
        }else{
            runnerManager.start(NotificationAcknowledge.OK, type);
        }
    }
    protected void log(EMAType emaType, String type){
        if(type.equals("SYSTEM")) {
            LogInfo logInfo = new LogInfo();
            logInfo.setOperation(LogInfo.OP_DELIVER);
            logInfo.setId(emaType.getId());
            logInfo.setType(emaType.getType());
            logInfo.setTimestamp(DateTime.getDateTime());
            logInfo.setMessage("trying to deliver...");
            LoggerManager.getInstance(context).insert(logInfo);
        }
    }

    public void stop() {
        Log.d(TAG, "stop()...");
        if (runnerManager != null)
            runnerManager.stop();
        if (notifierManager != null) {
            notifierManager.stop();
            notifierManager.clear();
        }
    }
}
