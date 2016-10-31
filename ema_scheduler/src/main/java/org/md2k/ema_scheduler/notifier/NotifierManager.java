package org.md2k.ema_scheduler.notifier;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.datatype.DataTypeJSONObject;
import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.messagehandler.OnReceiveListener;
import org.md2k.datakitapi.source.application.Application;
import org.md2k.datakitapi.source.application.ApplicationBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.ema_scheduler.ServiceEMAScheduler;
import org.md2k.ema_scheduler.configuration.Configuration;
import org.md2k.ema_scheduler.configuration.EMAType;
import org.md2k.ema_scheduler.configuration.Notification;
import org.md2k.ema_scheduler.delivery.Callback;
import org.md2k.ema_scheduler.logger.LogInfo;
import org.md2k.ema_scheduler.logger.LoggerManager;
import org.md2k.utilities.Report.Log;
import org.md2k.utilities.data_format.notification.NotificationRequest;
import org.md2k.utilities.data_format.notification.NotificationRequests;
import org.md2k.utilities.data_format.notification.NotificationResponse;

import java.util.ArrayList;

/*
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
public class NotifierManager {
    private static final String TAG = NotifierManager.class.getSimpleName();
    private Context context;
    private Handler handler;
    private Handler handlerSubscribe;
    private int notifyNo;
    private boolean delayEnable;
    private DataSourceClient dataSourceClientRequest;
    private ArrayList<DataSourceClient> dataSourceClientResponses;
    private Notification[] notifications;
    private NotificationRequests notificationRequestAll;
    private Callback callbackDelivery;
    private long lastInsertTime = 0;
    private EMAType emaType;
    private NotificationRequests notificationRequestSelected;
    private Runnable runnableNotify = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "runnableNotify...");
            try {
                logNotify(LogInfo.STATUS_NOTIFICATION_NOTIFYING, "notifying..." + String.valueOf(notifyNo + 1));
                notificationRequestSelected = findNotification(notifications[notifyNo].getTypes());
                lastInsertTime = DateTime.getDateTime();
                insertDataToDataKit(notificationRequestSelected);
                Log.d(TAG, "notifications length=" + notifications.length + " now=" + notifyNo);
                notifyNo++;
                if (notifyNo < notifications.length)
                    handler.postDelayed(this, (notifications[notifyNo].getTime() - notifications[notifyNo - 1].getTime()));
            } catch (DataKitException e) {
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ServiceEMAScheduler.BROADCAST_MSG));
            }
        }
    };
    private Runnable runnableSubscribe = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "runnableSubscribe...run()");
            Application application = new ApplicationBuilder().setId("org.md2k.notificationmanager").build();
            DataSourceBuilder dataSourceBuilderR = new DataSourceBuilder().setType(DataSourceType.NOTIFICATION_RESPONSE).setApplication(application);
            try {
                dataSourceClientResponses = DataKitAPI.getInstance(context).find(dataSourceBuilderR);
                if (dataSourceClientResponses.size() == 0) {
                    handlerSubscribe.postDelayed(this, 1000);
                } else {
                    subscribeNotificationResponse();
                }
            } catch (DataKitException e) {
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ServiceEMAScheduler.BROADCAST_MSG));
            }
        }
    };

    public NotifierManager(Context context) throws DataKitException {
        Log.d(TAG, "NotifierManager()...");
        this.context = context;
        Log.d(TAG, "datakit register ... before register()");
        dataSourceClientRequest = DataKitAPI.getInstance(context).register(new DataSourceBuilder().setType(DataSourceType.NOTIFICATION_REQUEST));
        Log.d(TAG, "datakit register ... after register() " + dataSourceClientRequest.getStatus().getStatusMessage());
        notificationRequestAll = Configuration.getInstance().getNotification_option();
        handler = new Handler();
        handlerSubscribe = new Handler();

    }

    public void set(EMAType emaType, Callback callback) {
        Log.d(TAG, "set()...");
        this.emaType = emaType;
        this.notifications = emaType.getNotifications();
        this.callbackDelivery = callback;
        lastInsertTime = 0;
        notifyNo = 0;
        delayEnable = true;
        Log.d(TAG, "before runnableSubscribe..");
        handlerSubscribe.post(runnableSubscribe);
    }

    public void clear() {
        try {
            Log.d(TAG, "clear()...");
            handler.removeCallbacks(runnableNotify);
            handlerSubscribe.removeCallbacks(runnableSubscribe);
            if (dataSourceClientResponses != null)
                for (int i = 0; i < dataSourceClientResponses.size(); i++)
                    DataKitAPI.getInstance(context).unsubscribe(dataSourceClientResponses.get(i));
            dataSourceClientResponses = null;
            Log.d(TAG, "...clear()");
        } catch (Exception e) {

        }
    }

    private void subscribeNotificationResponse() throws DataKitException {
        Log.d(TAG, "subscribeNotificationResponse...");
        for (int i = 0; i < dataSourceClientResponses.size(); i++) {
            DataKitAPI.getInstance(context).subscribe(dataSourceClientResponses.get(i), new OnReceiveListener() {
                @Override
                public void onReceived(final DataType dataType) {
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                DataTypeJSONObject dataTypeJSONObject = (DataTypeJSONObject) dataType;
                                Gson gson = new Gson();
                                NotificationResponse notificationResponse = gson.fromJson(dataTypeJSONObject.getSample().toString(), NotificationResponse.class);
                                Log.d(TAG, "notification_response = " + notificationResponse.getStatus());
                                stop();
                                switch (notificationResponse.getStatus()) {
                                    case NotificationResponse.DELAY:
                                        notifyNo = 0;
                                        long delay = notificationResponse.getNotificationRequest().getDuration();
                                        delayEnable = false;
                                        Log.d(TAG, "delay = " + delay);
                                        logNotificationResponse(LogInfo.STATUS_NOTIFICATION_RESPONSE_DELAY, "User select DELAY: " + String.valueOf(delay / (1000 * 60)) + " Minute");
                                        handler.postDelayed(runnableNotify, delay);
                                        break;
                                    case NotificationResponse.OK:
                                    case NotificationResponse.CANCEL:
                                    case NotificationResponse.DELAY_CANCEL:
                                        logNotificationResponse(notificationResponse.getStatus(), "User select " + notificationResponse.getStatus());
                                        callbackDelivery.onResponse(notificationResponse.getStatus());
                                        clear();
                                        break;
                                    case NotificationResponse.TIMEOUT:
                                        logNotificationResponse(notificationResponse.getStatus(), "notification: " + notificationResponse.getStatus());
                                        callbackDelivery.onResponse(notificationResponse.getStatus());
                                        clear();
                                        break;
                                }

                            } catch (DataKitException e) {
                                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ServiceEMAScheduler.BROADCAST_MSG));
                            }
                        }
                    });
                    t.start();
                }
            });
        }
    }

    public void start() {
        Log.d(TAG, "start()...");
        delayEnable = true;
        if (notifications.length == 0) return;
        Log.d(TAG, "Notification length=" + notifications.length);
        notifyNo = 0;
        handler.postDelayed(runnableNotify, notifications[notifyNo].getTime());
    }

    private void insertDataToDataKit(NotificationRequests notificationRequests) throws DataKitException {
        ArrayList<NotificationRequest> temp = new ArrayList<>();
        Log.d(TAG, "insertDataToDataKit()...");
        DataKitAPI dataKitAPI = DataKitAPI.getInstance(context);
        for (NotificationRequest notificationRequest : notificationRequests.getNotification_option()) {
            boolean isDelayOk = false;
            if (notificationRequest.getResponse_option() != null)
                isDelayOk = notificationRequest.getResponse_option().isDelay();
            if (isDelayOk && !delayEnable) {
                notificationRequest.getResponse_option().setDelay(false);
                temp.add(notificationRequest);
            }
        }
        Gson gson = new Gson();
        JsonObject sample = new JsonParser().parse(gson.toJson(notificationRequests)).getAsJsonObject();
        DataTypeJSONObject dataTypeJSONObject = new DataTypeJSONObject(DateTime.getDateTime(), sample);
        dataKitAPI.insert(dataSourceClientRequest, dataTypeJSONObject);
        Log.d(TAG, "...insertDataToDataKit()");
        for (int i = 0; i < temp.size(); i++)
            temp.get(i).getResponse_option().setDelay(true);

    }

    private NotificationRequests findNotification(String notificationType[]) {
        NotificationRequests notificationRequestSelected = new NotificationRequests();
        for (int i = 0; i < notificationType.length; i++) {
            for (NotificationRequest aNotificationRequestAll : notificationRequestAll.getNotification_option()) {
                if (notificationType[i].equals(aNotificationRequestAll.getId())) {
                    Log.d(TAG, "notification... ID=" + aNotificationRequestAll.getId());
                    notificationRequestSelected.getNotification_option().add(aNotificationRequestAll);
//                    if (delayEnable == false && notificationRequestSelected[i].getResponse_option() != null) {
//                        notificationRequestSelected[i].getResponse_option().setDelay(false);
//                    }
                    break;
                }
            }
        }
        return notificationRequestSelected;
    }

    public void stop() {
        Log.d(TAG, "stop()...");
        handler.removeCallbacks(runnableNotify);

    }

    private void logNotify(String status, String message) throws DataKitException {
        LogInfo logInfo = new LogInfo();
        logInfo.setOperation(LogInfo.OP_NOTIFICATION);
        logInfo.setId(emaType.getId());
        logInfo.setType(emaType.getType());
        logInfo.setTimestamp(DateTime.getDateTime());
        logInfo.setStatus(status);
        logInfo.setMessage(message);
        LoggerManager.getInstance(context).insert(logInfo);
    }

    private void logNotificationResponse(String status, String notificationResult) throws DataKitException {
        LogInfo logInfo = new LogInfo();
        logInfo.setOperation(LogInfo.OP_NOTIFICATION_RESPONSE);
        logInfo.setId(emaType.getId());
        logInfo.setType(emaType.getType());
        logInfo.setTimestamp(DateTime.getDateTime());
        logInfo.setStatus(status);
        logInfo.setMessage(notificationResult);
        LoggerManager.getInstance(context).insert(logInfo);
    }
}
