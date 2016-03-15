package org.md2k.ema_scheduler.scheduler;

import android.content.Context;

import org.md2k.ema_scheduler.configuration.Configuration;
import org.md2k.ema_scheduler.configuration.EMAType;

import java.util.ArrayList;

/**
 * Created by monowar on 3/10/16.
 */
public class SchedulerManager {
    private static final String TAG = SchedulerManager.class.getSimpleName();
    Context context;
    Configuration configuration;
    DayManager dayManager;
    ArrayList<Scheduler> scheduler;

    public SchedulerManager(Context context) {
        this.context = context;
        configuration = Configuration.getInstance();
        DayManager.clear();
        dayManager = DayManager.getInstance(context);
        prepareScheduler();
        dayManager.setCallback(new Callback() {
            @Override
            public void onDayStartChanged() {
                stop();
                start();
            }

            @Override
            public void onDayEndChanged() {
                stop();
            }
        });

    }

    void prepareScheduler() {
        for (int i = 0; i < configuration.getEma_types().length; i++) {
            if (configuration.getEma_types()[i].getType().equals(EMAType.TYPE_RANDOM))
                scheduler.add(new RandomEMAScheduler(context, configuration.getEma_types()[i]));
            else if (configuration.getEma_types()[i].getType().equals(EMAType.TYPE_EVENT))
                scheduler.add(new EventEMAScheduler(context, configuration.getEma_types()[i]));
        }
    }

    public void start() {
        for (int i = 0; i < scheduler.size(); i++)
            scheduler.get(i).start();
//        handler.postDelayed(deliver, 4000);
    }

    public void stop() {
        for (int i = 0; i < scheduler.size(); i++)
            scheduler.get(i).stop();
    }
}
