package org.md2k.ema_scheduler.condition.valid_block;

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
import org.md2k.ema_scheduler.configuration.Block;
import org.md2k.ema_scheduler.configuration.ConfigCondition;
import org.md2k.ema_scheduler.configuration.Configuration;
import org.md2k.ema_scheduler.logger.LogInfo;
import org.md2k.ema_scheduler.logger.LoggerManager;
import org.md2k.utilities.Report.Log;

import java.util.ArrayList;

/**
 * Created by monowar on 3/26/16.
 */
public class ValidBlockManager extends Condition {
    private static final String TAG = ValidBlockManager.class.getSimpleName();

    public ValidBlockManager(Context context){
        super(context);
    }
    public boolean isValid(ConfigCondition configCondition) throws DataKitException {
//        if(Constants.DEBUG) return true;
        long curTime=DateTime.getDateTime();
        long dayStart=getDay(DataSourceType.DAY_START);
        long dayEnd=getDay(DataSourceType.DAY_END);
        if(dayStart==-1){
            log(configCondition, "false: day is not started");
            return false;
        }else if(dayStart<dayEnd){
            log(configCondition, "false: new day is not started");
            return false;
        }else {
            Block[] blocks=Configuration.getInstance().getEma_types(configCondition.getSource().getId(), configCondition.getSource().getType()).getBlocks();
            for(int i=0;i<blocks.length;i++){
                long blockStartTime=blocks[i].getStart_offset()+dayStart;
                long blockEndTime=blocks[i].getEnd_offset()+dayStart;
                Log.d(TAG, "blockStartTime=" + blockStartTime + " curTime=" + curTime + " blockEndTime=" + blockEndTime);
                int count=0;
                if(blockStartTime<=curTime && curTime<blockEndTime){
                    int limitEMA=Integer.parseInt(configCondition.getValues().get(0));
                    ArrayList<LogInfo> logInfos=LoggerManager.getInstance(context).getLogInfos(LogInfo.OP_DELIVER,LogInfo.STATUS_DELIVER_SUCCESS, configCondition.getSource().getType(), configCondition.getSource().getId());
                    for(int j=0;j<logInfos.size();j++){
                        if(blockStartTime<=logInfos.get(j).getTimestamp() && logInfos.get(j).getTimestamp()<blockEndTime)
                            count++;
                    }
                    if(limitEMA>count) {
                        log(configCondition, "true: block(" + String.valueOf(i + 1)+") triggered: "+String.valueOf(count)+" expected: "+String.valueOf(limitEMA));
                        return true;
                    }else{
                        log(configCondition, "false: block(" + String.valueOf(i + 1)+") triggered: "+String.valueOf(count)+" expected: "+String.valueOf(limitEMA));
                        return false;

                    }
                }
            }
            log(configCondition,"false: no valid block found");
            return false;
        }
    }

    long getDay(String dataSourceType) throws DataKitException {
        long day=-1;
        DataKitAPI dataKitAPI=DataKitAPI.getInstance(context);
        DataSourceBuilder dataSourceBuilder=new DataSourceBuilder().setType(dataSourceType);
        ArrayList<DataSourceClient> dataSourceClients=dataKitAPI.find(dataSourceBuilder);

        if(dataSourceClients.size()==0){
            return day;
        }
        ArrayList<DataType> dataTypes=dataKitAPI.query(dataSourceClients.get(0),1);
        if(dataTypes.size()==0) return day;
        return ((DataTypeLong)dataTypes.get(0)).getSample();
    }
}
