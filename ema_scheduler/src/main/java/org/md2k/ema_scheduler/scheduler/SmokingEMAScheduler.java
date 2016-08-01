package org.md2k.ema_scheduler.scheduler;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.messagehandler.OnReceiveListener;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.ema_scheduler.ServiceEMAScheduler;
import org.md2k.ema_scheduler.condition.ConditionManager;
import org.md2k.ema_scheduler.configuration.EMAType;
import org.md2k.ema_scheduler.delivery.DeliveryManager;
import org.md2k.ema_scheduler.logger.LogInfo;
import org.md2k.utilities.Report.Log;

import java.util.ArrayList;

/**
 * Created by monowar on 3/14/16.
 */
public class SmokingEMAScheduler extends Scheduler {
    private static final String TAG = SmokingEMAScheduler.class.getSimpleName();
    Handler handler;
    DataSourceClient dataSourceClient;
    Runnable runnableListenEvent = new Runnable() {
        @Override
        public void run() {
            DataKitAPI dataKitAPI = DataKitAPI.getInstance(context);
            Log.d(TAG, "runnableEventEMA()...id=" + emaType.getId() + " find..." + emaType.getScheduler_rules()[0].getData_source().getType());
            ArrayList<DataSourceClient> dataSourceClients = null;
            try {
                dataSourceClients = dataKitAPI.find(new DataSourceBuilder(emaType.getScheduler_rules()[0].getData_source()));
                Log.d(TAG, "runnableEventEMA()...id=" + emaType.getId() + " find..." + dataSourceClients.size() + "...done");
                if (dataSourceClients.size() == 0)
                    handler.postDelayed(runnableListenEvent, 1000);
                else {
                    dataSourceClient = dataSourceClients.get(0);
                    subscribeEvent();
                }
            } catch (DataKitException e) {
                Log.d(TAG, "DataKitException...subscribeEvent");
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ServiceEMAScheduler.BROADCAST_MSG));
            }
        }
    };

    public SmokingEMAScheduler(Context context, EMAType emaType, DeliveryManager deliveryManager) throws DataKitException {
        super(context, emaType, deliveryManager);
        Log.d(TAG, "SmokingEMAScheduler()...id=" + emaType.getId());
        handler = new Handler();
    }

    @Override
    public void start(long dayStartTimestamp, long dayEndTimestamp) throws DataKitException {
        super.start(dayStartTimestamp, dayEndTimestamp);
        Log.d(TAG, "start()...");
        handler.removeCallbacks(runnableListenEvent);
        handler.post(runnableListenEvent);
    }

    @Override
    public void stop() {
        Log.d(TAG, "stop()...");
        stopDelivery();
        handler.removeCallbacks(runnableListenEvent);
        unsubscribeEvent();
    }

    @Override
    public void setDayStartTimestamp(long dayStartTimestamp) {
        this.dayStartTimestamp = dayStartTimestamp;

    }

    @Override
    public void setDayEndTimestamp(long dayEndTimestamp) {
        this.dayEndTimestamp = dayEndTimestamp;
    }

    public void subscribeEvent() throws DataKitException {
        Log.d(TAG, "subscribeEvent()...");
        DataKitAPI.getInstance(context).subscribe(dataSourceClient, new OnReceiveListener() {
            @Override
            public void onReceived(final DataType dataType) {
                Thread t=new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            sendToLogInfo(LogInfo.STATUS_SCHEDULER_SCHEDULED, DateTime.getDateTime());
                            conditionManager = ConditionManager.getInstance(context);
                            if (conditionManager.isValid(emaType.getScheduler_rules()[0].getConditions(), emaType.getType(), emaType.getId())) {
                                Log.d(TAG, "condition valid...");
                                startDelivery();
                            }
                        } catch (DataKitException e) {
                            Log.d(TAG,"DataKitException...startDelivery");
                            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ServiceEMAScheduler.BROADCAST_MSG));
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
