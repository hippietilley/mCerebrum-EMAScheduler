package org.md2k.ema_scheduler.logger;

import android.content.Context;
import android.os.Handler;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.datatype.DataTypeInt;
import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.ema_scheduler.condition.ConditionManager;
import org.md2k.ema_scheduler.configuration.ConfigCondition;
import org.md2k.ema_scheduler.configuration.Configuration;
import org.md2k.utilities.Report.Log;
import org.md2k.utilities.data_format.DATA_QUALITY;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

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
public class LoggerDataQuality {
    private static final String TAG = LoggerDataQuality.class.getSimpleName();
    private static final long MINUTE = 60 * 1000;
    private static LoggerDataQuality instance;
    Context context;
    int[] dataQuality = new int[60 * 24];
    Handler handler;
    DataSourceClient dataSourceClient;
    private Runnable runnableCurrent = new Runnable() {
        @Override
        public void run() {
            Calendar calendar = Calendar.getInstance();
            int index = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            dataQuality[index] = getCurrentQuality(calendar.getTimeInMillis() - MINUTE, calendar.getTimeInMillis());
            handler.postDelayed(this, MINUTE);
        }
    };
    Runnable runnableInitialize = new Runnable() {
        @Override
        public void run() {
            try {
                DataSourceBuilder dataSourceBuilder = null;
                ConfigCondition[] condition = Configuration.getInstance().getConditions();
                for (ConfigCondition aCondition : condition) {
                    if (ConditionManager.TYPE_DATA_QUALITY.equals(aCondition.getType())) {
                        dataSourceBuilder = new DataSourceBuilder(aCondition.getData_source());
                        break;
                    }
                }
                if (dataSourceBuilder == null)
                    return;
                ArrayList<DataSourceClient> dataSourceClientArrayList = DataKitAPI.getInstance(context).find(dataSourceBuilder);
                if (dataSourceClientArrayList.size() == 0) handler.postDelayed(this, 1000);
                else {
                    dataSourceClient = dataSourceClientArrayList.get(0);
                    Arrays.fill(dataQuality, -1);
                    prepare();
                    handler.postDelayed(runnableCurrent, MINUTE);
                }
            } catch (DataKitException ignored) {
            }
        }
    };

    private LoggerDataQuality(Context context) {
        Log.d(TAG, "LoggerManager()...");
        this.context = context;
        handler = new Handler();
    }

    public static LoggerDataQuality getInstance(Context context) {
        Log.d(TAG, "getInstance()...instance=" + instance);
        if (instance == null) {
            instance = new LoggerDataQuality(context);
        }
        return instance;
    }

    public void start() {
        Arrays.fill(dataQuality, -1);
        handler.post(runnableInitialize);
    }

    public void stop() {
        handler.removeCallbacks(runnableInitialize);
        handler.removeCallbacks(runnableCurrent);
    }

    void prepare() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<DataType> dataTypes;
                Calendar calendar = Calendar.getInstance();
                int index;
                int[] values = new int[1440];
                Arrays.fill(values, 0);
                try {
                    long curTimeStamp = DateTime.getDateTime();
                    for (long now = curTimeStamp - 24 * 60 * 60 * 1000; now < curTimeStamp; now += 30 * 60 * 1000) {
                        dataTypes = DataKitAPI.getInstance(context).query(dataSourceClient, now, now + 30 * 60 * 1000);
                        Log.d(TAG, "now = " + now + " datasize=" + dataTypes.size());

                        for (int i = 0; i < dataTypes.size(); i++) {
                            DataTypeInt dataTypeInt = (DataTypeInt) dataTypes.get(i);
                            calendar.setTimeInMillis(dataTypeInt.getDateTime());
                            index = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
                            if (isWearing(dataTypeInt.getSample()))
                                values[index]++;
                        }
                    }
                    for (int i = 0; i < 1440; i++)
                        if (values[i] >= 10 && dataQuality[i] == -1)
                            dataQuality[i] = DATA_QUALITY.GOOD;
                        else dataQuality[i] = DATA_QUALITY.BAD;
                } catch (DataKitException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public double getQuality(long startTimeStamp, long endTimeStamp) {
        int count = 0;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startTimeStamp);
        int sIndex = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE) - 1;
        calendar.setTimeInMillis(endTimeStamp);
        int eIndex = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE) - 1;
        for (int i = sIndex; i <= eIndex; i++) {
            if (dataQuality[i] == DATA_QUALITY.GOOD) {
                count++;
            }
        }
        return 100 * (double) (count) / (double) (eIndex - sIndex + 1);
    }

    private int getCurrentQuality(long startTimeStamp, long endTimeStamp) {
        int count = 0;
        ArrayList<DataType> dataTypes = null;
        try {
            dataTypes = DataKitAPI.getInstance(context).query(dataSourceClient, startTimeStamp, endTimeStamp);
            for (int i = 0; i < dataTypes.size(); i++) {
                int curQuality = ((DataTypeInt) dataTypes.get(i)).getSample();
                if (isWearing(curQuality))
                    count++;
            }
        } catch (DataKitException ignored) {

        }
        Log.d(TAG, "datatypes.size()=" + dataTypes.size() + " count=" + count);
        if (count >= 14) return DATA_QUALITY.GOOD;
        return DATA_QUALITY.BAD;
    }

    private boolean isWearing(int value) {
        return value == DATA_QUALITY.GOOD || value == DATA_QUALITY.NOISE || value == DATA_QUALITY.BAND_LOOSE;
    }
}
