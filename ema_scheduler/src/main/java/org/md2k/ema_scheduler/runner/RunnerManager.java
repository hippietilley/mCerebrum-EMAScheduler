/*
 * Copyright (c) 2018, The University of Memphis, MD2K Center of Excellence
 *
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

package org.md2k.ema_scheduler.runner;

import android.content.Context;

import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.ema_scheduler.configuration.Application;
import org.md2k.ema_scheduler.configuration.EMAType;
import org.md2k.ema_scheduler.delivery.Callback;
import org.md2k.utilities.Report.Log;

/**
 * Mannages the starting and stoping of a runner via <code>RunnerMonitor</code>.
 */
public class RunnerManager {
    private static final String TAG = RunnerManager.class.getSimpleName();
    private Context context;
    private RunnerMonitor runnerMonitor;
    private Application application;

    /**
     * Constructor
     * @param context Android context
     * @param callback Callback interface
     * @throws DataKitException
     */
    public RunnerManager(Context context, Callback callback) throws DataKitException {
        this.context = context;
        runnerMonitor = new RunnerMonitor(context, callback);
    }

    /**
     * Sets the application.
     * @param application Application to set.
     */
    public void set(Application application){
        this.application = application;
    }

    /**
     * Starts the runner.
     * @param emaType Type of EMA.
     * @param notificationResponse User response to the notification.
     * @param type EMA trigger type.
     * @throws DataKitException
     */
    public void start(EMAType emaType, String notificationResponse, String type) throws DataKitException {
        Log.d(TAG, "start()...status=" + notificationResponse + " filename=" + application.getId());
        runnerMonitor.start(emaType, notificationResponse, application, type);
    }

    /**
     * Stops the runner.
     */
    public void stop(){
        runnerMonitor.clear();
    }
}

