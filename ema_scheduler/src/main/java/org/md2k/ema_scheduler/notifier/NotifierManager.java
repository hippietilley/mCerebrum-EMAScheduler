package org.md2k.ema_scheduler.notifier;

import android.content.Context;

/**
 * Created by monowar on 3/10/16.
 */
public class NotifierManager {
    private static NotifierManager instance=null;
    Context context;
    public static NotifierManager getInstance(Context context){
        if(instance==null)
            instance=new NotifierManager(context);
        return instance;
    }
    private NotifierManager(Context context){
        this.context=context;
    }
    public void start(String id){
/*        ArrayList<ConfigurationEMAType> configurationEMATypeArrayList = ConfigurationManager.getInstance(context).getConfiguration().getEmas();
        for(int i=0;i< configurationEMATypeArrayList.size();i++){
            if(configurationEMATypeArrayList.get(i).getId().equals(id)){
                //TODO: generate Notification
            }
        }
*/    }
}
