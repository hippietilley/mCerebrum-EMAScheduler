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

package org.md2k.ema_scheduler.condition.privacy;

import android.content.Context;

import com.google.gson.Gson;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.datatype.DataTypeJSONObject;
import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.source.datasource.DataSource;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.ema_scheduler.condition.Condition;
import org.md2k.ema_scheduler.configuration.ConfigCondition;
import org.md2k.utilities.Report.Log;
import org.md2k.utilities.data_format.privacy.PrivacyData;

import java.util.ArrayList;

/**
 * Manages the privacy condition.
 */
public class PrivacyManager extends Condition{
    private static final String TAG = PrivacyManager.class.getSimpleName();

    /**
     * Constructor
     * @param context Android context
     */
    public PrivacyManager(Context context){
        super(context);
    }

    /**
     * Returns whether the condition is valid.
     * @param configCondition Configuration of the condition.
     * @return Whether the condition is valid.
     * @throws DataKitException
     */
    public boolean isValid(ConfigCondition configCondition) throws DataKitException {
        Log.d(TAG,"isValid()...");
        DataKitAPI dataKitAPI = DataKitAPI.getInstance(context);
        DataSource dataSource = configCondition.getData_source();
        DataSourceBuilder dataSourceBuilder = new DataSourceBuilder(dataSource);
        ArrayList<DataSourceClient> dataSourceClientArrayList = dataKitAPI.find(dataSourceBuilder);
        Log.d(TAG, "isValid()...find()...size=" + dataSourceClientArrayList.size());
        if (dataSourceClientArrayList.size() != 0) {
            ArrayList<DataType> dataTypes = dataKitAPI.query(dataSourceClientArrayList.get(0), 1);
            Log.d(TAG, "isValid()...dataTypes=" + dataTypes.size());
            if (dataTypes.size() == 0) {
                log(configCondition, "true: datapoint not found");
                return true;
            }
            DataTypeJSONObject dataTypeJSONObject = (DataTypeJSONObject) dataTypes.get(0);
            Gson gson = new Gson();
            PrivacyData privacyData = gson.fromJson(dataTypeJSONObject.getSample().toString(), PrivacyData.class);
            if (privacyData.isStatus() == false) {
                Log.d(TAG, "status=false");
                log(configCondition, "true: status = false");
                return true;
            }
            if(privacyData.getDuration().getValue()+privacyData.getStartTimeStamp() <= DateTime.getDateTime()) {
                Log.d(TAG,"privacytime < currenttime");
                log(configCondition, "true: privacytime (less than) currenttime");
                return true;
            }
            for(int i = 0; i < privacyData.getPrivacyTypes().size(); i++){
                if(privacyData.getPrivacyTypes().get(i).getId().equals("ema_intervention")) {
                    Log.d(TAG, "ema privacy enabled.");
                    log(configCondition, "false: ema privacy active");
                    return false;
                }
            }
            Log.d(TAG, "passed");
            log(configCondition, "true: privacy is not active");
            return true;
        } else {
            Log.d(TAG, "datasource not found");
            log(configCondition, "true: datasource not found");
            return true;
        }
    }
}
