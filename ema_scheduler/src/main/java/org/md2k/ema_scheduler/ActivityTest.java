package org.md2k.ema_scheduler;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.ema_scheduler.configuration.Configuration;
import org.md2k.ema_scheduler.configuration.EMAType;
import org.md2k.ema_scheduler.delivery.DeliveryManager;
import org.md2k.utilities.Report.Log;

public class ActivityTest extends AppCompatActivity {
    private static final String TAG = ActivityMain.class.getSimpleName();
    Configuration configuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        try {
            configuration = Configuration.getInstance();
            String packageName = getIntent().getStringExtra("package_name");
            int location = findEMAType(packageName);
            Log.d(TAG, "location=" + location + " packagename=" + packageName);
            if (location != -1) {
                DeliveryManager deliveryManager = null;
                deliveryManager = DeliveryManager.getInstance(ActivityTest.this);
                deliveryManager.start(configuration.getEma_types()[location], false, "USER");
                finish();
            } else {
                if (configuration.getEma_types() == null) {
                    Toast.makeText(getApplicationContext(), "!!!Error: EMA Configuration file not available...", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    addButtons();
                }
            }
        } catch (DataKitException e) {
            LocalBroadcastManager.getInstance(ActivityTest.this).sendBroadcast(new Intent(ServiceEMAScheduler.class.getSimpleName()));

        }
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

    void addButtons() {
        final EMAType[] emaTypes = configuration.getEma_types();
        for (int i = 0; i < emaTypes.length; i++) {
            Button myButton = new Button(this);
            myButton.setText(emaTypes[i].getName());
            LinearLayout ll = (LinearLayout) findViewById(R.id.linear_layout_buttons);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            ll.addView(myButton, lp);
            final int finalI = i;
            myButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        DeliveryManager deliveryManager = DeliveryManager.getInstance(ActivityTest.this);
                        deliveryManager.start(emaTypes[finalI], true, "TEST");
                    } catch (DataKitException e) {
                        LocalBroadcastManager.getInstance(ActivityTest.this).sendBroadcast(new Intent(ServiceEMAScheduler.class.getSimpleName()));

                    }
                }
            });
        }
    }


    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()...");
        super.onDestroy();
    }


}
