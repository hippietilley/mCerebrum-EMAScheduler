package org.md2k.ema_scheduler.scheduler;


import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.ema_scheduler.ServiceEMAScheduler;
import org.md2k.ema_scheduler.configuration.EMAType;
import org.md2k.ema_scheduler.configuration.SchedulerRule;
import org.md2k.ema_scheduler.logger.LogInfo;
import org.md2k.utilities.Report.Log;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by monowar on 3/14/16.
 */
public class RandomEMAScheduler extends Scheduler {
    private static final String TAG = RandomEMAScheduler.class.getSimpleName();
    Handler handler;
    public RandomEMAScheduler(Context context, EMAType emaType) throws DataKitException {
        super(context, emaType);
        handler = new Handler();
    }

    public void start(long dayStartTimestamp, long dayEndTimestamp) throws DataKitException {
        super.start(dayStartTimestamp, dayEndTimestamp);
        handler.removeCallbacks(runnableSchedule);
        handler.removeCallbacks(runnableDeliver);
        Log.d(TAG, "RandomEMA start..."+dayStartTimestamp+" "+dayEndTimestamp);
        if(dayStartTimestamp==-1 || dayStartTimestamp<dayEndTimestamp) return;
        long triggerTime = retrieveLastTriggerTime();
        Log.d(TAG, "RandomEMA start..."+triggerTime);
        if (triggerTime > 0) {
            handler.postDelayed(runnableDeliver, triggerTime);
        }
        else {
            schedule();
        }
    }

    void schedule() throws DataKitException {
        Log.d(TAG,"schedule()...");
        handler.removeCallbacks(runnableSchedule);
        long curTime = DateTime.getDateTime();
        int indexBlock = blockManager.getBlockIndex(dayStartTimestamp, curTime);
        Log.d(TAG,"schedule()...indexBlock="+indexBlock);
        if (indexBlock == -1) {
            logWhenSchedulerRun(LogInfo.STATUS_SCHEDULER_NO_VALID_BLOCK, "schedule()...no valid block");
            return;
        }
        long blockStartTime = blockManager.getBlockStartTime(dayStartTimestamp, curTime);
        long blockEndTime = blockManager.getBlockEndTime(dayStartTimestamp, curTime);
        if (isDeliveredAlready(blockStartTime, blockEndTime, indexBlock)) {
            Log.d(TAG,"schedule()...already delivered in this block");
            long nextWindowStartTime = blockManager.getNextBlockStartTime(dayStartTimestamp, curTime);
            Log.d(TAG,"schedule()...next block"+nextWindowStartTime);
            if (nextWindowStartTime != -1) {
                logWhenSchedulerRun(LogInfo.STATUS_SCHEDULER_ALREADY_DELIVERED, "schedule()...alreadyDelivered, next call="+formatTime(nextWindowStartTime));
                handler.postDelayed(runnableSchedule, nextWindowStartTime - curTime);
            }else{
                logWhenSchedulerRun(LogInfo.STATUS_SCHEDULER_ALREADY_DELIVERED, "schedule()...alreadyDelivered, no valid block after this");
            }
        } else {
            scheduleNow(blockStartTime, blockEndTime);
        }
    }
    int getCurScheduleIndex(long blockStartTime, long blockEndTime){
        int curScheduleIndex = loggerManager.getLogInfos(LogInfo.OP_SCHEDULE, LogInfo.STATUS_SCHEDULER_SCHEDULED, emaType.getType(), emaType.getId(), blockStartTime, blockEndTime).size();
        if (curScheduleIndex >= emaType.getScheduler_rules().length)
            curScheduleIndex = emaType.getScheduler_rules().length - 1;
        return curScheduleIndex;
    }

