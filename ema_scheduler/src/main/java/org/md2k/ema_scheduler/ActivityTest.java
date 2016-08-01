package org.md2k.ema_scheduler;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.md2k.ema_scheduler.configuration.Configuration;
import org.md2k.ema_scheduler.configuration.EMAType;
import org.md2k.utilities.Report.Log;

public class ActivityTest extends AppCompatActivity {
    private static final String TAG = ActivityMain.class.getSimpleName();
    Configuration configuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        configuration = Configuration.getInstance();
    }

    private int findEMAType(String packageName) {
        if (packageName == null) return -1;
        if (packageName.length() == 0) return -1;
        EMAType emaTypes[] = configuration.getEma_types();
        for (int i = 0; i < emaTypes.length; i++) {
            if (emaTypes[i].getApplication() == null) continue;
            if (emaTypes[i].getApplication().getPackage_name().equals(packageName)) return i;
        }
        return -1;
    }


    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()...");
        super.onDestroy();
    }


}
