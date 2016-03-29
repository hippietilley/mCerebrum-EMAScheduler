package org.md2k.ema_scheduler.scheduler;

import android.content.Context;
import android.os.Handler;

import org.md2k.ema_scheduler.configuration.EMAType;
import org.md2k.utilities.Report.Log;

/**
 * Created by monowar on 3/14/16.
 */
public class EMIScheduler extends Scheduler {
    private static final String TAG = EMIScheduler.class.getSimpleName();
    Handler handler;

    public EMIScheduler(Context context, EMAType emaType) {
        super(context, emaType);
        Log.d(TAG, "EMIScheduler()...");
        handler = new Handler();
    }

    @Override
    public void start(long dayStartTimestamp, long dayEndTimestamp) {
        super.start(dayStartTimestamp, dayEndTimestamp);
        Log.d(TAG, "start()...");
    }

    @Override
    public void stop() {
        Log.d(TAG, "stop()...");
    }

    @Override
    public void setDayStartTimestamp(long dayStartTimestamp) {

    }

    @Override
    public void setDayEndTimestamp(long dayEndTimestamp) {

    }

}
