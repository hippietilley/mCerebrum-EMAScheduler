package org.md2k.ema_scheduler.scheduler;

import android.content.Context;
import android.os.Handler;

import com.google.gson.Gson;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.datatype.DataTypeString;
import org.md2k.datakitapi.messagehandler.OnReceiveListener;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.ema_scheduler.configuration.EMAType;
import org.md2k.ema_scheduler.day.DayManager;
import org.md2k.utilities.Report.Log;
import org.md2k.utilities.data_format.Event;

import java.util.ArrayList;

/**
 * Created by monowar on 3/14/16.
 */
public class SmokingEMAScheduler extends Scheduler {
    private static final String TAG = SmokingEMAScheduler.class.getSimpleName();
    DataKitAPI dataKitAPI;
    Handler handler;
    ArrayList<DataSourceClient> dataSourceClients;
    Runnable runnableListenEvent = new Runnable() {
        @Override
        public void run() {
            dataSourceClients = dataKitAPI.find(createDataSourceBuilderEvent());
            Log.d(TAG, "runnableListenEvent()...dataSourceClients.size()="+dataSourceClients.size());

            if (dataSourceClients.size() == 0)
                handler.postDelayed(runnableListenEvent, 1000);
            else {
                subscribeEvent();
            }
        }
    };

    public SmokingEMAScheduler(Context context, EMAType emaType, DayManager dayManager) {
        super(context, emaType, dayManager);
        Log.d(TAG, "EventEMAScheduler()...");
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
        if(dataSourceClients!=null){
            for(int i=0;i<dataSourceClients.size();i++)
                dataKitAPI.unsubscribe(dataSourceClients.get(i));
        }

    }

    DataSourceBuilder createDataSourceBuilderEvent() {
        return new DataSourceBuilder().setType(DataSourceType.EVENT);
    }

    public void subscribeEvent() {
        Log.d(TAG, "subscribeEvent()...");
        for (int i = 0; i < dataSourceClients.size(); i++) {
            dataKitAPI.subscribe(dataSourceClients.get(i), new OnReceiveListener() {
                @Override
                public void onReceived(DataType dataType) {
                    Gson gson = new Gson();
                    DataTypeString dataTypeString = (DataTypeString) dataType;
                    Event event = gson.fromJson(dataTypeString.getSample(), Event.class);
                    Log.d(TAG, "receivedEventData()...");
                    if (event.getEvent().equals(Event.SMOKING))
                        if(conditionManager.isValid(emaType.getScheduler_rules()[0].getConditions())){
                            Log.d(TAG, "condition valid...");
                            startDelivery();
                            Log.d(TAG,"after delivery...");
                        }
                }
            });
        }
    }
    public void reset(){

    }
}
