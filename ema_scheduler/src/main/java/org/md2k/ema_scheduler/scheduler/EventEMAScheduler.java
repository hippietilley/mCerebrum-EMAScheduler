package org.md2k.ema_scheduler.scheduler;

import android.content.Context;
import android.os.Handler;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.messagehandler.OnReceiveListener;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.ema_scheduler.condition.ConditionManager;
import org.md2k.ema_scheduler.configuration.EMAType;
import org.md2k.ema_scheduler.day.DayManager;
import org.md2k.utilities.Report.Log;

import java.util.ArrayList;

/**
 * Created by monowar on 3/14/16.
 */
public class EventEMAScheduler extends Scheduler {
    private static final String TAG = EventEMAScheduler.class.getSimpleName();
    DataKitAPI dataKitAPI;
    Handler handler;
    DataSourceClient dataSourceClient;
    boolean isRunning;

    public EventEMAScheduler(Context context, EMAType emaType, DayManager dayManager) {
        super(context, emaType, dayManager);
        Log.d(TAG, "EventEMAScheduler()...id=" + emaType.getId());
        handler = new Handler();
    }

    @Override
    public void start() {
        super.start();
        Log.d(TAG, "start()...");
        dataKitAPI = DataKitAPI.getInstance(context);
        ArrayList<DataSourceClient> dataSourceClients = dataKitAPI.find(new DataSourceBuilder(emaType.getScheduler_rules()[0].getData_source()));
        dataSourceClient = dataSourceClients.get(0);
        handler.post(runnableListenEvent);
    }

    @Override
    public void stop() {
        Log.d(TAG, "stop()...");
        handler.removeCallbacks(runnableListenEvent);

    }

    Runnable runnableListenEvent = new Runnable() {
        @Override
        public void run() {
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
                Log.d(TAG, "onReceived...");
                if(!isRunning)
                handler.post(runnableDeliver);
            }
        });
    }
    Runnable runnableDeliver=new Runnable() {
        @Override
        public void run() {
            conditionManager = ConditionManager.getInstance(context);
            if (conditionManager.isValid(emaType.getScheduler_rules()[0].getConditions())) {
                Log.d(TAG, "condition valid...");
                isRunning=false;
                startDelivery();
            }
        }
    };

    @Override
    public void reset() {

    }
}
