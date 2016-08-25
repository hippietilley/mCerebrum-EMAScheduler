package org.md2k.ema_scheduler.scheduler;

import android.content.Context;

import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.ema_scheduler.configuration.Configuration;
import org.md2k.ema_scheduler.configuration.EMAType;
import org.md2k.ema_scheduler.delivery.DeliveryManager;
import org.md2k.utilities.Report.Log;

import java.util.ArrayList;

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
public class SchedulerManager {
    private static final String TAG = SchedulerManager.class.getSimpleName();
    Context context;
    Configuration configuration;
    ArrayList<Scheduler> scheduler;
    DeliveryManager deliveryManager;

    boolean isStarted;

    public SchedulerManager(Context context) throws DataKitException {
        Log.d(TAG, "SchedulerManager()...");
        this.context = context;
        configuration = Configuration.getInstance();
        scheduler = new ArrayList<>();
        isStarted = false;
        deliveryManager = new DeliveryManager(context);
        prepareScheduler();
    }

    private void prepareScheduler() throws DataKitException {
        Log.d(TAG, "prepareScheduler()...");
        for (int i = 0; i < configuration.getEma_types().length; i++) {
            Log.d(TAG, "prepareScheduler()...emaType ID=" + configuration.getEma_types()[i].getId());
            if (configuration.getEma_types()[i].getId() == null) continue;
            if (!configuration.getEma_types()[i].isEnable()) continue;
            Log.d(TAG, "prepareScheduler()...emaType ID=" + configuration.getEma_types()[i].getId()+"....success");
            switch (configuration.getEma_types()[i].getId()) {
                case EMAType.ID_RANDOM_EMA:
                    scheduler.add(new RandomEMAScheduler(context, configuration.getEma_types()[i], deliveryManager));
                    break;
                case EMAType.ID_EMI:
                    scheduler.add(new EMIScheduler(context, configuration.getEma_types()[i], deliveryManager));
                    break;
                case EMAType.ID_END_OF_DAY_EMA:
                    scheduler.add(new EndOfDayEMAScheduler(context, configuration.getEma_types()[i], deliveryManager));
                    break;
                case EMAType.ID_SMOKING_EMA:
                    scheduler.add(new SmokingEMAScheduler(context, configuration.getEma_types()[i], deliveryManager));
                    break;
                case EMAType.ID_STRESS_EMA:
                    scheduler.add(new StressEMAScheduler(context, configuration.getEma_types()[i], deliveryManager));
                    break;
                default:
                    break;
            }
            scheduler.add(new SchedulerUser(context, null, deliveryManager));
        }
    }
    public void setDayStartTimestamp(long dayStartTimestamp) throws DataKitException {
        for (int i = 0; i < scheduler.size(); i++)
            scheduler.get(i).setDayStartTimestamp(dayStartTimestamp);
    }
    public void setDayEndTimestamp(long dayEndTimestamp) throws DataKitException {
        for (int i = 0; i < scheduler.size(); i++)
            scheduler.get(i).setDayEndTimestamp(dayEndTimestamp);

    }

    public void start(long dayStartTimestamp, long dayEndTimestamp) throws DataKitException {
        if (isStarted) return;
        isStarted = true;
        Log.d(TAG, "start()...");
        for (int i = 0; i < scheduler.size(); i++)
            scheduler.get(i).start(dayStartTimestamp, dayEndTimestamp);
    }

    public void stop() {
        if (!isStarted) return;
        isStarted = false;
        Log.d(TAG, "stop()...");
        for (int i = 0; i < scheduler.size(); i++)
            scheduler.get(i).stop();
    }
}
