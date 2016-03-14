package org.md2k.ema_scheduler.runner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;

import org.md2k.datakitapi.time.DateTime;
import org.md2k.utilities.Report.Log;

/**
 * Created by monowar on 3/14/16.
 */
public class RunnerMonitor {
    private static final String TAG = RunnerMonitor.class.getSimpleName();
    public static final long NO_RESPONSE_TIME=10000;
    private MyBroadcastReceiver myReceiver;
    IntentFilter intentFilter;
    Handler handler;
    Context context;
    long lastResponseTime;

    public RunnerMonitor(Context context){
        this.context=context;
        myReceiver = new MyBroadcastReceiver();
        intentFilter = new IntentFilter("org.md2k.ema_scheduler.response");
        if (intentFilter != null) {
            context.registerReceiver(myReceiver, intentFilter);
        }
        handler = new Handler();
    }
    public void start(long timeout){
        handler.postDelayed(runnableTimeOut, timeout);
    }
    Runnable runnableTimeOut = new Runnable() {
        @Override
        public void run() {
            if(DateTime.getDateTime()-lastResponseTime<NO_RESPONSE_TIME)
                handler.postDelayed(this,DateTime.getDateTime()-lastResponseTime);
            else {
                sendData();
            }
        }
    };
    void sendData() {
        Intent intent = new Intent();
        intent.setAction("org.md2k.ema.operation");
        intent.putExtra("type", "missed");
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        context.sendBroadcast(intent);
    }
    void clear(){
        Log.d(TAG,"clear()...");
        sendData();
        handler.removeCallbacks(runnableTimeOut);
        if (myReceiver != null)
            context.unregisterReceiver(myReceiver);
        Log.d(TAG,"...clear()");

    }
    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra("type");
            if (type.equals("question_answer")) {
                String value = intent.getStringExtra("value");
                Log.d(TAG, "data received... sample=" + value);
            } else if (type.equals("last_response_time")) {
                lastResponseTime = intent.getLongExtra("value", -1);
                Log.d(TAG, "data received... lastResponseTime=" + lastResponseTime);
            }
        }
    }
}
