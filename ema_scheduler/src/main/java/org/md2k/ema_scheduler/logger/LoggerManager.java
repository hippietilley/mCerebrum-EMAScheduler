package org.md2k.ema_scheduler.logger;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.datatype.DataTypeJSONObject;
import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.source.METADATA;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.source.platform.Platform;
import org.md2k.datakitapi.source.platform.PlatformBuilder;
import org.md2k.datakitapi.source.platform.PlatformType;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.ema_scheduler.Constants;
import org.md2k.ema_scheduler.ServiceEMAScheduler;
import org.md2k.utilities.FileManager;
import org.md2k.utilities.Report.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by monowar on 3/14/16.
 */
public class LoggerManager {
    private static final String TAG = LoggerManager.class.getSimpleName();
    private static LoggerManager instance;
    Context context;
    DataKitAPI dataKitAPI;
    DataSourceBuilder dataSourceBuilderLogger;
    DataSourceClient dataSourceClientLogger;
    ArrayList<LogInfo> logInfos;

    private LoggerManager(Context context) throws DataKitException {
        Log.d(TAG,"LoggerManager()...");
        this.context = context;
        dataKitAPI = DataKitAPI.getInstance(context);
        dataSourceBuilderLogger = createDataSourceBuilderLogger();
        registerLogInfo();
        logInfos = new ArrayList<>();
    }

