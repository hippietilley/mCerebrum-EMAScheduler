/*
 * Copyright (c) 2018, The University of Memphis, MD2K Center of Excellence
 *
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
import org.md2k.ema_scheduler.condition.data_quality.DataQualityManager;
import org.md2k.ema_scheduler.configuration.ConfigCondition;
import org.md2k.ema_scheduler.configuration.Configuration;
import org.md2k.ema_scheduler.configuration.EMAType;
import org.md2k.ema_scheduler.configuration.IncentiveRule;
import org.md2k.ema_scheduler.logger.LogInfo;
import org.md2k.ema_scheduler.logger.LoggerDataQuality;
import org.md2k.utilities.Report.Log;
import org.md2k.utilities.data_format.notification.NotificationResponse;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Provides methods for managing incentives.
 */
public class IncentiveManager {
    private static final String TAG = IncentiveManager.class.getSimpleName();
    private ConditionManager conditionManager;
    private Context context;
    private DataSourceClient dataSourceClient;

    /**
     * Constructor
     * @param context Android context
     * @throws DataKitException
     */
    public IncentiveManager(Context context) throws DataKitException {
        this.context = context;
        conditionManager = ConditionManager.getInstance(context);
        register();
    }

    /**
     * Starts a new <code>Thread</code>.
     * @param emaType Type of EMA.
     * @param notificationResponse User response.
     * @param completionResponse Completion response.
     * @throws DataKitException
     */
    public void start(final EMAType emaType, final String notificationResponse, final String completionResponse) throws DataKitException {
        Thread t = new Thread(new Runnable() {
            /**
             * Logs the <code>EMAType</code>, <code>notificationResponse</code>, and <code>completionResponse</code>,
             * saves the incentive to <code>DataKit</code> and calls <code>show()</code>.
             */
            @Override
            public void run() {
                try {
                    Log.d(TAG, "ematype=" + emaType.getId() + " noti=" + notificationResponse + " com=" + completionResponse);
                    if (notificationResponse.equals(NotificationResponse.CANCEL) ||
                            notificationResponse.equals(NotificationResponse.TIMEOUT))
                        return;
                    if (emaType.getIncentive_rules() == null)
                        return;
                    if (completionResponse != null && completionResponse.equals(LogInfo.STATUS_RUN_COMPLETED)) {
                        int index = getIncentiveRule(emaType);
                        if(index == -1)
                            return;
                        Incentive incentive = saveIncentiveToDataKit(emaType, index);
                        show(emaType.getIncentive_rules()[index], incentive);
                    }
                } catch (DataKitException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    /**
     * Returns the incentive rule for the given EMA type.
     * @param emaType Type of the EMA.
     * @return The incentive rule for the given EMA type.
     * @throws DataKitException
     */
    private int getIncentiveRule(EMAType emaType) throws DataKitException {
        for (int i = 0; i < emaType.getIncentive_rules().length; i++) {
            if (conditionManager.isValid(emaType.getIncentive_rules()[i].getConditions(), emaType.getType(), emaType.getId())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the last total incentive.
     * @return The last total incentive.
     * @throws DataKitException
     */
    public double getLastTotalIncentive() throws DataKitException {
        Gson gson = new Gson();
        ArrayList<DataType> dataTypes = DataKitAPI.getInstance(context).query(dataSourceClient, 1);
        if (dataTypes.size() == 0)
            return 0;
        DataTypeJSONObject dataTypeJSONObject = (DataTypeJSONObject) dataTypes.get(0);
        Incentive incentive = gson.fromJson(dataTypeJSONObject.getSample().toString(), Incentive.class);
        return incentive.getTotalIncentive();
    }

    /**
     *
     * @param emaType Type of the EMA.
     * @param index
     * @return
     * @throws DataKitException
     */
    private Incentive saveIncentiveToDataKit(EMAType emaType, int index) throws DataKitException {
        Log.d(TAG, "IncentiveManager...saveIncentiveToDataKitAndShow()...");
        IncentiveRule incentiveRule = emaType.getIncentive_rules()[index];
        ConfigCondition configCondition;
        Incentive incentive = new Incentive();
        incentive.emaId = emaType.getId();
        incentive.emaType = emaType.getType();
        incentive.timeStamp = DateTime.getDateTime();
        incentive.incentive = incentiveRule.getIncentive();
        incentive.incentiveRule = index;
        for(int i = 0; i < incentiveRule.getConditions().length; i++) {
            configCondition = Configuration.getInstance().getConditions(incentiveRule.getConditions()[i]);
            if(configCondition.getType().equals(ConditionManager.TYPE_DATA_QUALITY)){
                DataQualityManager dataQualityManager = new DataQualityManager(context);
                double percentage = dataQualityManager.getPercentage(configCondition);
                incentive.setDataQuality(percentage);
               break;
            }
        }
        incentive.totalIncentive = getLastTotalIncentive() + incentive.getIncentive();
        Gson gson = new Gson();
        JsonObject sample = new JsonParser().parse(gson.toJson(incentive)).getAsJsonObject();
        DataTypeJSONObject dataTypeJSONObject = new DataTypeJSONObject(DateTime.getDateTime(), sample);
        Log.d(TAG, "IncentiveManager...saveIncentiveToDataKitAndShow()...insert to datakit...");
        DataKitAPI.getInstance(context).insert(dataSourceClient, dataTypeJSONObject);
        Log.d(TAG, "IncentiveManager...saveIncentiveToDataKitAndShow()...insert to datakit...done");
        return incentive;
    }

    /**
     *
     * @param incentiveValue
     * @return
     * @throws DataKitException
     */
    public Incentive saveIncentiveAdminToDataKit(double incentiveValue) throws DataKitException {
        Log.d(TAG, "IncentiveManager...saveIncentiveToDataKitAndShow()...");
        Incentive incentive = new Incentive();
        incentive.emaId = "ADMIN";
        incentive.emaType = "ADMIN";
        incentive.timeStamp = DateTime.getDateTime();
        incentive.incentive = incentiveValue;
        incentive.totalIncentive = getLastTotalIncentive() + incentive.getIncentive();
        incentive.incentiveRule = -1;
        Gson gson = new Gson();
        JsonObject sample = new JsonParser().parse(gson.toJson(incentive)).getAsJsonObject();
        DataTypeJSONObject dataTypeJSONObject = new DataTypeJSONObject(DateTime.getDateTime(), sample);
        Log.d(TAG, "IncentiveManager...saveIncentiveToDataKitAndShow()...insert to datakit...");
        DataKitAPI.getInstance(context).insert(dataSourceClient, dataTypeJSONObject);
        Log.d(TAG, "IncentiveManager...saveIncentiveToDataKitAndShow()...insert to datakit...done");
        return incentive;
    }

    /**
     * Shows the total incentive in a separate activity.
     * @param incentiveRule Incentive rule to show.
     * @param incentive Incentive to show.
     * @throws DataKitException
     */
    private void show(IncentiveRule incentiveRule, Incentive incentive) throws DataKitException {
        Intent intent = new Intent(context, ActivityIncentive.class);
        intent.putExtra("messages", incentiveRule.getMessages());
        intent.putExtra("total_incentive", incentive.totalIncentive);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * Registers a <code>DataSourceBuilder</code> with <code>DataKitAPI</code>.
     * @throws DataKitException
     */
    private void register() throws DataKitException {
        dataSourceClient = DataKitAPI.getInstance(context).register(createDataSourceBuilderLogger());
    }

    /**
     * Creates a <code>DataSourceBuilder</code> object for the <code>Logger</code>.
     * @return A <code>DataSourceBuilder</code> object.
     */
    private DataSourceBuilder createDataSourceBuilderLogger() {
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