    void scheduleNow(long blockStartTime, long blockEndTime) throws DataKitException {
        int curScheduleIndex= getCurScheduleIndex(blockStartTime, blockEndTime);
        long curTime=DateTime.getDateTime();
        switch (emaType.getScheduler_rules()[curScheduleIndex].getType()) {
            case SchedulerRule.TYPE_RANDOM:
                long startTimestamp = getTimeFromType(emaType.getScheduler_rules()[curScheduleIndex].getStart_time());
                long endTimestamp = getTimeFromType(emaType.getScheduler_rules()[curScheduleIndex].getEnd_time());
                long scheduledTime = startTimestamp + getRandomNumber((endTimestamp - startTimestamp) / emaType.getScheduler_rules()[curScheduleIndex].getDivide());
                sendToLogInfo(LogInfo.STATUS_SCHEDULER_SCHEDULED, scheduledTime);
                if (scheduledTime > curTime) {
                    handler.removeCallbacks(runnableSchedule);
                    handler.removeCallbacks(runnableDeliver);
                    handler.postDelayed(runnableDeliver, scheduledTime - curTime);
                }else{
                    handler.removeCallbacks(runnableSchedule);
                    handler.removeCallbacks(runnableDeliver);
                    schedule();
                }
                break;
            case SchedulerRule.TYPE_IMMEDIATE:
                handler.removeCallbacks(runnableSchedule);
                handler.removeCallbacks(runnableDeliver);
                if (curTime + 60000 > blockEndTime) {
                    logWhenSchedulerRun(LogInfo.STATUS_SCHEDULER_NO_TIME_LEFT, "schedule()...not possible to send EMA in this block, next call=" + formatTime(curTime+60000));
                    handler.postDelayed(runnableSchedule, 60000);
                }else {
                    sendToLogInfo(LogInfo.STATUS_SCHEDULER_SCHEDULED, curTime + 60000);
                    handler.postDelayed(runnableDeliver, 60000);
                }
                break;
        }
    }
    Runnable runnableDeliver = new Runnable() {
        @Override
        public void run() {
            long curTime=DateTime.getDateTime();
            long blockStartTime = blockManager.getBlockStartTime(dayStartTimestamp, curTime);
            long blockEndTime = blockManager.getBlockEndTime(dayStartTimestamp, curTime);
            int curScheduleIndex= getCurScheduleIndex(blockStartTime, blockEndTime);
            try {
                if (conditionManager.isValid(emaType.getScheduler_rules()[curScheduleIndex].getConditions(), emaType.getType(), emaType.getId()))
                    startDelivery();
                schedule();
            } catch (DataKitException e) {
                Log.d(TAG,"DataKitException...schedule()");
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ServiceEMAScheduler.BROADCAST_MSG));

            }
        }
    };
    Runnable runnableSchedule = new Runnable() {
        @Override
        public void run() {
            try {
                schedule();
            } catch (DataKitException e) {
                Log.d(TAG,"DataKitException...schedule()..");
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ServiceEMAScheduler.BROADCAST_MSG));
            }
        }
    };


    void logWhenSchedulerRun(String status, String message) throws DataKitException {
        LogInfo logInfo = new LogInfo();
        logInfo.setId(emaType.getId());
        logInfo.setType(emaType.getType());
        logInfo.setTimestamp(DateTime.getDateTime());
        logInfo.setOperation(LogInfo.OP_SCHEDULE);
        logInfo.setStatus(status);
        logInfo.setMessage(message);
        loggerManager.insert(logInfo);
    }


    long getRandomNumber(long range) {
        int intRange = (int) (range / (1000 * 60));
        Random random = new Random();
        return ((long) random.nextInt(intRange)) * (1000 * 60);
    }

    long getTimeFromType(String type) {
        switch (type) {
            case SchedulerRule.TIME_BLOCK_START:
                return blockManager.getBlockStartTime(dayStartTimestamp, DateTime.getDateTime());
            case SchedulerRule.TIME_BLOCK_END:
                return blockManager.getBlockEndTime(dayStartTimestamp, DateTime.getDateTime());
            case SchedulerRule.TIME_LAST_SCHEDULE:
                long lastScheduleTime = -1;
                ArrayList<LogInfo> logInfos = loggerManager.getLogInfos(LogInfo.OP_SCHEDULE, LogInfo.STATUS_SCHEDULER_SCHEDULED, emaType.getType(), emaType.getId());
                for (int i = 0; i < logInfos.size(); i++) {
                    if (logInfos.get(i).getLogSchedule().getScheduleTimestamp() > lastScheduleTime) {
                        lastScheduleTime = logInfos.get(i).getLogSchedule().getScheduleTimestamp();
                    }
                }
                return lastScheduleTime;
        }
        return -1;
    }

    boolean isDeliveredAlready(long startTime, long endTime, int indexWindow) {
        ArrayList<LogInfo> logInfoArrayList = loggerManager.getLogInfos(LogInfo.OP_DELIVER, LogInfo.STATUS_DELIVER_SUCCESS,  emaType.getType(), emaType.getId(), startTime, endTime);
        if (logInfoArrayList.size() >= blockManager.getBlocks()[indexWindow].getTotal())
            return true;
        else return false;
    }

    @Override
    public void stop() {
        handler.removeCallbacks(runnableDeliver);
        handler.removeCallbacks(runnableSchedule);
    }

    @Override
    public void setDayStartTimestamp(long dayStartTimestamp) throws DataKitException {
        stop();
        this.dayStartTimestamp=dayStartTimestamp;
        schedule();

    }

    @Override
    public void setDayEndTimestamp(long dayEndTimestamp) {
        this.dayEndTimestamp=dayEndTimestamp;
        stop();
    }


    long retrieveLastTriggerTime() {
        long curTimestamp = DateTime.getDateTime();
        ArrayList<LogInfo> logInfoArrayList = loggerManager.getLogInfos(LogInfo.OP_SCHEDULE, LogInfo.STATUS_SCHEDULER_SCHEDULED, emaType.getType(), emaType.getId());
        for (int i = 0; i < logInfoArrayList.size(); i++) {
            if(logInfoArrayList.get(i).getLogSchedule()==null) continue;
            if (logInfoArrayList.get(i).getLogSchedule().getScheduleTimestamp() > curTimestamp)
                return logInfoArrayList.get(i).getLogSchedule().getScheduleTimestamp() - curTimestamp;
        }
        return -1;
    }
}
