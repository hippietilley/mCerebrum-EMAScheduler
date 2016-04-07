package org.md2k.ema_scheduler.scheduler;

import android.content.Context;
import android.os.Handler;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.datatype.DataTypeDouble;
import org.md2k.datakitapi.messagehandler.OnReceiveListener;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.ema_scheduler.condition.ConditionManager;
import org.md2k.ema_scheduler.configuration.EMAType;
import org.md2k.ema_scheduler.logger.LogInfo;
import org.md2k.utilities.Report.Log;

import java.util.ArrayList;

/**
 * Created by monowar on 3/14/16.
 */
public class StressEMAScheduler extends Scheduler {
    private static final String TAG = StressEMAScheduler.class.getSimpleName();
    Handler handler;
    DataSourceClient dataSourceClient;
    boolean isRunning;

    public StressEMAScheduler(Context context, EMAType emaType) {
        super(context, emaType);
        Log.d(TAG, "SmokingEMAScheduler()...id=" + emaType.getId());
        handler = new Handler();
    }

    @Override
    public void start(long dayStartTimestamp, long dayEndTimestamp) {
        super.start(dayStartTimestamp, dayEndTimestamp);
        Log.d(TAG, "start()...");
        handler.removeCallbacks(runnableListenEvent);
        handler.post(runnableListenEvent);
    }

    @Override
    public void stop() {
        Log.d(TAG, "stop()...");
        handler.removeCallbacks(runnableListenEvent);
        unsubscribeEvent();
    }

    @Override
    public void setDayStartTimestamp(long dayStartTimestamp) {

    }

    @Override
    public void setDayEndTimestamp(long dayEndTimestamp) {
    }

    Runnable runnableListenEvent = new Runnable() {
        @Override
        public void run() {
            DataKitAPI dataKitAPI=DataKitAPI.getInstance(context);
            Log.d(TAG, "runnableEventEMA()...id=" + emaType.getId() + " find..." + emaType.getScheduler_rules()[0].getData_source().getType());
            ArrayList<DataSourceClient> dataSourceClients = dataKitAPI.find(new DataSourceBuilder(emaType.getScheduler_rules()[0].getData_source()));
            Log.d(TAG, "runnableEventEMA()...id=" + emaType.getId() + " find..." + dataSourceClients.size() + "...done");
            if (dataSourceClients.size() == 0)
                handler.postDelayed(runnableListenEvent, 1000);
            else {
                dataSourceClient = dataSourceClients.get(0);
                subscribeEvent();
            }
        }
    };

    public void subscribeEvent() {
        Log.d(TAG, "subscribeEvent()...");
        isRunning = false;
        DataKitAPI.getInstance(context).subscribe(dataSourceClient, new OnReceiveListener() {
            @Override
            public void onReceived(DataType dataType) {
                double sample = ((DataTypeDouble) dataType).getSample();
                if(sample!=2) return;
                Thread t=new Thread(new Runnable() {
                    @Override
                    public void run() {
                        sendToLogInfo(LogInfo.STATUS_SCHEDULER_SCHEDULED, DateTime.getDateTime());
                        conditionManager = ConditionManager.getInstance(context);
                        if (conditionManager.isValid(emaType.getScheduler_rules()[0].getConditions(), emaType.getType(), emaType.getId())) {
                            Log.d(TAG, "condition valid...");
                            isRunning = false;
                            startDelivery();
                        }
                    }
                });
                t.start();
            }
        });
    }

    public void unsubscribeEvent() {
        try {
            if (dataSourceClient != null)
                DataKitAPI.getInstance(context).unsubscribe(dataSourceClient);
        }catch (Exception e){

        }
    }
}
