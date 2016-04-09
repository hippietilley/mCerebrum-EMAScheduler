package org.md2k.ema_scheduler.notifier;

import android.content.Context;
import android.os.Handler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.datatype.DataTypeJSONObject;
import org.md2k.datakitapi.messagehandler.OnReceiveListener;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.ema_scheduler.configuration.Configuration;
import org.md2k.ema_scheduler.configuration.EMAType;
import org.md2k.ema_scheduler.configuration.Notification;
import org.md2k.ema_scheduler.delivery.Callback;
import org.md2k.ema_scheduler.logger.LogInfo;
import org.md2k.ema_scheduler.logger.LoggerManager;
import org.md2k.utilities.Report.Log;
import org.md2k.utilities.data_format.NotificationAcknowledge;
import org.md2k.utilities.data_format.NotificationRequest;

import java.util.ArrayList;

/**
 * Created by monowar on 3/10/16.
 */
public class NotifierManager {
    private static final String TAG = NotifierManager.class.getSimpleName();
    Context context;
    Handler handler;
    int notifyNo;
    boolean delayEnable = true;
    DataSourceClient dataSourceClientRequest;
    ArrayList<DataSourceClient> dataSourceClientAcknowledges;
    Notification[] notifications;
    NotificationRequest notificationRequestAll[];
    Handler handlerSubscribe;
    Callback callbackDelivery;
    EMAType emaType;
    Runnable runnableNotify = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "runnableNotify...");
            logNotify(LogInfo.STATUS_NOTIFICATION_NOTIFYING, "notifying..." + String.valueOf(notifyNo + 1));
            NotificationRequest notificationRequestSelected[] = findNotification(notifications[notifyNo].getTypes());
            insertDataToDataKit(notificationRequestSelected);
            Log.d(TAG, "notifications length=" + notifications.length + " now=" + notifyNo);
            notifyNo++;
            if (notifyNo < notifications.length)
                handler.postDelayed(this, (notifications[notifyNo].getTime() - notifications[notifyNo - 1].getTime()));
        }
    };
    Runnable runnableSubscribe = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "runnableSubscribe...run()");
            DataSourceBuilder dataSourceBuilder = new DataSourceBuilder().setType(DataSourceType.NOTIFICATION_ACKNOWLEDGE);
            dataSourceClientAcknowledges = DataKitAPI.getInstance(context).find(dataSourceBuilder);
            Log.d(TAG, "DataSourceClients...size=" + dataSourceClientAcknowledges.size());
            if (dataSourceClientAcknowledges.size() == 0) {
                handlerSubscribe.postDelayed(this, 1000);
            } else {
                subscribeNotificationAcknowledge();
            }
        }
    };

    public NotifierManager(Context context) {
        Log.d(TAG, "NotifierManager()...");
        this.context = context;
        Log.d(TAG, "datakit register ... before register()");
        dataSourceClientRequest = DataKitAPI.getInstance(context).register(new DataSourceBuilder().setType(DataSourceType.NOTIFICATION_REQUEST));
        Log.d(TAG, "datakit register ... after register() " + dataSourceClientRequest.getStatus().getStatusMessage());
        notificationRequestAll = Configuration.getInstance().getNotificationRequests();
        handler = new Handler();
        handlerSubscribe = new Handler();

    }

    public void set(EMAType emaType, Callback callback) {
        Log.d(TAG, "set()...");
        this.emaType = emaType;
        this.notifications = emaType.getNotifications();
        this.callbackDelivery = callback;
        Log.d(TAG, "before runnableSubscribe..");
        handlerSubscribe.post(runnableSubscribe);
    }

    void subscribeNotificationAcknowledge() {
        Log.d(TAG, "subscribeNotificationAcknowledge...");
        for (int i = 0; i < dataSourceClientAcknowledges.size(); i++) {
            DataKitAPI.getInstance(context).subscribe(dataSourceClientAcknowledges.get(i), new OnReceiveListener() {
                @Override
                public void onReceived(final DataType dataType) {
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            DataTypeJSONObject dataTypeJSONObject = (DataTypeJSONObject) dataType;
                            Gson gson = new Gson();
                            NotificationAcknowledge notificationAcknowledge = gson.fromJson(dataTypeJSONObject.getSample().toString(), NotificationAcknowledge.class);
                            Log.d(TAG, "notification_acknowledge = " + notificationAcknowledge.getStatus());
                            stop();
                            switch (notificationAcknowledge.getStatus()) {

                                case NotificationAcknowledge.DELAY:
                                    notifyNo = 0;
                                    long delay = notificationAcknowledge.getNotificationRequest().getResponse_option().getDelay_time();
                                    delayEnable = false;
                                    Log.d(TAG, "delay = " + delay);
                                    logNotificationResponse(LogInfo.STATUS_NOTIFICATION_RESPONSE_DELAY, "User select DELAY: " + String.valueOf(delay/(1000*60))+" Minute");
                                    handler.postDelayed(runnableNotify, delay);
                                    break;
                                case NotificationAcknowledge.OK:
                                case NotificationAcknowledge.CANCEL:
                                case NotificationAcknowledge.DELAY_CANCEL:
                                    logNotificationResponse(notificationAcknowledge.getStatus(), "User select "+notificationAcknowledge.getStatus());
                                    callbackDelivery.onResponse(notificationAcknowledge.getStatus());
                                    clear();
                                    break;
                                case NotificationAcknowledge.TIMEOUT:
                                    logNotificationResponse(notificationAcknowledge.getStatus(), "notification: "+notificationAcknowledge.getStatus());
                                    callbackDelivery.onResponse(notificationAcknowledge.getStatus());
                                    clear();
                                    break;
                            }
                        }
                    });
                    t.start();
                }
            });
        }
    }

    public void clear() {
        try {
            Log.d(TAG, "clear()...");
            handler.removeCallbacks(runnableNotify);
            handlerSubscribe.removeCallbacks(runnableSubscribe);
            if (dataSourceClientAcknowledges != null)
                for (int i = 0; i < dataSourceClientAcknowledges.size(); i++)
                    DataKitAPI.getInstance(context).unsubscribe(dataSourceClientAcknowledges.get(i));
            dataSourceClientAcknowledges = null;
            Log.d(TAG, "...clear()");
        }catch (Exception e){

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

    private void insertDataToDataKit(NotificationRequest[] notificationRequests) {
        Log.d(TAG, "insertDataToDataKit()...");
        DataKitAPI dataKitAPI = DataKitAPI.getInstance(context);
        for (NotificationRequest notificationRequest : notificationRequests) {
            boolean isDelayOk = false;
            if (notificationRequest.getResponse_option() != null)
                isDelayOk = notificationRequest.getResponse_option().isDelay();
            if (isDelayOk == true && delayEnable == false)
                notificationRequest.getResponse_option().setDelay(false);
            Gson gson = new Gson();
            JsonObject sample = new JsonParser().parse(gson.toJson(notificationRequest)).getAsJsonObject();
            DataTypeJSONObject dataTypeJSONObject = new DataTypeJSONObject(DateTime.getDateTime(), sample);
            dataKitAPI.insert(dataSourceClientRequest, dataTypeJSONObject);
            if (isDelayOk == true && delayEnable == false && notificationRequest.getResponse_option() != null)
                notificationRequest.getResponse_option().setDelay(true);
        }
        Log.d(TAG, "...insertDataToDataKit()");
    }

    public NotificationRequest[] findNotification(String notificationType[]) {
        NotificationRequest notificationRequestSelected[] = new NotificationRequest[notificationType.length];
        for (int i = 0; i < notificationType.length; i++) {
            for (NotificationRequest aNotificationRequestAll : notificationRequestAll) {
                if (notificationType[i].equals(aNotificationRequestAll.getId())) {
                    Log.d(TAG, "notification... ID=" + aNotificationRequestAll.getId());
                    notificationRequestSelected[i] = aNotificationRequestAll;
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

    protected void logNotify(String status, String message) {
        LogInfo logInfo = new LogInfo();
        logInfo.setOperation(LogInfo.OP_NOTIFICATION);
        logInfo.setId(emaType.getId());
        logInfo.setType(emaType.getType());
        logInfo.setTimestamp(DateTime.getDateTime());
        logInfo.setStatus(status);
        logInfo.setMessage(message);
        LoggerManager.getInstance(context).insert(logInfo);
    }

    protected void logNotificationResponse(String status, String notificationResult) {
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
