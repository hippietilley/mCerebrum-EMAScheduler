package org.md2k.ema_scheduler.scheduler;

import android.content.Context;

import org.md2k.ema_scheduler.configuration.Configuration;
import org.md2k.ema_scheduler.configuration.EMAType;
import org.md2k.ema_scheduler.day.DayManager;
import org.md2k.utilities.Report.Log;

import java.util.ArrayList;

/**
 * Created by monowar on 3/10/16.
 */
public class SchedulerManager {
    private static final String TAG = SchedulerManager.class.getSimpleName();
    Context context;
    Configuration configuration;
    ArrayList<Scheduler> scheduler;
    DayManager dayManager;
    boolean isStarted;

    public SchedulerManager(Context context, DayManager dayManager) {
        Log.d(TAG, "SchedulerManager()...");
        this.context = context;
        this.dayManager=dayManager;
        configuration = Configuration.getInstance();
        scheduler=new ArrayList<>();
        isStarted=false;
        prepareScheduler();
    }

    private void prepareScheduler() {
        Log.d(TAG, "prepareScheduler()...");
        for (int i = 0; i < configuration.getEma_types().length; i++) {
            Log.d(TAG, "prepareScheduler()...emaType ID="+configuration.getEma_types()[i].getId());
            if(configuration.getEma_types()[i].getId()==null) continue;
            switch(configuration.getEma_types()[i].getId()){
                case EMAType.ID_RANDOM_EMA:
                    scheduler.add(new RandomEMAScheduler(context, configuration.getEma_types()[i], dayManager));
                    break;
                case EMAType.ID_SMOKING_EMA:
                    scheduler.add(new SmokingEMAScheduler(context, configuration.getEma_types()[i], dayManager));
                    break;
                case EMAType.ID_END_OF_DAY_EMA:
                    scheduler.add(new EndOfDayEMAScheduler(context, configuration.getEma_types()[i], dayManager));
                    break;
                case EMAType.ID_EMI:
                    scheduler.add(new EMIScheduler(context, configuration.getEma_types()[i], dayManager));
                    break;
                default:
                    break;
            }
        }
    }

    public void start() {
        if(isStarted) return;
        isStarted=true;
        Log.d(TAG, "start()...");
        for (int i = 0; i < scheduler.size(); i++)
            scheduler.get(i).start();
    }

    public void stop() {
        if(!isStarted) return;
        isStarted=false;
        Log.d(TAG, "stop()...");
        for (int i = 0; i < scheduler.size(); i++)
            scheduler.get(i).stop();
    }
    public void reset(){
        for(int i=0;i<scheduler.size();i++)
            scheduler.get(i).reset();
    }
}
