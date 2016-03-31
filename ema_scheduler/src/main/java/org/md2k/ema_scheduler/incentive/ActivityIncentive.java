package org.md2k.ema_scheduler.incentive;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import org.md2k.ema_scheduler.R;
import org.md2k.ema_scheduler.logger.LogInfo;
import org.md2k.ema_scheduler.logger.LoggerManager;

public class ActivityIncentive extends AppCompatActivity {
    private static final String TAG = ActivityIncentive.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incentive);
        IncentiveManagerNW incentiveManagerNW=new IncentiveManagerNW(this);
        LogInfo logInfo=LoggerManager.getInstance(this).getLogInfoLast(LogInfo.OP_RUN, null, null);
            ((TextView)findViewById(R.id.textView_msg)).setText(incentiveManagerNW.getCurrentMessage());
        ((TextView)findViewById(R.id.textView_current_incentive)).setText(incentiveManagerNW.getCurrentIncentiveString());

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
