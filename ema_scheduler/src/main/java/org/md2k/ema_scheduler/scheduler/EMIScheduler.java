package org.md2k.ema_scheduler.scheduler;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import com.google.gson.Gson;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.datatype.DataTypeDouble;
import org.md2k.datakitapi.datatype.DataTypeJSONObject;
import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.messagehandler.OnReceiveListener;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.ema_scheduler.ServiceEMAScheduler;
import org.md2k.ema_scheduler.condition.ConditionManager;
import org.md2k.ema_scheduler.configuration.EMAType;
import org.md2k.ema_scheduler.logger.LogInfo;
import org.md2k.ema_scheduler.scheduler.emi.ProbabilityEMI;
import org.md2k.utilities.Report.Log;
import org.md2k.utilities.data_format.DayTypeInfo;

import java.util.ArrayList;

/**
 * Created by monowar on 3/14/16.
 */
public class EMIScheduler extends Scheduler {
    private static final String TAG = EMIScheduler.class.getSimpleName();
    Handler handler;
    boolean isPreQuit;
    boolean isStress;
    DataSourceClient dataSourceClient;

    public EMIScheduler(Context context, EMAType emaType) throws DataKitException {
        super(context, emaType);
        Log.d(TAG, "EMIScheduler()...");
        handler = new Handler();
        isPreQuit = true;
    }

    @Override
    public void start(long dayStartTimestamp, long dayEndTimestamp) throws DataKitException {
        Log.d(TAG, "start()...");
        super.start(dayStartTimestamp, dayEndTimestamp);
        handler.removeCallbacks(runnableStressClassification);
        handler.post(runnableStressClassification);
    }

    @Override
    public void stop() {
        handler.removeCallbacks(runnableStressClassification);
        unsubscribeEvent();
        Log.d(TAG, "stop()...");
    }

    @Override
    public void setDayStartTimestamp(long dayStartTimestamp) {

    }

    @Override
    public void setDayEndTimestamp(long dayEndTimestamp) {

    }

    void readTypeOfDay() throws DataKitException {
        DataKitAPI dataKitAPI = DataKitAPI.getInstance(context);
        DataSourceBuilder dataSourceBuilder = new DataSourceBuilder().setType(DataSourceType.TYPE_OF_DAY);
        DayTypeInfo dayTypeInfo;
        ArrayList<DataSourceClient> dataSourceClientArrayList = dataKitAPI.find(dataSourceBuilder);
        if (dataSourceClientArrayList.size() != 0) {
            ArrayList<DataType> dataTypes = dataKitAPI.query(dataSourceClientArrayList.get(0), 1);
            if (dataTypes.size() == 0) {
                isPreQuit = true;
            } else {
                DataTypeJSONObject dataTypeJSONObject = (DataTypeJSONObject) dataTypes.get(0);
                Gson gson = new Gson();
                dayTypeInfo = gson.fromJson(dataTypeJSONObject.getSample().toString(), DayTypeInfo.class);
                if (dayTypeInfo.getDay_type() == DayTypeInfo.PRE_QUIT_INT)
                    isPreQuit = true;
                else isPreQuit = false;
            }
        }
    }

    Runnable runnableStressClassification = new Runnable() {
        @Override
        public void run() {
            DataKitAPI dataKitAPI = DataKitAPI.getInstance(context);
            ArrayList<DataSourceClient> dataSourceClients = null;
            try {
                dataSourceClients = dataKitAPI.find(new DataSourceBuilder().setType(DataSourceType.ORG_MD2K_CSTRESS_STRESS_EPISODE_CLASSIFICATION));
                Log.d(TAG, "runnableListenDayStart()...dataSourceClients.size()=" + dataSourceClients.size());
                if (dataSourceClients.size() == 0)
                    handler.postDelayed(runnableStressClassification, 60000);
                else {
                    dataSourceClient = dataSourceClients.get(0);
                    subscribeStress();
                }
            } catch (DataKitException e) {
                e.printStackTrace();
            }
        }
    };

    public void prepareAndDeliver(DataType dataType) throws DataKitException {
        if (!isValidDay()) return;
        double sample = ((DataTypeDouble) dataType).getSample();
        if (!(sample == 0 || sample == 2)) return;
        sendToLogInfo(LogInfo.STATUS_SCHEDULER_SCHEDULED, DateTime.getDateTime());
        if (sample == 0)
            isStress = false;
        else isStress = true;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                conditionManager = ConditionManager.getInstance(context);
                try {
                    if (conditionManager.isValid(emaType.getScheduler_rules()[0].getConditions(), emaType.getType(), emaType.getId()))
                        try {
                            deliverIfProbability();
                        } catch (DataKitException e) {
                            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ServiceEMAScheduler.class.getSimpleName()));
                        }
                } catch (DataKitException e) {
                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ServiceEMAScheduler.class.getSimpleName()));

                }
            }
        });
        t.start();
    }

    public void subscribeStress() throws DataKitException {
        DataKitAPI dataKitAPI = DataKitAPI.getInstance(context);
        Log.d(TAG, "subscribeDayStart()...");
        dataKitAPI.subscribe(dataSourceClient, new OnReceiveListener() {
            @Override
            public void onReceived(DataType dataType) {
                try {
                    prepareAndDeliver(dataType);
                } catch (DataKitException e) {
                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ServiceEMAScheduler.class.getSimpleName()));

                }
            }
        });
    }

    void deliverIfProbability() throws DataKitException {
        readTypeOfDay();
        ProbabilityEMI probabilityEMI = new ProbabilityEMI(context, dayStartTimestamp, isPreQuit, isStress, emaType.getType(), emaType.getId());
        if (probabilityEMI.isTrigger())
            startDelivery();
    }

    public boolean isValidDay() {
        if (dayStartTimestamp == -1) return false;
        if (dayStartTimestamp < dayEndTimestamp) return false;
        if (dayStartTimestamp + 12 * 60 * 60 * 1000 < DateTime.getDateTime()) return false;
        return true;
    }

    public void unsubscribeEvent() {
        try {
            if (dataSourceClient != null)
                DataKitAPI.getInstance(context).unsubscribe(dataSourceClient);
        } catch (Exception e) {

        }
    }

}
