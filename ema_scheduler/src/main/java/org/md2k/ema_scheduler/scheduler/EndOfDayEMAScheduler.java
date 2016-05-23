package org.md2k.ema_scheduler.scheduler;

import android.content.Context;
import android.os.Handler;

import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.ema_scheduler.condition.ConditionManager;
import org.md2k.ema_scheduler.configuration.EMAType;
import org.md2k.ema_scheduler.logger.LogInfo;
import org.md2k.utilities.Report.Log;

/**
 * Created by monowar on 3/14/16.
 */
public class EndOfDayEMAScheduler extends Scheduler {
    private static final String TAG = EndOfDayEMAScheduler.class.getSimpleName();
    Handler handler;
    boolean isRunning;

    public EndOfDayEMAScheduler(Context context, EMAType emaType) throws DataKitException {
        super(context, emaType);
        Log.d(TAG, "SmokingEMAScheduler()...id=" + emaType.getId());
        handler = new Handler();
    }

    @Override
    public void start(long dayStartTimestamp, long dayEndTimestamp) throws DataKitException {
        super.start(dayStartTimestamp, dayEndTimestamp);
    }

    @Override
    public void stop() {
        Log.d(TAG, "stop()...");
        stopDelivery();
    }

    @Override
    public void setDayStartTimestamp(long dayStartTimestamp) {

    }

    @Override
    public void setDayEndTimestamp(long dayEndTimestamp) throws DataKitException {
        sendToLogInfo(LogInfo.STATUS_SCHEDULER_SCHEDULED, DateTime.getDateTime());
        conditionManager = ConditionManager.getInstance(context);
        if (conditionManager.isValid(emaType.getScheduler_rules()[0].getConditions(), emaType.getType(), emaType.getId())) {
            Log.d(TAG, "condition valid...");
            isRunning = false;
            startDelivery();

        }
    }
}