    public static LoggerManager getInstance(Context context) {
        Log.d(TAG,"getInstance()...instance="+instance);
        if (instance == null)
            try {
                instance = new LoggerManager(context);
            } catch (DataKitException e) {
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ServiceEMAScheduler.BROADCAST_MSG));
            }
        return instance;
    }

    public static void clear() {
        Log.d(TAG, "clear()...");
        instance = null;
    }

    private void registerLogInfo() throws DataKitException {
        dataSourceClientLogger = dataKitAPI.register(dataSourceBuilderLogger);
    }

    public void reset(long dayStartTime) throws DataKitException {
        readLogInfoFromFile(dayStartTime);
    }

    public void insert(LogInfo logInfo) throws DataKitException {
        Gson gson=new Gson();
        JsonObject sample = new JsonParser().parse(gson.toJson(logInfo)).getAsJsonObject();
        Log.d(TAG,"log="+sample.toString());
        DataTypeJSONObject dataTypeJSONObject = new DataTypeJSONObject(DateTime.getDateTime(), sample);
        dataKitAPI.insert(dataSourceClientLogger, dataTypeJSONObject);
        switch (logInfo.getOperation()) {
            case LogInfo.OP_SCHEDULE:
            case LogInfo.OP_DELIVER:
            case LogInfo.OP_RUN:
            case LogInfo.OP_EMI_INFO:
                logInfos.add(logInfo);
                writeLogInfoToFile();
                break;
        }
//        logInfos.add(logInfo);

    }

    /*    private void readLogInfosFromDataKit(long startTimestamp) throws DataKitException {
            Log.d(TAG,"readLogInfosFromDataKit...");
            long endTimestamp = DateTime.getDateTime();
            logInfos=new ArrayList<>();
            Gson gson=new Gson();
            ArrayList<DataType> dataTypes=dataKitAPI.query(dataSourceClientLogger, startTimestamp, endTimestamp);
            for(int i=0;i<dataTypes.size();i++){
                DataTypeJSONObject dataTypeJSONObject = (DataTypeJSONObject) dataTypes.get(i);
                LogInfo logInfo = gson.fromJson(dataTypeJSONObject.getSample().toString(), LogInfo.class);
                logInfos.add(logInfo);
            }
            Log.d(TAG, "readLogInfosFromDataKit...size=" + logInfos.size());
        }
        */
    private void readLogInfoFromFile(long dayStartTime) {
        boolean flag = false;
        try {
            logInfos = FileManager.readJSONArray(Constants.CONFIG_DIRECTORY, Constants.LOG_FILENAME, LogInfo.class);
            Iterator<LogInfo> i = logInfos.iterator();
            while (i.hasNext()) {
                LogInfo logInfo = i.next();
                if (logInfo.getTimestamp() < dayStartTime) {
                    i.remove();
                    flag = true;
                }
            }
            if (flag)
                writeLogInfoToFile();
        } catch (FileNotFoundException e) {
            logInfos = new ArrayList<>();
        }
    }

    private void writeLogInfoToFile() {
        try {
            FileManager.writeJSONArray(Constants.CONFIG_DIRECTORY, Constants.LOG_FILENAME, logInfos);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    DataSourceBuilder createDataSourceBuilderLogger() {
        Platform platform = new PlatformBuilder().setType(PlatformType.PHONE).build();
        DataSourceBuilder dataSourceBuilder = new DataSourceBuilder().setType(DataSourceType.LOG).setPlatform(platform);
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.NAME, "Log");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.DESCRIPTION, "Represents the log of EMA Scheduler");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.DATA_TYPE, DataTypeJSONObject.class.getName());
        ArrayList<HashMap<String, String>> dataDescriptors = new ArrayList<>();
        HashMap<String, String> dataDescriptor = new HashMap<>();
        dataDescriptor.put(METADATA.NAME, "Log");
        dataDescriptor.put(METADATA.UNIT, "string");
        dataDescriptor.put(METADATA.DESCRIPTION, "Contains log");
        dataDescriptor.put(METADATA.DATA_TYPE, String.class.getName());
        dataDescriptors.add(dataDescriptor);
        dataSourceBuilder = dataSourceBuilder.setDataDescriptors(dataDescriptors);
        return dataSourceBuilder;
    }

    public ArrayList<LogInfo> getLogInfos() {
        return logInfos;
    }

    public ArrayList<LogInfo> getLogInfos(String operation, String status, String type, String id){
        Log.d(TAG,"getLogInfos("+operation+" "+type+" "+id+")");
        ArrayList<LogInfo> logInfosTemp=new ArrayList<>();
        for(int i=0;i<logInfos.size();i++){
            if(!logInfos.get(i).getOperation().equals(operation)) continue;
            if(!logInfos.get(i).getStatus().equals(status)) continue;
            if(!logInfos.get(i).getType().equals(type)) continue;
            if(!logInfos.get(i).getId().equals(id)) continue;
            logInfosTemp.add(logInfos.get(i));
        }
        Log.d(TAG,"getLogInfos("+operation+" "+type+" "+id+") size="+logInfosTemp.size());
        return logInfosTemp;
    }

    public LogInfo getLogInfoLast(String operation, String status, String type, String id) {
        LogInfo logInfo=null;
        Log.d(TAG,"getLogInfoLast("+operation+" "+type+" "+id+")");
        for (int i = 0; i < logInfos.size(); i++) {
            if (operation!=null && !logInfos.get(i).getOperation().equals(operation)) continue;
            if(status!=null && !logInfos.get(i).getStatus().equals(status)) continue;
            if (type!=null && !logInfos.get(i).getType().equals(type)) continue;
            if (id!=null && !logInfos.get(i).getId().equals(id)) continue;
            if (logInfo == null || logInfo.getTimestamp() < logInfos.get(i).getTimestamp())
                logInfo = logInfos.get(i);
        }
        return logInfo;
    }

    public ArrayList<LogInfo> getLogInfos(String operation, String status, String type, String id, long startTime, long endTime){
        Log.d(TAG,"getLogInfos("+operation+" "+type+" "+id+" "+startTime+" "+endTime+")");
        ArrayList<LogInfo> logInfosTemp=new ArrayList<>();
        for(int i=0;i<logInfos.size();i++){
            if(operation!=null && !logInfos.get(i).getOperation().equals(operation)) continue;
            if(status!=null && logInfos.get(i).getStatus()!=null && !logInfos.get(i).getStatus().equals(status)) continue;
            if(type!=null && !logInfos.get(i).getType().equals(type)) continue;
            if(id!=null && !logInfos.get(i).getId().equals(id)) continue;
            if(startTime!=-1 && logInfos.get(i).getTimestamp()<startTime) continue;
            if(endTime!=-1 && logInfos.get(i).getTimestamp()>endTime) continue;
            logInfosTemp.add(logInfos.get(i));
        }
        Log.d(TAG,"getLogInfos("+operation+" "+type+" "+id+" "+startTime+" "+endTime+") size="+logInfosTemp.size());
        return logInfosTemp;
    }

}
