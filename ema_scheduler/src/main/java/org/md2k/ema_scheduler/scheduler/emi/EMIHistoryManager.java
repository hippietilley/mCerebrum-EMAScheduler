package org.md2k.ema_scheduler.scheduler.emi;

import android.content.Context;

import org.md2k.datakitapi.time.DateTime;
import org.md2k.ema_scheduler.logger.EMIInfo;
import org.md2k.ema_scheduler.logger.LogInfo;
import org.md2k.ema_scheduler.logger.LoggerManager;

import java.util.ArrayList;

/**
 * Created by monowar on 3/14/16.
 */
public class EMIHistoryManager {
    private static final String TAG = EMIHistoryManager.class.getSimpleName();
    Context context;
    String type, id;


    public EMIHistoryManager(Context context, String type, String id) {
        this.context = context;
        this.type=type;
        this.id=id;
    }


    public void insert(EMIInfo emiInfo){
        LogInfo logInfo=new LogInfo();
        logInfo.setOperation(LogInfo.OP_EMI_INFO);
        logInfo.setTimestamp(DateTime.getDateTime());
        logInfo.setId(id);
        logInfo.setType(type);
        logInfo.setEmiInfo(emiInfo);
        LoggerManager.getInstance(context).insert(logInfo);
    }

    public ArrayList<EMIInfo> getEmiHistories(boolean isStress, long dayStartTime, long curTime){
        ArrayList<LogInfo> logInfos=LoggerManager.getInstance(context).getLogInfos(LogInfo.OP_EMI_INFO,type,id,dayStartTime, curTime);
        ArrayList<EMIInfo> emiInfos=new ArrayList<>();
        for(int i=0;i< logInfos.size();i++){
            if(logInfos.get(i).getEmiInfo().isStress==isStress)
                emiInfos.add(logInfos.get(i).getEmiInfo());
        }
        return emiInfos;
    }
}

