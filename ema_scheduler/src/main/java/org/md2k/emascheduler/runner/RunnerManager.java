package org.md2k.emascheduler.runner;

import android.content.Context;
import android.content.Intent;

import org.md2k.emascheduler.configuration.ConfigurationApplication;
import org.md2k.emascheduler.configuration.ConfigurationEMAType;
import org.md2k.emascheduler.configuration.ConfigurationManager;

import java.util.ArrayList;

/**
 * Created by monowar on 3/10/16.
 */
public class RunnerManager {
    private static RunnerManager instance=null;
    Context context;
    public static RunnerManager getInstance(Context context){
        if(instance==null)
            instance=new RunnerManager(context);
        return instance;
    }
    private RunnerManager(Context context){
        this.context=context;
    }
    public void start(String id){
        ArrayList<ConfigurationEMAType> configurationEMATypeArrayList = ConfigurationManager.getInstance(context).getConfiguration().getEmas();
        for(int i=0;i< configurationEMATypeArrayList.size();i++){
            if(configurationEMATypeArrayList.get(i).getId().equals(id)){
                ConfigurationApplication configurationApplication= configurationEMATypeArrayList.get(i).getApplication();
                Intent intent = context.getPackageManager().getLaunchIntentForPackage(configurationApplication.getPackage_name());
                intent.setAction(configurationApplication.getPackage_name());
                intent.putExtra("file_name", configurationApplication.getFile_name());
                //Todo: Set TimeOut
                //intent.putExtra("timeout", notificationConfig.getTimeout().getCompletion_timeout());
                context.startActivity(intent);
            }
        }
    }
}
