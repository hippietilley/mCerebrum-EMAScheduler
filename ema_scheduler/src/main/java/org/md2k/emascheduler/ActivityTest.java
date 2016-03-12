package org.md2k.emascheduler;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.md2k.emascheduler.configuration.ConfigurationEMAType;
import org.md2k.emascheduler.configuration.ConfigurationManager;
import org.md2k.emascheduler.notifier.NotifierManager;

import java.util.ArrayList;

public class ActivityTest extends AppCompatActivity {
    private static final String TAG = ActivityMain.class.getSimpleName();
    ConfigurationManager configurationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        configurationManager = ConfigurationManager.getInstance(getApplicationContext());
        if (configurationManager.getConfiguration() == null) {
            Toast.makeText(getApplicationContext(), "!!!Error: EMA Configuration file not available...", Toast.LENGTH_LONG).show();
            finish();
        } else {
            addButtons();
        }
    }

    void addButtons() {
        final ArrayList<ConfigurationEMAType> configurationEMATypes = configurationManager.getConfiguration().getEmas();
        for (int i = 0; i < configurationEMATypes.size(); i++) {
            Button myButton = new Button(this);
            myButton.setText(configurationEMATypes.get(i).getName());
            LinearLayout ll = (LinearLayout) findViewById(R.id.linear_layout_buttons);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            ll.addView(myButton, lp);
            final int finalI = i;
            myButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    NotifierManager.getInstance(ActivityTest.this).start(configurationEMATypes.get(finalI).getId());
                }
            });
        }
    }
}
