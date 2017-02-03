package org.md2k.ema_scheduler.delivery;

import android.content.Context;

import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.ema_scheduler.configuration.Configuration;
import org.md2k.ema_scheduler.configuration.EMAType;
import org.md2k.ema_scheduler.logger.LogInfo;
import org.md2k.ema_scheduler.logger.LoggerManager;
import org.md2k.ema_scheduler.notifier.NotifierManager;
import org.md2k.ema_scheduler.runner.RunnerManager;
import org.md2k.utilities.Report.Log;
import org.md2k.utilities.data_format.notification.NotificationResponse;

import java.util.ArrayList;
import java.util.Random;

/**
 * Copyright (c) 2016, The University of Memphis, MD2K Center
 * - Syed Monowar Hossain <monowar.hossain@gmail.com>
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * <p>
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p>
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
public class DeliveryManager {
    private static final String TAG = DeliveryManager.class.getSimpleName();
    private Context context;
    private NotifierManager notifierManager;
    private RunnerManager runnerManager;
    private boolean isRunning;

    public DeliveryManager(Context context) throws DataKitException {
        this.context = context;
        runnerManager = new RunnerManager(context, new Callback() {
            @Override
            public void onResponse(String response) {
                isRunning = false;
            }
        });
        notifierManager = new NotifierManager(context);
        isRunning = false;
    }

    public boolean start(EMAType emaType, boolean isNotifyRequired, final String type) throws DataKitException {
        if (isRunning) {
            log(LogInfo.STATUS_DELIVER_ALREADY_RUNNING, emaType, "Not started..another one is running");
            return false;
        }
        Log.d(TAG, "start()...emaType=" + emaType.getType() + " id=" + emaType.getId());
        log(LogInfo.STATUS_DELIVER_SUCCESS, emaType, type);
        if (emaType.getId().equals("EMI")) {
            emaType = findEMIType();
            logRandom(LogInfo.STATUS_DELIVER_SUCCESS, emaType, type);
            if (emaType == null) return false;
        }
        runnerManager.set(emaType.getApplication());
        Log.d(TAG, "runner=" + runnerManager);
        final EMAType finalEmaType = emaType;
        isRunning = true;
        if (isNotifyRequired) {
            notifierManager.set(emaType, new Callback() {
                @Override
                public void onResponse(String response) throws DataKitException {
                    Log.d(TAG, "callback received...response=" + response);
                    if (!response.equals(NotificationResponse.DELAY)) {
                        Log.d(TAG, "matched...runner=" + runnerManager + " response=" + response);
                        notifierManager.stop();
                        notifierManager.clear();
                        runnerManager.start(finalEmaType, response, type);
                    }
                }
            });
            notifierManager.start();
        } else {
            runnerManager.start(emaType, NotificationResponse.OK, type);
        }
        return true;
    }

    private void log(String status, EMAType emaType, String type) throws DataKitException {
        if (type.equals("SYSTEM")) {
            LogInfo logInfo = new LogInfo();
            logInfo.setOperation(LogInfo.OP_DELIVER);
            logInfo.setId(emaType.getId());
            logInfo.setType(emaType.getType());
            logInfo.setTimestamp(DateTime.getDateTime());
            logInfo.setStatus(status);
            logInfo.setMessage("trying to deliver...");
            LoggerManager.getInstance(context).insert(logInfo);
        }
    }

    private void logRandom(String status, EMAType emaType, String type) throws DataKitException {
        if (type.equals("SYSTEM")) {
            LogInfo logInfo = new LogInfo();
            logInfo.setOperation(LogInfo.OP_DELIVER);
            logInfo.setId(emaType.getId());
            logInfo.setType(emaType.getType());
            logInfo.setStatus(status);
            logInfo.setTimestamp(DateTime.getDateTime());
            logInfo.setMessage("EMI randomly selected. trying to deliver...");
            LoggerManager.getInstance(context).insert(logInfo);
        }
    }

    private EMAType findEMIType() {
        EMAType[] emaTypes = Configuration.getInstance().getEma_types();
        ArrayList<EMAType> emis = new ArrayList<>();
        for (EMAType emaType : emaTypes) {
            if (!emaType.getType().equals("EMI"))
                continue;
            if (emaType.getId().equals("EMI")) continue;
            emis.add(emaType);
        }
        if (emis.size() == 0) return null;
        Random random = new Random();
        return emis.get(random.nextInt(emis.size()));
    }

    public void stop() {
        Log.d(TAG, "stop()...");
        if (runnerManager != null)
            runnerManager.stop();
        if (notifierManager != null) {
            notifierManager.stop();
            notifierManager.clear();
        }
        isRunning = false;
    }
}
