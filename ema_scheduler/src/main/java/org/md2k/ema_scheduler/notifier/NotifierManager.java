package org.md2k.ema_scheduler.notifier;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.datatype.DataTypeString;
import org.md2k.datakitapi.messagehandler.OnReceiveListener;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.source.platform.PlatformBuilder;
import org.md2k.datakitapi.source.platform.PlatformType;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.ema_scheduler.configuration.Configuration;
import org.md2k.ema_scheduler.configuration.Notification;
import org.md2k.ema_scheduler.delivery.Callback;
import org.md2k.utilities.data_format.NotificationAcknowledge;
import org.md2k.utilities.data_format.NotificationRequest;
import org.md2k.utilities.Report.Log;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by monowar on 3/10/16.
 */
public class NotifierManager {
    private static final String TAG = NotifierManager.class.getSimpleName();
    Context context;
    Handler handler;
    int notifyNo;
    boolean delayEnable=true;
    DataSourceClient dataSourceClientRequest;
    ArrayList<DataSourceClient> dataSourceClientAcknowledges;
    Notification[] notifications;
    NotificationRequest notificationRequestAll[];
    Handler handlerSubscribe;
    Callback callbackDelivery;
    public NotifierManager(Context context,Notification[] notifications, Callback callback){
        this.context=context;
        this.notifications=notifications;
        this.callbackDelivery=callback;
        notificationRequestAll= Configuration.getInstance().getNotificationRequests();
        handler=new Handler();
        DataSourceBuilder dataSourceBuilder = createDataSourceBuilder();
        Log.d(TAG,"datakit register ... before register()");
        dataSourceClientRequest = DataKitAPI.getInstance(context).register(dataSourceBuilder);
        Log.d(TAG,"datakit register ... after register() "+dataSourceClientRequest.getStatus().getStatusMessage());
        handlerSubscribe = new Handler();
        handlerSubscribe.post(runnbleSubscribe);
    }
    void subscribeDataKit(){
        for (int i = 0; i < dataSourceClientAcknowledges.size(); i++) {
            DataKitAPI.getInstance(context).subscribe(dataSourceClientAcknowledges.get(i), new OnReceiveListener() {
                @Override
                public void onReceived(DataType dataType) {
                    DataTypeString dataTypeString = (DataTypeString) dataType;
                    Log.d(TAG, "dataTypeString=" + dataTypeString.getSample());
                    Gson gson = new Gson();
                    Type collectionType = new TypeToken<NotificationAcknowledge>() {
                    }.getType();
                    NotificationAcknowledge notificationAcknowledge = gson.fromJson(dataTypeString.getSample(), collectionType);
                    Log.d(TAG,"notification_acknowledge = "+notificationAcknowledge.getStatus());
                    stop();
                    switch (notificationAcknowledge.getStatus()) {
                        case NotificationAcknowledge.DELAY:
                            notifyNo = 0;
                            long delay = getDelay();
                            delayEnable = false;
                            Log.d(TAG, "delay = " + delay);
                            handler.postDelayed(runnableNotify, delay);
                            break;
                        case NotificationAcknowledge.OK:
                        case NotificationAcknowledge.CANCEL:
                        case NotificationAcknowledge.TIMEOUT:
                            callbackDelivery.onResponse(notificationAcknowledge.getStatus());
                            clear();
                            break;
                    }
                }
            });
        }
    }
    Runnable runnbleSubscribe = new Runnable() {
        @Override
        public void run() {
            DataSourceBuilder dataSourceBuilder = new DataSourceBuilder().setType(DataSourceType.NOTIFICATION_ACKNOWLEDGE);
            dataSourceClientAcknowledges = DataKitAPI.getInstance(context).find(dataSourceBuilder);
            Log.d(TAG, "DataSourceClients...size=" + dataSourceClientAcknowledges.size());
            if (dataSourceClientAcknowledges.size() == 0) {
                handlerSubscribe.postDelayed(this, 1000);
            } else {
                subscribeDataKit();
            }
        }
    };
    public void clear(){
        handler.removeCallbacks(runnableNotify);
        handlerSubscribe.removeCallbacks(runnbleSubscribe);
//        for(int i=0;i<dataSourceClientAcknowledges.size();i++)
//            DataKitAPI.getInstance(context).unsubscribe(dataSourceClientAcknowledges.get(i));
    }
    long getDelay(){
        NotificationRequest notificationRequestSelected[]=findNotification(notifications[0].getTypes());
        for (NotificationRequest aNotificationRequestSelected : notificationRequestSelected) {
            if (aNotificationRequestSelected.getResponse_option() == null) continue;
            if (aNotificationRequestSelected.getResponse_option().isDelay())
                return aNotificationRequestSelected.getResponse_option().getDelay_time();
        }
        return -1;
    }
    public void start(){
        delayEnable=true;
        if(notifications.length==0) return;
        Log.d(TAG,"Notification length="+notifications.length);
        notifyNo=0;
        handler.postDelayed(runnableNotify, notifications[notifyNo].getTime());
    }
    Runnable runnableNotify =new Runnable() {
        @Override
        public void run() {
            NotificationRequest notificationRequestSelected[]=findNotification(notifications[notifyNo].getTypes());
            insertDataToDataKit(notificationRequestSelected);
            Log.d(TAG,"notifications length="+notifications.length+" now="+notifyNo);
            notifyNo++;
            if(notifyNo<notifications.length)
                handler.postDelayed(this, (notifications[notifyNo].getTime()-notifications[notifyNo-1].getTime()));
        }
    };
    private void insertDataToDataKit(NotificationRequest[] notificationRequests) {
        DataKitAPI dataKitAPI = DataKitAPI.getInstance(context);
        if (dataKitAPI.isConnected()) {
            for (NotificationRequest notificationRequest : notificationRequests) {
                Gson gson = new Gson();
                String json = gson.toJson(notificationRequest);
                DataTypeString dataTypeString = new DataTypeString(DateTime.getDateTime(), json);
                dataKitAPI.insert(dataSourceClientRequest, dataTypeString);
            }
        } else {
            Toast.makeText(context, "DataKit is not available...", Toast.LENGTH_LONG).show();
        }
    }

    public NotificationRequest[] findNotification(String notificationType[]){
        NotificationRequest notificationRequestSelected[]=new NotificationRequest[notificationType.length];
        for(int i=0;i<notificationType.length;i++){
            for (NotificationRequest aNotificationRequestAll : notificationRequestAll) {
                if (notificationType[i].equals(aNotificationRequestAll.getId())) {
                    Log.d(TAG, "notification... ID=" + aNotificationRequestAll.getId());
                    notificationRequestSelected[i] = aNotificationRequestAll;
                    if(delayEnable==false && notificationRequestSelected[i].getResponse_option()!=null){
                        notificationRequestSelected[i].getResponse_option().setDelay(false);
                    }
                    break;
                }
            }
        }
        return notificationRequestSelected;
    }
    public void stop(){
      handler.removeCallbacks(runnableNotify);

    }
    DataSourceBuilder createDataSourceBuilder() {
        PlatformBuilder platformBuilder = new PlatformBuilder().setType(PlatformType.PHONE);
        return new DataSourceBuilder().setType(DataSourceType.NOTIFICATION_REQUEST).setPlatform(platformBuilder.build());
    }

}
