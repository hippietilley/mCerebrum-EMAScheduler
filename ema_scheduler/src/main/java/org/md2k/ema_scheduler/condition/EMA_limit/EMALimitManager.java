package org.md2k.ema_scheduler.condition.EMA_limit;

import android.content.Context;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.datatype.DataTypeDouble;
import org.md2k.datakitapi.datatype.DataTypeLong;
import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.source.datasource.DataSource;
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
public class EMALimitManager extends Condition {
    public static final String TAG = EMALimitManager.class.getSimpleName();

    public EMALimitManager(Context context) {
        super(context);
    }

    public boolean isValid(ConfigCondition configCondition) throws DataKitException {
        long curTime= DateTime.getDateTime();
        long prevTime= 0;
//        int sampleNo = (Integer.parseInt(configCondition.getValues().get(0))) / 60000;
        int value = Integer.parseInt(configCondition.getValues().get(0));
        DataKitAPI dataKitAPI=DataKitAPI.getInstance(context);
        DataSource dataSource = configCondition.getData_source();
        DataSourceBuilder dataSourceBuilder = new DataSourceBuilder(dataSource);
        ArrayList<DataSourceClient> dataSourceClientArrayList = dataKitAPI.find(dataSourceBuilder);
        if (dataSourceClientArrayList.size() != 0) {
            ArrayList<DataType> dataTypes = dataKitAPI.query(dataSourceClientArrayList.get(0), prevTime, curTime);
            if (dataTypes.size() <value) {
                log(configCondition, "true: no. of EMA triggered: "+String.valueOf(dataTypes.size())+"< no. of EMA expected: "+String.valueOf(value));
                return true;
            }else{
                log(configCondition, "false: no. of EMA triggered: "+String.valueOf(dataTypes.size())+">= no. of EMA expected: "+String.valueOf(value));
                return false;
            }

        } else {
            log(configCondition, "true: datasource not found");
            return true;
        }
    }
}
