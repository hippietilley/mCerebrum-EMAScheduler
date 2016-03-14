package org.md2k.ema_scheduler.scheduler;

import android.content.Context;

/**
 * Created by monowar on 3/14/16.
 */
public class DayManager {
    private static DayManager instance;
    Context context;
    long dayStartTime, dayEndTime;
    public static DayManager getInstance(Context context){
        if(instance==null)
            instance=new DayManager(context);
        return instance;
    }
    private DayManager(Context context){
        this.context=context;
    }

    public long getDayStartTime() {
        return dayStartTime;
    }

    public void setDayStartTime(long dayStartTime) {
        this.dayStartTime = dayStartTime;
    }

    public long getDayEndTime() {
        return dayEndTime;
    }

    public void setDayEndTime(long dayEndTime) {
        this.dayEndTime = dayEndTime;
    }
}
