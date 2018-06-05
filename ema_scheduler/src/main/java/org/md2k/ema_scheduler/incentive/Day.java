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

package org.md2k.ema_scheduler.incentive;

import android.content.Context;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.datatype.DataTypeLong;
import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.utilities.Report.Log;

import java.util.ArrayList;

/**
 * Provides methods for getting start and end times for the day.
 */
class Day {
    private static final String TAG = Day.class.getSimpleName();
    private Context context;

    /**
     * Constructor
     * @param context Android context
     */
    private Day(Context context){
        this.context = context;
    }

    /**
     * Returns the <code>dayStartTime</code> from <code>DataKit</code>.
     * @return The <code>dayStartTime</code>.
     * @throws DataKitException
     */
    public long readDayStartFromDataKit() throws DataKitException {
        long dayStartTime;
        DataKitAPI dataKitAPI = DataKitAPI.getInstance(context);
        Log.d(TAG, "readDayStartFromDataKit()...");
        ArrayList<DataSourceClient> dataSourceClients;
        dayStartTime = -1;
        dataSourceClients = dataKitAPI.find(new DataSourceBuilder().setType(DataSourceType.DAY_START));
        Log.d(TAG,"readDayStartFromDataKit()...find..dataSourceClient.size()=" + dataSourceClients.size());
        if (dataSourceClients.size() > 0) {
            ArrayList<DataType> dataTypes = dataKitAPI.query(dataSourceClients.get(0), 1);
            if (dataTypes.size() != 0) {
                DataTypeLong dataTypeLong = (DataTypeLong) dataTypes.get(0);
                dayStartTime = dataTypeLong.getSample();
            }
        }
        return dayStartTime;
    }

    /**
     * Returns the <code>dayEndTime</code> from <code>DataKit</code>.
     * @return The <code>dayEndTime</code>.
     * @throws DataKitException
     */
    public long readDayEndFromDataKit() throws DataKitException {
        long dayEndTime=-1;
        DataKitAPI dataKitAPI=DataKitAPI.getInstance(context);
        ArrayList<DataSourceClient> dataSourceClients;
        dataSourceClients = dataKitAPI.find(new DataSourceBuilder().setType(DataSourceType.DAY_END));
        Log.d(TAG,"readDayEndFromDataKit()...find..dataSourceClient.size()="+dataSourceClients.size());
        if (dataSourceClients.size() > 0) {
            ArrayList<DataType> dataTypes = dataKitAPI.query(dataSourceClients.get(0), 1);
            if (dataTypes.size() != 0) {
                DataTypeLong dataTypeLong = (DataTypeLong) dataTypes.get(0);
                dayEndTime = dataTypeLong.getSample();
                Log.d(TAG,"readDayEndFromDataKit()...dayEndTime="+dayEndTime);
            }
        }
        return dayEndTime;
    }
}
