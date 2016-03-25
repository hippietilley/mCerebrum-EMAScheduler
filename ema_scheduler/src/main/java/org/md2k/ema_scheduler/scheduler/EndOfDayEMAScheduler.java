package org.md2k.ema_scheduler.scheduler;

import android.content.Context;
import android.os.Handler;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.messagehandler.OnReceiveListener;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.ema_scheduler.condition.ConditionManager;
import org.md2k.ema_scheduler.configuration.EMAType;
import org.md2k.ema_scheduler.day.DayManager;
import org.md2k.utilities.Report.Log;

import java.util.ArrayList;

/**
 * Created by monowar on 3/14/16.
 */
public class EndOfDayEMAScheduler extends Scheduler {
    private static final String TAG = EndOfDayEMAScheduler.class.getSimpleName();
    DataKitAPI dataKitAPI;
    Handler handler;
    Runnable runnableListenEvent = new Runnable() {
        @Override
        public void run() {
            ArrayList<DataSourceClient> dataSourceClients = dataKitAPI.find(new DataSourceBuilder().setType(DataSourceType.DAY_END));
            Log.d(TAG, "runnableListenEndOfDay()...dataSourceClients.size()="+dataSourceClients.size());

            if (dataSourceClients.size() == 0)
                handler.postDelayed(runnableListenEvent, 1000);
            else {
                subscribeEvent(dataSourceClients);
            }
        }
    };

    public EndOfDayEMAScheduler(Context context, EMAType emaType, DayManager dayManager) {
        super(context, emaType, dayManager);
        Log.d(TAG, "EndOfDayEMAScheduler()...");
        conditionManager = new ConditionManager();
        handler = new Handler();
    }

    @Override
    public void start() {
        super.start();
        Log.d(TAG, "start()...");
        dataKitAPI = DataKitAPI.getInstance(context);
        handler.post(runnableListenEvent);
    }

    @Override
    public void stop() {
        Log.d(TAG, "stop()...");
        handler.removeCallbacks(runnableListenEvent);

    }

    public void subscribeEvent(ArrayList<DataSourceClient> dataSourceClients) {
        Log.d(TAG, "subscribeEndOfDay()...");
        for (int i = 0; i < dataSourceClients.size(); i++) {
            dataKitAPI.subscribe(dataSourceClients.get(i), new OnReceiveListener() {
                @Override
                public void onReceived(DataType dataType) {
                        if(conditionManager.isValid()){
                            Log.d(TAG, "condition valid...");
                            startDelivery();
                        }
                }
            });
        }
    }
    @Override
    public void reset(){

    }
}
