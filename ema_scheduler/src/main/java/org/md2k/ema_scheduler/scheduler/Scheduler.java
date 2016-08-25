package org.md2k.ema_scheduler.scheduler;

import android.content.Context;

import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.ema_scheduler.condition.ConditionManager;
import org.md2k.ema_scheduler.configuration.EMAType;
import org.md2k.ema_scheduler.delivery.DeliveryManager;
import org.md2k.ema_scheduler.logger.LogInfo;
import org.md2k.ema_scheduler.logger.LogSchedule;
import org.md2k.ema_scheduler.logger.LoggerManager;
import org.md2k.utilities.Report.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
abstract public class Scheduler {
    private static final String TAG = Scheduler.class.getSimpleName();
    protected Context context;
    protected EMAType emaType;
    protected ConditionManager conditionManager;
    protected long dayStartTimestamp;
    protected long dayEndTimestamp;
    LoggerManager loggerManager;
    DeliveryManager deliveryManager;
    BlockManager blockManager;

    public Scheduler(Context context, EMAType emaType, DeliveryManager deliveryManager) throws DataKitException {
        this.context=context;
        this.emaType=emaType;
        this.deliveryManager = deliveryManager;
        if (emaType != null && emaType.getBlocks() != null)
            blockManager = new BlockManager(context, emaType.getBlocks());
    }

     public void start(long dayStartTimestamp, long dayEndTimestamp) throws DataKitException {
         loggerManager=LoggerManager.getInstance(context);
         conditionManager = ConditionManager.getInstance(context);
         this.dayStartTimestamp=dayStartTimestamp;
         this.dayEndTimestamp=dayEndTimestamp;
     }

    abstract public void stop();
    abstract public void setDayStartTimestamp(long dayStartTimestamp) throws DataKitException;
    abstract public void setDayEndTimestamp(long dayEndTimestamp) throws DataKitException;

    public boolean startDelivery() throws DataKitException {
        Log.d(TAG, "startDelivery...emaType="+emaType.getType()+" emaId="+emaType.getId());
        return deliveryManager.start(emaType, true, "SYSTEM");
    }

    public void stopDelivery(){
        Log.d(TAG, "stopDelivery...");
        deliveryManager.stop();
    }
    protected void sendToLogInfo(String status, long scheduledTime) throws DataKitException {
        LogSchedule logSchedule = new LogSchedule();
        logSchedule.setScheduleTimestamp(scheduledTime);
        LogInfo logInfo = new LogInfo();
        logInfo.setId(emaType.getId());
        logInfo.setType(emaType.getType());
        logInfo.setTimestamp(DateTime.getDateTime());
        logInfo.setOperation(LogInfo.OP_SCHEDULE);
        logInfo.setStatus(status);
        logInfo.setLogSchedule(logSchedule);
        logInfo.setMessage("scheduled at: " + formatTime(scheduledTime));
        loggerManager.insert(logInfo);
    }
    String formatTime(long timestamp) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            Date currenTimeZone = calendar.getTime();
            return sdf.format(currenTimeZone);
        } catch (Exception ignored) {
        }
        return "";
    }
}
