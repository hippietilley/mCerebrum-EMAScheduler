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

package org.md2k.ema_scheduler.condition;

import android.content.Context;

import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.ema_scheduler.configuration.ConfigCondition;
import org.md2k.ema_scheduler.logger.LogInfo;
import org.md2k.ema_scheduler.logger.LoggerManager;

/**
 * Defines a condition.
 */
public abstract class Condition {
    private static final String TAG = Condition.class.getSimpleName();
    protected Context context;

    /**
     * Constructor
     * @param context Android context
     */
    protected Condition(Context context){
        this.context = context;
    }

    /**
     * Returns whether the condition is valid.
     * @param configCondition Configuration of the condition.
     * @return Whether the condition is valid.
     * @throws DataKitException
     */
    public abstract boolean isValid(ConfigCondition configCondition) throws DataKitException;

    /**
     * Logs the given message for the given condition configuration.
     * @param configCondition  Configuration of the condition.
     * @param message Message to log.
     * @throws DataKitException
     */
    protected void log(ConfigCondition configCondition, String message) throws DataKitException {
        LogInfo logInfo = new LogInfo();
        logInfo.setOperation(LogInfo.OP_CONDITION);
        logInfo.setId(configCondition.getId());
        logInfo.setType(configCondition.getType());
        logInfo.setTimestamp(DateTime.getDateTime());
        logInfo.setMessage(message);
        LoggerManager.getInstance(context).insert(logInfo);
    }
}
