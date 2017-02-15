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
import org.md2k.ema_scheduler.delivery.DeliveryManager;
import org.md2k.ema_scheduler.logger.LogInfo;
import org.md2k.utilities.Report.Log;

import java.util.ArrayList;
import java.util.Random;

/**
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
public class RandomEMAScheduler extends Scheduler {
    private static final String TAG = RandomEMAScheduler.class.getSimpleName();
    private Handler handler;
    private Runnable runnableSchedule = new Runnable() {
        @Override
        public void run() {
            try {
                schedule();
            } catch (DataKitException e) {
                Log.d(TAG, "DataKitException...schedule()..");
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ServiceEMAScheduler.BROADCAST_MSG));
            }
        }
    };
    private Runnable runnableDeliver = new Runnable() {
        @Override
        public void run() {
            long curTime = DateTime.getDateTime();
            long blockStartTime = blockManager.getBlockStartTime(dayStartTimestamp, curTime);
            long blockEndTime = blockManager.getBlockEndTime(dayStartTimestamp, curTime);
            int curScheduleIndex = getCurScheduleIndex(blockStartTime, blockEndTime);
            try {
                if (conditionManager.isValid(emaType.getScheduler_rules()[curScheduleIndex].getConditions(), emaType.getType(), emaType.getId()))
                    startDelivery();
                schedule();
            } catch (DataKitException e) {
                Log.d(TAG, "DataKitException...schedule()");
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ServiceEMAScheduler.BROADCAST_MSG));

            }
        }
    };

    public RandomEMAScheduler(Context context, EMAType emaType, DeliveryManager deliveryManager) throws DataKitException {
        super(context, emaType, deliveryManager);
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

    private void schedule() throws DataKitException {
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
//                logWhenSchedulerRun(LogInfo.STATUS_SCHEDULER_ALREADY_DELIVERED, "schedule()...alreadyDelivered, next call="+formatTime(nextWindowStartTime));
                handler.postDelayed(runnableSchedule, nextWindowStartTime - curTime);
            }else{
//                logWhenSchedulerRun(LogInfo.STATUS_SCHEDULER_ALREADY_DELIVERED, "schedule()...alreadyDelivered, no valid block after this");
            }
        } else {
            scheduleNow(blockStartTime, blockEndTime);
        }
    }

    private int getCurScheduleIndex(long blockStartTime, long blockEndTime){
        int curScheduleIndex = loggerManager.getLogInfos(LogInfo.OP_SCHEDULE, LogInfo.STATUS_SCHEDULER_SCHEDULED, emaType.getType(), emaType.getId(), blockStartTime, blockEndTime).size();
        if (curScheduleIndex >= emaType.getScheduler_rules().length)
            curScheduleIndex = emaType.getScheduler_rules().length - 1;
        return curScheduleIndex;
    }

    private void scheduleNow(long blockStartTime, long blockEndTime) throws DataKitException {
        int curScheduleIndex= getCurScheduleIndex(blockStartTime, blockEndTime);
        long curTime=DateTime.getDateTime();
        handler.removeCallbacks(runnableSchedule);
        handler.removeCallbacks(runnableDeliver);
        switch (emaType.getScheduler_rules()[curScheduleIndex].getType()) {
            case SchedulerRule.TYPE_RANDOM:
                long startTimestamp = getTimeFromType(emaType.getScheduler_rules()[curScheduleIndex].getStart_time());
                long endTimestamp = getTimeFromType(emaType.getScheduler_rules()[curScheduleIndex].getEnd_time());
                long scheduledTime = startTimestamp + getRandomNumber((endTimestamp - startTimestamp) / emaType.getScheduler_rules()[curScheduleIndex].getDivide());
                sendToLogInfo(LogInfo.STATUS_SCHEDULER_SCHEDULED, scheduledTime);
                if (scheduledTime > curTime) {
                    handler.postDelayed(runnableDeliver, scheduledTime - curTime);
                }else{
                    schedule();
                }
                break;
            case SchedulerRule.TYPE_IMMEDIATE:
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

    private void logWhenSchedulerRun(String status, String message) throws DataKitException {
        LogInfo logInfo = new LogInfo();
        logInfo.setId(emaType.getId());
        logInfo.setType(emaType.getType());
        logInfo.setTimestamp(DateTime.getDateTime());
        logInfo.setOperation(LogInfo.OP_SCHEDULE);
        logInfo.setStatus(status);
        logInfo.setMessage(message);
        loggerManager.insert(logInfo);
    }


    private long getRandomNumber(long range) {
        int intRange = (int) (range / (1000 * 60));
        Random random = new Random();
        return ((long) random.nextInt(intRange)) * (1000 * 60);
    }

    private long getTimeFromType(String type) {
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

    private boolean isDeliveredAlready(long startTime, long endTime, int indexWindow) {
        ArrayList<LogInfo> logInfoArrayList = loggerManager.getLogInfos(LogInfo.OP_DELIVER, LogInfo.STATUS_DELIVER_SUCCESS,  emaType.getType(), emaType.getId(), startTime, endTime);
        return logInfoArrayList.size() >= blockManager.getBlocks()[indexWindow].getTotal();
    }

    @Override
    public void stop() {
        handler.removeCallbacks(runnableDeliver);
        handler.removeCallbacks(runnableSchedule);
        stopDelivery();
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


    private long retrieveLastTriggerTime() {
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
