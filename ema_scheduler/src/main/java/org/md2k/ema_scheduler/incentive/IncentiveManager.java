package org.md2k.ema_scheduler.incentive;

import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.datatype.DataTypeJSONObject;
import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.source.METADATA;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.source.platform.Platform;
import org.md2k.datakitapi.source.platform.PlatformBuilder;
import org.md2k.datakitapi.source.platform.PlatformType;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.ema_scheduler.condition.ConditionManager;
import org.md2k.ema_scheduler.configuration.EMAType;
import org.md2k.ema_scheduler.configuration.IncentiveRule;
import org.md2k.ema_scheduler.logger.LogInfo;
import org.md2k.utilities.Report.Log;
import org.md2k.utilities.data_format.notification.NotificationResponse;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Copyright (c) 2016, The University of Memphis, MD2K Center
 * - Syed Monowar Hossain <monowar.hossain@gmail.com>
 * All rights reserved.
 * <p/>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p/>
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * <p/>
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p/>
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
public class IncentiveManager {
    private static final String TAG = IncentiveManager.class.getSimpleName();
    protected ConditionManager conditionManager;
    Context context;
    DataSourceClient dataSourceClient;

    public IncentiveManager(Context context) throws DataKitException {
        this.context = context;
        conditionManager = ConditionManager.getInstance(context);
        register();
    }

    public void start(final EMAType emaType, final String notificationResponse, final String completionResponse) throws DataKitException {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "ematype=" + emaType.getId() + " noti=" + notificationResponse + " com=" + completionResponse);
                    if (notificationResponse.equals(NotificationResponse.CANCEL) || notificationResponse.equals(NotificationResponse.TIMEOUT))
                        return;
                    if (emaType.getIncentive_rules() == null) return;
                    if (completionResponse != null && completionResponse.equals(LogInfo.STATUS_RUN_COMPLETED)) {
                        IncentiveRule incentiveRule = getIncentiveRule(emaType);
                        Incentive incentive = saveIncentiveToDataKit(emaType, incentiveRule);
                        show(incentiveRule, incentive);
                    }
                } catch (DataKitException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    private IncentiveRule getIncentiveRule(EMAType emaType) throws DataKitException {
        for (int i = 0; i < emaType.getIncentive_rules().length; i++) {
            if (conditionManager.isValid(emaType.getIncentive_rules()[i].getConditions(), emaType.getType(), emaType.getId())) {
                return emaType.getIncentive_rules()[i];
            }
        }
        return null;
    }

    public double getLastTotalIncentive() throws DataKitException {
        Gson gson = new Gson();
        ArrayList<DataType> dataTypes = DataKitAPI.getInstance(context).query(dataSourceClient, 1);
        if (dataTypes.size() == 0) return 0;
        DataTypeJSONObject dataTypeJSONObject = (DataTypeJSONObject) dataTypes.get(0);
        Incentive incentive = gson.fromJson(dataTypeJSONObject.getSample().toString(), Incentive.class);
        return incentive.getTotalIncentive();
    }

    public Incentive saveIncentiveToDataKit(EMAType emaType, IncentiveRule incentiveRule) throws DataKitException {
        Log.d(TAG, "IncentiveManager...saveIncentiveToDataKitAndShow()...");
        Incentive incentive = new Incentive();
        incentive.emaId = emaType.getId();
        incentive.emaType = emaType.getType();
        incentive.timeStamp = DateTime.getDateTime();
        incentive.incentive = incentiveRule.getIncentive();
        incentive.totalIncentive = getLastTotalIncentive() + incentive.getIncentive();
        Gson gson = new Gson();
        JsonObject sample = new JsonParser().parse(gson.toJson(incentive)).getAsJsonObject();
        DataTypeJSONObject dataTypeJSONObject = new DataTypeJSONObject(DateTime.getDateTime(), sample);
        Log.d(TAG, "IncentiveManager...saveIncentiveToDataKitAndShow()...insert to datakit...");
        DataKitAPI.getInstance(context).insert(dataSourceClient, dataTypeJSONObject);
        Log.d(TAG, "IncentiveManager...saveIncentiveToDataKitAndShow()...insert to datakit...done");
        return incentive;
    }

    public void show(IncentiveRule incentiveRule, Incentive incentive) throws DataKitException {
        Intent intent = new Intent(context, ActivityIncentive.class);
        intent.putExtra("messages", incentiveRule.getMessages());
        intent.putExtra("total_incentive", incentive.totalIncentive);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void register() throws DataKitException {
        dataSourceClient = DataKitAPI.getInstance(context).register(createDataSourceBuilderLogger());
    }

    DataSourceBuilder createDataSourceBuilderLogger() {
        Platform platform = new PlatformBuilder().setType(PlatformType.PHONE).build();
        DataSourceBuilder dataSourceBuilder = new DataSourceBuilder().setType(DataSourceType.INCENTIVE).setPlatform(platform);
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.NAME, "Incentive");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.DESCRIPTION, "Represents the log of EMA Scheduler");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.DATA_TYPE, DataTypeJSONObject.class.getName());
        ArrayList<HashMap<String, String>> dataDescriptors = new ArrayList<>();
        HashMap<String, String> dataDescriptor = new HashMap<>();
        dataDescriptor.put(METADATA.NAME, "Incentive");
        dataDescriptor.put(METADATA.UNIT, "String");
        dataDescriptor.put(METADATA.DESCRIPTION, "Contains incentive");
        dataDescriptor.put(METADATA.DATA_TYPE, Incentive.class.getName());
        dataDescriptors.add(dataDescriptor);
        dataSourceBuilder = dataSourceBuilder.setDataDescriptors(dataDescriptors);
        return dataSourceBuilder;
    }


}
