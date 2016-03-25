package org.md2k.ema_scheduler.scheduler;

import android.content.Context;

import org.md2k.ema_scheduler.configuration.Window;
import org.md2k.ema_scheduler.day.DayManager;

/**
 * Created by monowar on 3/14/16.
 */
public class WindowManager {
    Context context;
    Window[] windows;
    DayManager dayManager;
    public WindowManager(Context context, Window[] windows, DayManager dayManager){
        this.windows=windows;
        this.context=context;
        this.dayManager=dayManager;
    }
    int getWindowIndex(long timestamp){
        for(int i=0;i<windows.length;i++){
            if(windows[i].getType().equals(Window.TYPE_DAY_START)){
                long dayStartTimestamp= dayManager.getDayStartTime();
                long startTime=dayStartTimestamp+windows[i].getStart_offset();
                long endTime=dayStartTimestamp+windows[i].getEnd_offset();
                if(timestamp>=startTime && timestamp<=endTime)
                    return i;
            }
        }
        return -1;
    }
    long getWindowStartTime(long timestamp){
        for(int i=0;i<windows.length;i++){
            if(windows[i].getType().equals(Window.TYPE_DAY_START)){
                long startTime=dayManager.getDayStartTime()+windows[i].getStart_offset();
                long endTime=dayManager.getDayStartTime()+windows[i].getEnd_offset();
                if(timestamp>=startTime && timestamp<=endTime)
                    return startTime;
            }
        }
        return -1;
    }
    long getNextWindowStartTime(long timestamp){
        for(int i=0;i<windows.length;i++){
            if(windows[i].getType().equals(Window.TYPE_DAY_START)){
                long startTime=dayManager.getDayStartTime()+windows[i].getStart_offset();
                long endTime=dayManager.getDayStartTime()+windows[i].getEnd_offset();
                if(timestamp>=startTime && timestamp<=endTime)
                    if(i<windows.length-1)
                    return dayManager.getDayStartTime()+windows[i+1].getStart_offset();
            }
        }
        return -1;
    }

    long getWindowEndTime(long timestamp){
        for(int i=0;i<windows.length;i++){
            if(windows[i].getType().equals(Window.TYPE_DAY_START)){
                long startTime=dayManager.getDayStartTime()+windows[i].getStart_offset();
                long endTime=dayManager.getDayStartTime()+windows[i].getEnd_offset();
                if(timestamp>=startTime && timestamp<=endTime)
                    return endTime;
            }
        }
        return -1;
    }

    public Window[] getWindows() {
        return windows;
    }
}
