package org.md2k.ema_scheduler.delivery;

import android.content.Context;

import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.ema_scheduler.configuration.Configuration;
import org.md2k.ema_scheduler.configuration.EMAType;
import org.md2k.ema_scheduler.logger.LogInfo;
import org.md2k.ema_scheduler.logger.LoggerManager;
import org.md2k.ema_scheduler.notifier.NotifierManager;
import org.md2k.ema_scheduler.runner.RunnerManager;
import org.md2k.utilities.Report.Log;
import org.md2k.utilities.data_format.NotificationAcknowledge;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by monowar on 3/10/16.
 */
public class DeliveryManager {
    private static final String TAG = DeliveryManager.class.getSimpleName();
    private static DeliveryManager instance=null;
    Context context;
    NotifierManager notifierManager;
    RunnerManager runnerManager;
    boolean isRunning;

    private DeliveryManager(Context context) throws DataKitException {
        this.context = context;
        runnerManager = new RunnerManager(context, new Callback() {
            @Override
            public void onResponse(String response) {
                isRunning=false;
            }
        });
        notifierManager=new NotifierManager(context);
        isRunning=false;
    }

    public static DeliveryManager getInstance(Context context) throws DataKitException {
        if(instance==null) instance=new DeliveryManager(context);
        return instance;
    }
    EMAType findEMIType(){
        EMAType[] emaTypes=Configuration.getInstance().getEma_types();
        ArrayList<EMAType> emis=new ArrayList<>();
        for(int i=0;i<emaTypes.length;i++) {
            if(!emaTypes[i].getType().equals("EMI"))
                continue;
            if(emaTypes[i].getId().equals("EMI")) continue;
            emis.add(emaTypes[i]);
        }
        if(emis.size()==0) return null;
        Random random=new Random();
        return emis.get(random.nextInt(emis.size()));
    }

    public boolean start(EMAType emaType, boolean isNotifyRequired, final String type) throws DataKitException {
        if(isRunning){
            log(LogInfo.STATUS_DELIVER_ALREADY_RUNNING, emaType,"Not started..another one is running");
            return false;
        }
        Log.d(TAG, "start()...emaType=" + emaType.getType() + " id=" + emaType.getId());
        log(LogInfo.STATUS_DELIVER_SUCCESS, emaType,type);
        if(emaType.getId().equals("EMI")){
            emaType=findEMIType();
            logRandom(emaType,type);
            if(emaType==null) return false;
        }
        runnerManager.set(emaType.getApplication());
        Log.d(TAG,"runner="+runnerManager);
        final EMAType finalEmaType = emaType;
        notifierManager.set(emaType, new Callback() {
            @Override
            public void onResponse(String response) throws DataKitException {
                Log.d(TAG, "callback received...response=" + response);
                switch (response) {
                    case NotificationAcknowledge.OK:
                    case NotificationAcknowledge.CANCEL:
                    case NotificationAcknowledge.TIMEOUT:
                    case NotificationAcknowledge.DELAY_CANCEL:
                        Log.d(TAG, "matched...runner=" + runnerManager + " response=" + response);
                        notifierManager.stop();
                        runnerManager.start(finalEmaType, response, type);
                        notifierManager.clear();
                        break;
                }
            }
        });
        isRunning=true;
        if(isNotifyRequired){
            notifierManager.start();
        }else{
            runnerManager.start(emaType, NotificationAcknowledge.OK, type);
        }
        return true;
    }
    protected void log(String status, EMAType emaType, String type) throws DataKitException {
        if(type.equals("SYSTEM")) {
            LogInfo logInfo = new LogInfo();
            logInfo.setOperation(LogInfo.OP_DELIVER);
            logInfo.setId(emaType.getId());
            logInfo.setType(emaType.getType());
            logInfo.setTimestamp(DateTime.getDateTime());
            logInfo.setStatus(status);
            logInfo.setMessage("trying to deliver...");
            LoggerManager.getInstance(context).insert(logInfo);
        }
    }
    protected void logRandom(EMAType emaType, String type) throws DataKitException {
        if(type.equals("SYSTEM")) {
            LogInfo logInfo = new LogInfo();
            logInfo.setOperation(LogInfo.OP_DELIVER);
            logInfo.setId(emaType.getId());
            logInfo.setType(emaType.getType());
            logInfo.setTimestamp(DateTime.getDateTime());
            logInfo.setMessage("EMI randomly selected. trying to deliver...");
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
        isRunning=false;
    }
    public static void clear(){
        instance=null;
    }
}
