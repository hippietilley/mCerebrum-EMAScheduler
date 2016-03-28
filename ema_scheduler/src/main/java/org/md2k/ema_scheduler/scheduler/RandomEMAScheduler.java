package org.md2k.ema_scheduler.scheduler;


import android.content.Context;
import android.os.Handler;

import org.md2k.datakitapi.time.DateTime;
import org.md2k.ema_scheduler.configuration.EMAType;
import org.md2k.ema_scheduler.configuration.SchedulerRule;
import org.md2k.ema_scheduler.day.DayManager;
import org.md2k.ema_scheduler.logger.LogInfo;
import org.md2k.ema_scheduler.logger.LogSchedule;
import org.md2k.utilities.Report.Log;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by monowar on 3/14/16.
 */
public class RandomEMAScheduler extends Scheduler {
    private static final String TAG = RandomEMAScheduler.class.getSimpleName();
    Handler handler;
    Runnable runnableDeliver = new Runnable() {
        @Override
        public void run() {
            startDelivery();
        }
    };
    Runnable runnableSchedule = new Runnable() {
        @Override
        public void run() {
            schedule();
        }
    };

    public RandomEMAScheduler(Context context, EMAType emaType, DayManager dayManager) {
        super(context, emaType, dayManager);
        Log.d(TAG, "RandomEMAScheduler()...");
        handler = new Handler();

    }

    public void start() {
        super.start();
        long triggerTime = retriveLastTriggerTime();
        if (triggerTime > 0)
            setTrigger(triggerTime);
        else {
            schedule();
        }
    }

    void setTrigger(long triggerTime) {
        //TODO: add condition
        startDelivery();
//        handler.postDelayed(runnableCondition, triggerTime);
    }

    void schedule() {
        long curTime = DateTime.getDateTime();
        int indexWindow = blockManager.getWindowIndex(curTime);
        if (indexWindow == -1) return;
        long windowStartTime = blockManager.getWindowStartTime(curTime);
        long windowEndTime = blockManager.getWindowEndTime(curTime);
        if (isDeliveredAlready(windowStartTime, windowEndTime, indexWindow)) {
            long nextWindowStartTime = blockManager.getNextWindowStartTime(curTime);
            if (nextWindowStartTime != -1)
                handler.postDelayed(runnableSchedule, nextWindowStartTime - curTime);
        } else {
            scheduleNow(windowStartTime, windowEndTime);

        }
    }

    void scheduleNow(long windowStartTime, long windowEndTime) {
        int curScheduleIndex;
        int scheduleCount = loggerManager.getLogInfos(LogInfo.OP_SCHEDULE, emaType.getType(), emaType.getId(), windowStartTime, windowEndTime).size();
        curScheduleIndex = scheduleCount;
        if (curScheduleIndex >= emaType.getScheduler_rules().length)
            curScheduleIndex = emaType.getScheduler_rules().length - 1;
        switch (emaType.getScheduler_rules()[curScheduleIndex].getType()) {
            case SchedulerRule.TYPE_RANDOM:
                long startTimestamp = getTimeFromType(emaType.getScheduler_rules()[curScheduleIndex].getStart_time());
                long endTimestamp = getTimeFromType(emaType.getScheduler_rules()[curScheduleIndex].getEnd_time());
                long scheduledTime = startTimestamp + getRandomNumber((endTimestamp - startTimestamp) / emaType.getScheduler_rules()[curScheduleIndex].getDivide());
                sendToLogInfo(scheduledTime);
                if (scheduledTime > DateTime.getDateTime()) {
                    handler.removeCallbacks(runnableSchedule);
                    handler.removeCallbacks(runnableDeliver);
                    handler.postDelayed(runnableDeliver, scheduledTime - DateTime.getDateTime());
                }else{
                    handler.removeCallbacks(runnableSchedule);
                    handler.removeCallbacks(runnableDeliver);
                    handler.post(runnableSchedule);
                }
                break;
            case SchedulerRule.TYPE_IMMEDIATE:
                sendToLogInfo(DateTime.getDateTime()+60000);
                handler.removeCallbacks(runnableSchedule);
                handler.removeCallbacks(runnableDeliver);
                handler.postDelayed(runnableDeliver, 60000);
                break;
        }
    }

    void sendToLogInfo(long scheduledTime) {
        LogSchedule logSchedule = new LogSchedule();
        logSchedule.setScheduleTimestamp(scheduledTime);
        LogInfo logInfo = new LogInfo();
        logInfo.setId(emaType.getId());
        logInfo.setType(emaType.getType());
        logInfo.setTimestamp(DateTime.getDateTime());
        logInfo.setOperation(LogInfo.OP_SCHEDULE);
        logInfo.setLogSchedule(logSchedule);
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
                return blockManager.getWindowStartTime(DateTime.getDateTime());
            case SchedulerRule.TIME_BLOCK_END:
                return blockManager.getWindowEndTime(DateTime.getDateTime());
            case SchedulerRule.TIME_LAST_SCHEDULE:
                long lastScheduleTime = -1;
                ArrayList<LogInfo> logInfos = loggerManager.getLogInfos(LogInfo.OP_SCHEDULE, emaType.getType(), emaType.getId());
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
        ArrayList<LogInfo> logInfoArrayList = loggerManager.getLogInfos(LogInfo.OP_DELIVER, emaType.getType(), emaType.getId(), startTime, endTime);
        if (logInfoArrayList.size() > blockManager.getWindows()[indexWindow].getTotal())
            return false;
        else return true;
    }

    @Override
    public void stop() {

    }

    @Override
    public void reset() {
        stop();
        start();
    }

    long retriveLastTriggerTime() {
        long curTimestamp = DateTime.getDateTime();
        ArrayList<LogInfo> logInfoArrayList = loggerManager.getLogInfos(LogInfo.OP_SCHEDULE, emaType.getType(), emaType.getId());
        for (int i = 0; i < logInfoArrayList.size(); i++) {
            if (logInfoArrayList.get(i).getLogSchedule().getScheduleTimestamp() > curTimestamp)
                return logInfoArrayList.get(i).getLogSchedule().getScheduleTimestamp() - curTimestamp;
        }
        return -1;
    }
}
