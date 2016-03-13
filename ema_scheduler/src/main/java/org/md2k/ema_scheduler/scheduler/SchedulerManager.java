package org.md2k.ema_scheduler.scheduler;

import android.content.Context;

/**
 * Created by monowar on 3/10/16.
 */
public class SchedulerManager {
    private static SchedulerManager instance=null;
    Context context;
    public static SchedulerManager getInstance(Context context){
        if(instance==null)
            instance=new SchedulerManager(context);
        return instance;
    }
    private SchedulerManager(Context context){
        this.context=context;
    }
}
