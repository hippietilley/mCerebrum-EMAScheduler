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

package org.md2k.ema_scheduler.condition.battery_level;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.ema_scheduler.condition.Condition;
import org.md2k.ema_scheduler.configuration.ConfigCondition;

/**
 *
 */
public class BatteryLevelManager extends Condition {
    /**
     * Constructor
     * @param context Android context
     */
    public BatteryLevelManager(Context context) {
        super(context);
    }

    /**
     * Returns whether the battery level is a valid condition.
     * @param configCondition Configuration of the condition.
     * @return Whether the battery level is a valid condition.
     * @throws DataKitException
     */
    public boolean isValid(ConfigCondition configCondition) throws DataKitException {
        double limitPercentage = Double.parseDouble(configCondition.getValues().get(0));
        double percentage = getBatteryLevel();
        if (percentage > limitPercentage) {
            log(configCondition, "true: " + percentage + " > " + limitPercentage);
            return true;
        } else {
            log(configCondition, "false: " + percentage + " < " + limitPercentage);
            return false;
        }
    }

    /**
     * Returns the current battery level as a percentage of charge left.
     * @return The current battery level as a percentage of charge left.
     */
    private double getBatteryLevel() {
        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent intent = context.registerReceiver(null, iFilter);
        assert intent != null;
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
        float percentage;
        if (level == -1 || scale == -1) {
            percentage = 0.0f;
        } else {
            percentage = ((float) level / (float) scale) * 100.0f;
        }
        return percentage;
    }
}
