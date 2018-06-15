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

package org.md2k.ema_scheduler.condition.last_ema_emi;

import android.content.Context;

import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.ema_scheduler.condition.Condition;
import org.md2k.ema_scheduler.configuration.ConfigCondition;
import org.md2k.ema_scheduler.logger.LogInfo;
import org.md2k.ema_scheduler.logger.LoggerManager;

/**
 * Manages the last EMA EMI condition.
 */
public class LastEmaEmiManager extends Condition {

    /**
     * Constructor
     * @param context Android context
     */
    public LastEmaEmiManager(Context context) {
        super(context);
    }

    /**
     * Returns whether the condition is valid.
     * @param configCondition Configuration of the condition.
     * @return Whether the condition is valid.
     * @throws DataKitException
     */
    public boolean isValid(ConfigCondition configCondition) throws DataKitException {
        LoggerManager loggerManager = LoggerManager.getInstance(context);
        LogInfo logInfo = loggerManager.getLogInfoLast(LogInfo.OP_DELIVER, LogInfo.STATUS_DELIVER_SUCCESS,
                configCondition.getSource().getType(), configCondition.getSource().getId());
        if(logInfo == null) {
            log(configCondition, "true: not triggered yet");
            return true;
        }else {
            long diff = Long.parseLong(configCondition.getValues().get(0));
            long curTime = DateTime.getDateTime();
            long min = ((curTime - logInfo.getTimestamp()) / (1000*60));
            if(curTime - logInfo.getTimestamp() > diff){
                log(configCondition, "true: last triggered " + String.valueOf(min) +
                        " (require: " + String.valueOf(diff / (1000*60)) + ") minute ago");
                return true;
            }else{
                log(configCondition, "false: last triggered " + String.valueOf(min) +
                        " (require: " + String.valueOf(diff / (1000*60)) + ") minute ago");
                return false;
            }
        }
    }
}
