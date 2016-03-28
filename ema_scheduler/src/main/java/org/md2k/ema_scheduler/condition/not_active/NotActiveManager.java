package org.md2k.ema_scheduler.condition.not_active;

import android.content.Context;

import org.md2k.ema_scheduler.condition.Condition;
import org.md2k.ema_scheduler.configuration.ConfigCondition;

/**
 * Created by monowar on 3/26/16.
 */
public class NotActiveManager extends Condition{
    public NotActiveManager(Context context){
        super(context);
    }
    public boolean isValid(ConfigCondition configCondition){
        //TODO: implement walking/running condition
        log(configCondition, "true: not walking/running");
        return true;
    }
}
