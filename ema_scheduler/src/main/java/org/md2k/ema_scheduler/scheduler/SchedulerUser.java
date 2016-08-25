package org.md2k.ema_scheduler.scheduler;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.ema_scheduler.Constants;
import org.md2k.ema_scheduler.configuration.EMAType;
import org.md2k.ema_scheduler.delivery.DeliveryManager;

/**
 * Copyright (c) 2016, The University of Memphis, MD2K Center
 * - Syed Monowar Hossain <monowar.hossain@gmail.com>
 * All rights reserved.
 * <p/>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p/>
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * <p/>
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p/>
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
public class SchedulerUser extends Scheduler {
    private static final String TAG = SchedulerUser.class.getSimpleName();
    private BroadcastReceiver messageReceiverTest = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            EMAType emaType = (EMAType) intent.getSerializableExtra(EMAType.class.getSimpleName());
            String type = intent.getStringExtra("type");
            boolean isNotify = intent.getBooleanExtra("is_notify", false);
            try {
                deliveryManager.start(emaType, isNotify, type);
            } catch (DataKitException ignored) {
            }
        }
    };

    public SchedulerUser(Context context, EMAType emaType, DeliveryManager deliveryManager) throws DataKitException {
        super(context, emaType, deliveryManager);
        LocalBroadcastManager.getInstance(context.getApplicationContext()).registerReceiver(messageReceiverTest, new IntentFilter(Constants.INTENT_USER));
    }

    @Override
    public void stop() {
        LocalBroadcastManager.getInstance(context.getApplicationContext()).unregisterReceiver(messageReceiverTest);
        stopDelivery();
    }

    @Override
    public void setDayStartTimestamp(long dayStartTimestamp) throws DataKitException {

    }

    @Override
    public void setDayEndTimestamp(long dayEndTimestamp) throws DataKitException {

    }

}
