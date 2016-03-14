package org.md2k.ema_scheduler;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.md2k.ema_scheduler.configuration.Configuration;
import org.md2k.ema_scheduler.configuration.EMAType;
import org.md2k.ema_scheduler.runner.RunnerManager;
import org.md2k.utilities.Report.Log;

public class ActivityTest extends AppCompatActivity {
    private static final String TAG = ActivityMain.class.getSimpleName();
    Configuration configuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        configuration = Configuration.getInstance();
        if (configuration.getEma_types() == null) {
            Toast.makeText(getApplicationContext(), "!!!Error: EMA Configuration file not available...", Toast.LENGTH_LONG).show();
            finish();
        } else {
            addButtons();
        }
    }

    void addButtons() {
        final EMAType[] EMATypes = configuration.getEma_types();
        for (int i = 0; i < EMATypes.length; i++) {
            Button myButton = new Button(this);
            myButton.setText(EMATypes[i].getName());
            LinearLayout ll = (LinearLayout) findViewById(R.id.linear_layout_buttons);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            ll.addView(myButton, lp);
            final int finalI = i;
            myButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    RunnerManager.getInstance(ActivityTest.this).start(EMATypes[finalI].getApplication());
//                    NotifierManager.getInstance(ActivityTest.this).start(EMATypes.get(finalI).getId());
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
