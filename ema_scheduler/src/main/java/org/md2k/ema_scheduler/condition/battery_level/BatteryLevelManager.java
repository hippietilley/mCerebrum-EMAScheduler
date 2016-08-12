package org.md2k.ema_scheduler.condition.battery_level;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.ema_scheduler.condition.Condition;
import org.md2k.ema_scheduler.configuration.ConfigCondition;

/**
 * Created by monowar on 3/26/16.
 */
public class BatteryLevelManager extends Condition {
    public BatteryLevelManager(Context context) {
        super(context);
    }

    public boolean isValid(ConfigCondition configCondition) throws DataKitException {
        double limitPercentage = Double.parseDouble(configCondition.getValues().get(0));
        double percentage = getBatteryLevel();
        if (percentage > limitPercentage) {
            log(configCondition, "true: " + percentage + " > " + limitPercentage);
            return true;
        } else {
            log(configCondition, "false: " + percentage + " < " + limitPercentage);
            return false;
        }
    }

    double getBatteryLevel() {
        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent intent = context.registerReceiver(null, iFilter);
        assert intent != null;
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
        float percentage;
        if (level == -1 || scale == -1) {
            percentage = 0.0f;
        } else {
            percentage = ((float) level / (float) scale) * 100.0f;
        }
        return percentage;
    }
}
