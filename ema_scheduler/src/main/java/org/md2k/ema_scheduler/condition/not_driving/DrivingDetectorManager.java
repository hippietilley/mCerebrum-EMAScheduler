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

package org.md2k.ema_scheduler.condition.not_driving;

import android.content.Context;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.datatype.DataTypeDoubleArray;
import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.source.application.ApplicationBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.ema_scheduler.condition.Condition;
import org.md2k.ema_scheduler.configuration.ConfigCondition;
import org.md2k.utilities.data_format.DataFormat;
import org.md2k.utilities.data_format.ResultType;

import java.util.ArrayList;

/**
 * Manages the driving detector condition.
 */
public class DrivingDetectorManager extends Condition {
    /**
     * Constructor
     * @param context Android context
     */
    public DrivingDetectorManager(Context context) {
        super(context);
    }

    /**
     * Returns whether the condition is valid.
     * @param configCondition Configuration of the condition.
     * @return Whether the condition is valid.
     * @throws DataKitException
     */
    public boolean isValid(ConfigCondition configCondition) throws DataKitException {
        DataKitAPI dataKitAPI = DataKitAPI.getInstance(context);
        long lastXMinute = Long.parseLong(configCondition.getValues().get(0));
        double limitPercentage = Double.parseDouble(configCondition.getValues().get(1));
        int notDriving = 0;
        int driving = 0;
        boolean result = false;
        long curTime = DateTime.getDateTime();
        ApplicationBuilder applicationBuilder = new ApplicationBuilder().setId("org.md2k.phonesensor");
        DataSourceBuilder dataSourceBuilder = new DataSourceBuilder()
                .setType(DataSourceType.ACTIVITY_TYPE).setApplication(applicationBuilder.build());
        ArrayList<DataSourceClient> dataSourceClientArrayList = dataKitAPI.find(dataSourceBuilder);
        if (dataSourceClientArrayList.size() != 0) {
            ArrayList<DataType> dataTypes = dataKitAPI
                    .query(dataSourceClientArrayList.get(0), curTime - lastXMinute, curTime);
            long lastTimestamp = -1;
            double lastSpeed = 0.0;
            for (int i = 0; i < dataTypes.size(); i++) {
                double samples[] = ((DataTypeDoubleArray) dataTypes.get(i)).getSample();
                if(samples[DataFormat.ActivityType.Type] == ResultType.ActivityType.IN_VEHICLE) {
                    driving++;
                    if(curTime - dataTypes.get(i).getDateTime() <= 60000)
                        driving += 100;
                }
                else notDriving++;
            }
            if (dataTypes.size() == 0) {
                log(configCondition, "true: no data point found");
                return true;
            } else {
                if(driving > dataTypes.size()) driving = dataTypes.size();
                double percentage = 100.0 * ((double) driving) / ((double) (dataTypes.size()));
                if (percentage < (100 - limitPercentage)) {
                    log(configCondition, "true: not driving = " + (100-percentage) + "%% > " + limitPercentage + " %%");
                    return true;
                } else {
                    log(configCondition, "false: not driving = " + (100-percentage) + "%% < " + limitPercentage + " %%");
                    return false;
                }
            }
        }
        log(configCondition, "true: no datasource found");
        return false;
    }
}
