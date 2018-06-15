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

package org.md2k.ema_scheduler.condition.data_quality;

import android.content.Context;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.datatype.DataTypeLong;
import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.ema_scheduler.condition.Condition;
import org.md2k.ema_scheduler.configuration.ConfigCondition;
import org.md2k.ema_scheduler.logger.LogInfo;
import org.md2k.ema_scheduler.logger.LoggerDataQuality;
import org.md2k.ema_scheduler.logger.LoggerManager;

import java.util.ArrayList;

/**
 * Manages the data quality condition.
 */
public class DataQualityManager extends Condition {
    public static final String TAG = DataQualityManager.class.getSimpleName();
    private static final String DAY_START = "DAY_START";
    private static final String LAST_EMA = "LAST_EMA";

    /**
     * Constructor
     * @param context Android context
     */
    public DataQualityManager(Context context) {
        super(context);
    }

    /**
     * Returns whether the condition is valid.
     * @param configCondition Configuration of the condition.
     * @return Whether the condition is valid.
     * @throws DataKitException
     */
    public boolean isValid(ConfigCondition configCondition) throws DataKitException {
        long startTimeStamp = getStartTimeStamp(configCondition);
        if (startTimeStamp == -1)
            return false;
        double limitPercentage = Double.parseDouble(configCondition.getValues().get(1));
        double percentage = LoggerDataQuality.getInstance(context).getQuality(startTimeStamp, DateTime.getDateTime());
        if (percentage >= limitPercentage) {
            log(configCondition, "true: good_quality:" + String.valueOf(percentage));
            return true;
        } else {
            log(configCondition, "false: good_quality:" + String.valueOf(percentage));
            return false;
        }
    }

    /**
     * Returns the percentage.
     * @param configCondition Configuration of the condition.
     * @return The percentage.
     * @throws DataKitException
     */
    public double getPercentage(ConfigCondition configCondition) throws DataKitException {
        long startTimeStamp = getStartTimeStamp(configCondition);
        if (startTimeStamp == -1)
            return 0;
        return LoggerDataQuality.getInstance(context).getQuality(startTimeStamp, DateTime.getDateTime());
    }

    /**
     * Returns the starting timestamp.
     * @param configCondition Configuration of the condition.
     * @return The starting timestamp.
     * @throws DataKitException
     */
    private long getStartTimeStamp(ConfigCondition configCondition) throws DataKitException {
        long startTimestamp;
        if (configCondition.getValues().get(0).equals(DAY_START)) {
            startTimestamp = getDay(DataSourceType.DAY_START);
        } else if (configCondition.getValues().get(0).equals(LAST_EMA)) {
            startTimestamp = getLastEMA();
            if (startTimestamp < 0)
                startTimestamp = getDay(DataSourceType.DAY_START);
        } else startTimestamp = DateTime.getDateTime() - Long.parseLong(configCondition.getValues().get(0));
        return startTimestamp;
    }

    /**
     * Returns the timestamp of the last EMA.
     * @return The timestamp of the last EMA.
     */
    private long getLastEMA() {
        LogInfo logInfo = LoggerManager.getInstance(context)
                .getLogInfoLast(LogInfo.OP_DELIVER, LogInfo.STATUS_DELIVER_SUCCESS, null, null);
        if (logInfo == null)
            return -1;
        return logInfo.getTimestamp();
    }

    /**
     * Returns the day for the given data source type.
     * @param dataSourceType Data source type.
     * @return The day.
     * @throws DataKitException
     */
    private long getDay(String dataSourceType) throws DataKitException {
        long day = -1;
        DataKitAPI dataKitAPI = DataKitAPI.getInstance(context);
        DataSourceBuilder dataSourceBuilder = new DataSourceBuilder().setType(dataSourceType);
        ArrayList<DataSourceClient> dataSourceClients = dataKitAPI.find(dataSourceBuilder);
        if (dataSourceClients.size() == 0) {
            return day;
        }
        ArrayList<DataType> dataTypes = dataKitAPI.query(dataSourceClients.get(0), 1);
        if (dataTypes.size() == 0)
            return day;
        return ((DataTypeLong) dataTypes.get(0)).getSample();
    }
}
