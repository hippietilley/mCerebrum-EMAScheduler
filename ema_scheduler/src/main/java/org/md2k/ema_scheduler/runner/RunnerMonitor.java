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

package org.md2k.ema_scheduler.runner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.md2k.datakitapi.DataKitAPI;
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
import org.md2k.ema_scheduler.ServiceEMAScheduler;
import org.md2k.ema_scheduler.configuration.Application;
import org.md2k.ema_scheduler.configuration.EMAType;
import org.md2k.ema_scheduler.delivery.Callback;
import org.md2k.ema_scheduler.incentive.IncentiveManager;
import org.md2k.ema_scheduler.logger.LogInfo;
import org.md2k.ema_scheduler.logger.LoggerManager;
import org.md2k.utilities.Report.Log;
import org.md2k.utilities.data_format.notification.NotificationResponse;

/**
 * Monitors the runners for timeout and saving data.
 */
class RunnerMonitor {
    private static final long NO_RESPONSE_TIME = 35000;
    private static final String TAG = RunnerMonitor.class.getSimpleName();
    private Handler handler;
    private Context context;
    private long lastResponseTime;
    private String message;
    private String type;
    private Application application;
    private EMA ema;
    private EMAType emaType;
    private Callback callback;
    private boolean isStart = false;
    private MyBroadcastReceiver myReceiver;
    private Runnable runnableTimeOut = new Runnable() {
        /**
         * Handles the timeout for the EMA.
         */
        @Override
        public void run() {
            if (DateTime.getDateTime() - lastResponseTime < NO_RESPONSE_TIME)
                handler.postDelayed(this, DateTime.getDateTime() - lastResponseTime);
            else {
                sendData();
                handler.postDelayed(runnableWaitThenSave, 3000);
            }
        }
    };
    private Runnable runnableWaitThenSave = new Runnable() {
        /**
         * Saves EMA data when the EMA is abandoned by timeout.
         */
        @Override
        public void run() {
            try {
                saveData(null, LogInfo.STATUS_RUN_ABANDONED_BY_TIMEOUT);
            } catch (DataKitException e) {
                Log.d(TAG, "DataKitException...saveData");
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ServiceEMAScheduler.BROADCAST_MSG));
            }
        }
    };

    /**
     * Constructor
     * @param context Android context
     * @param callback Response callback
     * @throws DataKitException
     */
    RunnerMonitor(Context context, Callback callback) throws DataKitException {
        this.context = context;
        this.callback = callback;
        myReceiver = new MyBroadcastReceiver();
        handler = new Handler();
    }

    /**
     * Starts a new <code>EMA</code> and registers a receiver for user responses.
     * @param emaType Type of EMA.
     * @param notificationResponse User response.
     * @param application Application
     * @param type EMA trigger type.
     * @throws DataKitException
     */
    public void start(EMAType emaType, String notificationResponse, Application application, String type) throws DataKitException {
        isStart = true;
        context.registerReceiver(myReceiver, new IntentFilter("org.md2k.ema_scheduler.response"));
        this.type = type;
        this.application = application;
        this.emaType = emaType;
        ema = new EMA();
        ema.start_timestamp = DateTime.getDateTime();
        ema.id = application.getId();
        ema.name = application.getName();
        ema.trigger_type = type;
        if (!notificationResponse.equals(NotificationResponse.DELAY)) {
            switch (notificationResponse) {
                case NotificationResponse.OK:
                case NotificationResponse.DELAY_CANCEL:
                    Intent intent = context.getPackageManager().getLaunchIntentForPackage(application.getPackage_name());
                    intent.setAction(application.getPackage_name());
                    intent.putExtra("file_name", application.getFile_name());
                    intent.putExtra("id", application.getId());
                    intent.putExtra("name", application.getName());
                    intent.putExtra("timeout", application.getTimeout());
                    context.startActivity(intent);
                    Log.d(TAG, "timeout=" + application.getTimeout());
                    handler.postDelayed(runnableTimeOut, application.getTimeout());
                    log(LogInfo.STATUS_RUN_START, "EMA Starts");
                    break;
                case NotificationResponse.CANCEL:
                    ema.status = LogInfo.STATUS_RUN_ABANDONED_BY_USER;
                    ema.end_timestamp = DateTime.getDateTime();
                    log(LogInfo.STATUS_RUN_ABANDONED_BY_USER, "EMA abandoned by user at prompt");
                    saveToDataKit(notificationResponse, null);
                    clear();
                    break;
                case NotificationResponse.TIMEOUT:
                    ema.status = LogInfo.STATUS_RUN_MISSED;
                    ema.end_timestamp = DateTime.getDateTime();
                    log(LogInfo.STATUS_RUN_MISSED, "EMA is timed out..at prompt..MISSED");
                    saveToDataKit(notificationResponse, null);
                    clear();
                    break;
                default: {
                    ema.status = LogInfo.STATUS_RUN_COMPLETED;
                    ema.end_timestamp = DateTime.getDateTime();
                    ema.question_answers = createJson(notificationResponse);
                    log(LogInfo.STATUS_RUN_COMPLETED, "EMA completed");
                    ema.question_answers = createJson(notificationResponse);
                    saveToDataKit(notificationResponse, ema.status);
                    clear();
                }
            }
        }
    }

    /**
     * Creates a <code>JsonArray</code> for the question, the question type, and response options.
     * @param response User response.
     * @return A <code>JsonArray</code> for the question, the question type, and response options.
     */
    private JsonArray createJson(String response) {
        JsonArray valueArray = new JsonArray();
        JsonObject jsonPropValue = new JsonObject();
        jsonPropValue.addProperty("question_text", "In the last 10 minutes, did you smoke?");
        jsonPropValue.addProperty("question_type", "singlechoice");
        jsonPropValue.addProperty("question_option1", "YES - I smoked a cigarette or cigar.");
        jsonPropValue.addProperty("question_option2", "YES - I smoked an e-cigarette or vaporizer.");
        jsonPropValue.addProperty("question_option3", "NO - I did NOT smoke.");
        jsonPropValue.addProperty("question_answer", response);
        valueArray.add(jsonPropValue);
        return valueArray;
    }

    /**
     * Removes callbacks from the handler.
     */
    void clear() {
        Log.d(TAG, "clear()...");
        if (isStart) {
            handler.removeCallbacks(runnableTimeOut);
            handler.removeCallbacks(runnableWaitThenSave);
            if (myReceiver != null)
                context.unregisterReceiver(myReceiver);
        }
        Log.d(TAG, "...clear()");
        isStart = false;
    }

    /**
     * Creates a new <code>LogInfo</code> object and inserts it into the log.
     * @param status Status of the EMA.
     * @param message Log message.
     * @throws DataKitException
     */
    private void log(String status, String message) throws DataKitException {
        if (type.equals("SYSTEM")) {
            LogInfo logInfo = new LogInfo();
            logInfo.setOperation(LogInfo.OP_RUN);
            logInfo.setId(emaType.getId());
            logInfo.setType(emaType.getType());
            logInfo.setTimestamp(DateTime.getDateTime());
            logInfo.setStatus(status);
            logInfo.setMessage(message);
            LoggerManager.getInstance(context).insert(logInfo);
        }
    }

    /**
     * Sends an <code>Intent</code>.
     */
    private void sendData() {
        Intent intent = new Intent();
        intent.setAction("org.md2k.ema.operation");
        intent.putExtra("TYPE", "TIMEOUT");
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        context.sendBroadcast(intent);
    }

    /**
     * Creates a <code>DataSourceBuilder</code> for the given EMA id.
     * @param id Id of the EMA.
     * @return A <code>DataSourceBuilder</code> for the given EMA id.
     */
    private DataSourceBuilder createDataSourceBuilder(String id) {
        Platform platform = new PlatformBuilder().setType(PlatformType.PHONE).setMetadata(METADATA.NAME, "Phone").build();
        DataSourceBuilder dataSourceBuilder = new DataSourceBuilder().setType(DataSourceType.EMA).setPlatform(platform).setId(id);
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.NAME, "EMA");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.DESCRIPTION, "EMA & EMI Question and answers");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.DATA_TYPE, DataTypeJSONObject.class.getName());
        return dataSourceBuilder;
    }

    /**
     * Starts <code>IncentiveManager</code> to show the user the incentive.
     * @param notificationResponse User response to notification.
     * @param completionResponse Response about EMA completetion.
     * @throws DataKitException
     */
    private void saveAndShowIncentive(String notificationResponse, String completionResponse) throws DataKitException {
        if (!type.equals("SYSTEM"))
            return;
        IncentiveManager incentiveManager = new IncentiveManager(context);
        incentiveManager.start(emaType, notificationResponse, completionResponse);
    }

    /**
     * Saves the EMA data to <code>DataKit</code>.
     * @param notificationResponse User response to notification.
     * @param completionResponse Response about EMA completetion.
     * @throws DataKitException
     */
    private void saveToDataKit(String notificationResponse, String completionResponse) throws DataKitException {
        Gson gson = new Gson();
        JsonObject sample = new JsonParser().parse(gson.toJson(ema)).getAsJsonObject();
        DataTypeJSONObject dataTypeJSONObject = new DataTypeJSONObject(DateTime.getDateTime(), sample);
        DataSourceClient dataSourceClient = DataKitAPI.getInstance(context).register(createDataSourceBuilder(ema.id));
        DataKitAPI.getInstance(context).insert(dataSourceClient, dataTypeJSONObject);
        saveAndShowIncentive(notificationResponse, completionResponse);
        callback.onResponse(ema.status);
    }

    /**
     * Logs the EMA status and extracts the <code>question_answers</code> from the given <code>JsonArray</code>
     * of answers, then calls <code>saveToDataKit()</code>.
     * @param answer Array of question answers.
     * @param status Status of the EMA.
     * @throws DataKitException
     */
    private void saveData(JsonArray answer, String status) throws DataKitException {
        ema.end_timestamp = DateTime.getDateTime();
        ema.question_answers = answer;
        Log.d(TAG, "status=" + status);
        if (status == null)
            ema.status = LogInfo.STATUS_RUN_ABANDONED_BY_USER;
        else
            ema.status = status;
        log(ema.status, ema.status);
        saveToDataKit(NotificationResponse.OK, ema.status);
        clear();
    }

    /**
     * Nested class for receiving broadcasts.
     */
    public class MyBroadcastReceiver extends BroadcastReceiver {
        /**
         * Saves data from the intent if it is a <code>"RESULT"</code> and logs the status if
         * the intent is a <code>"STATUS_MESSAGE"</code>.
         * @param context Android context.
         * @param intent Intent received.
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String type = intent.getStringExtra("TYPE");
                if (type.equals("RESULT")) {
                    String answer = intent.getStringExtra("ANSWER");
                    String status = intent.getStringExtra("STATUS");
                    Log.d(TAG, "result...");
                    JsonParser parser = new JsonParser();
                    JsonElement tradeElement = parser.parse(answer);
                    JsonArray question_answer = tradeElement.getAsJsonArray();
                    handler.removeCallbacks(runnableWaitThenSave);
                    saveData(question_answer, status);
                } else if (type.equals("STATUS_MESSAGE")) {
                    lastResponseTime = intent.getLongExtra("TIMESTAMP", -1);
                    message = intent.getStringExtra("MESSAGE");
                    Log.d(TAG, "data received... lastResponseTime=" + lastResponseTime + " message=" + message);
                }
            } catch (DataKitException e) {
                Log.d(TAG, "DataKitException...savedata..");
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ServiceEMAScheduler.BROADCAST_MSG));
            }
        }
    }
}
