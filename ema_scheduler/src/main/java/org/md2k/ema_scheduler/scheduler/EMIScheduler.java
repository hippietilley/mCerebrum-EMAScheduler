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
import org.md2k.datakitapi.source.datasource.DataSourceId;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.ema_scheduler.ServiceEMAScheduler;
import org.md2k.ema_scheduler.condition.ConditionManager;
import org.md2k.ema_scheduler.configuration.EMAType;
import org.md2k.ema_scheduler.delivery.DeliveryManager;
import org.md2k.ema_scheduler.logger.LogInfo;
import org.md2k.ema_scheduler.scheduler.emi.ProbabilityEMI;
import org.md2k.utilities.Report.Log;
import org.md2k.utilities.data_format.DayTypeInfo;

import java.util.ArrayList;

/**
 * Copyright (c) 2016, The University of Memphis, MD2K Center
 * - Syed Monowar Hossain <monowar.hossain@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
public class EMIScheduler extends Scheduler {
    private static final String TAG = EMIScheduler.class.getSimpleName();
    private Handler handler;
    private boolean isPreQuit;
    private boolean isStress;
    private boolean isPreLapse;
    private DataSourceClient dataSourceClient;
    private Runnable runnableStressClassification = new Runnable() {
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
                Log.w(TAG,"DataKitException...runnableStressClassification...");
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ServiceEMAScheduler.BROADCAST_MSG));
            }
        }
    };

    public EMIScheduler(Context context, EMAType emaType, DeliveryManager deliveryManager) throws DataKitException {
        super(context, emaType, deliveryManager);
        Log.d(TAG, "EMIScheduler()...");
        handler = new Handler();
        isPreQuit = true;
        isPreLapse = true;
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
        stopDelivery();
        Log.d(TAG, "stop()...");
    }

    @Override
    public void setDayStartTimestamp(long dayStartTimestamp) {
        this.dayStartTimestamp = dayStartTimestamp;
    }

    @Override
    public void setDayEndTimestamp(long dayEndTimestamp) {
        this.dayEndTimestamp = dayEndTimestamp;
    }

    private void readTypeOfDay() throws DataKitException {
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
                else {
                    isPreQuit = false;
                    isPreLapse = true;
                    long startTime = dataTypeJSONObject.getDateTime();
                    DataSourceBuilder dataSourceBuilder1 = new DataSourceBuilder().setType(DataSourceType.SMOKING).setId(DataSourceId.SELF_REPORT);
                    ArrayList<DataSourceClient> dataSourceClientArrayList1 = dataKitAPI.find(dataSourceBuilder1);
                    if (dataSourceClientArrayList1.size() != 0) {
                        ArrayList<DataType> dataTypess = dataKitAPI.query(dataSourceClientArrayList1.get(0), startTime, DateTime.getDateTime());
                        if(dataTypess.size()>0)
                            isPreLapse = false;
                    }
                }
            }
        }
    }

    private void prepareAndDeliver(DataType dataType) throws DataKitException {
        if (!isValidDay()) return;
        double sample = ((DataTypeDouble) dataType).getSample();
        if (!(sample == 0 || sample == 2)) return;
        readTypeOfDay();
        if (isPreQuit) return;

        sendToLogInfo(LogInfo.STATUS_SCHEDULER_SCHEDULED, DateTime.getDateTime());
        isStress = (sample != 0);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                conditionManager = ConditionManager.getInstance(context);
                try {
                    if (conditionManager.isValid(emaType.getScheduler_rules()[0].getConditions(), emaType.getType(), emaType.getId()))
                        deliverIfProbability();
                } catch (DataKitException e) {
                    Log.w(TAG, "DataKitException...deliverIfProbability()");
                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ServiceEMAScheduler.BROADCAST_MSG));

                }
            }
        });
        t.start();
    }

    private void subscribeStress() throws DataKitException {
        DataKitAPI dataKitAPI = DataKitAPI.getInstance(context);
        Log.d(TAG, "subscribeDayStart()...");
        dataKitAPI.subscribe(dataSourceClient, new OnReceiveListener() {
            @Override
            public void onReceived(final DataType dataType) {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            prepareAndDeliver(dataType);
                        } catch (DataKitException e) {
                            Log.w(TAG, "DataKitException...prepareAndDeliver...");
                            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ServiceEMAScheduler.BROADCAST_MSG));
                        }
                    }
                });
                t.start();
            }
        });
    }

    private void deliverIfProbability() throws DataKitException {
        ProbabilityEMI probabilityEMI = new ProbabilityEMI(context, dayStartTimestamp, isPreLapse, isStress, emaType.getType(), emaType.getId());
        if (probabilityEMI.isTrigger())
            startDelivery();
    }

    private boolean isValidDay() {
        if (dayStartTimestamp <= 0) return false;
        if (dayStartTimestamp < dayEndTimestamp) return false;
        return dayStartTimestamp + 12 * 60 * 60 * 1000 >= DateTime.getDateTime();
    }

    private void unsubscribeEvent() {
        try {
            if (dataSourceClient != null)
                DataKitAPI.getInstance(context).unsubscribe(dataSourceClient);
        } catch (Exception ignored) {

        }
    }

}
