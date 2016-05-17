package org.md2k.ema_scheduler;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.ema_scheduler.logger.LogInfo;
import org.md2k.ema_scheduler.logger.LoggerManager;
import org.md2k.utilities.Apps;
import org.md2k.utilities.UI.ActivityAbout;
import org.md2k.utilities.UI.ActivityCopyright;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import io.fabric.sdk.android.Fabric;

public class ActivityMain extends AppCompatActivity {
    private static final String TAG = ActivityMain.class.getSimpleName();
    Handler mHandler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            {
                long time = Apps.serviceRunningTime(ActivityMain.this, ServiceEMAScheduler.class.getName());
                if (time < 0) {
                    ((TextView) findViewById(R.id.button_app_status)).setText("START");
                    findViewById(R.id.button_app_status).setBackground(ContextCompat.getDrawable(ActivityMain.this, R.drawable.button_status_off));

                } else {

                    ((TextView) findViewById(R.id.button_app_status)).setText(DateTime.convertTimestampToTimeStr(time));
                    findViewById(R.id.button_app_status).setBackground(ContextCompat.getDrawable(ActivityMain.this, R.drawable.button_status_on));
                }
                mHandler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Set up Crashlytics, disabled for debug builds
        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build();

        // Initialize Fabric with the debug-disabled crashlytics.
        Fabric.with(this, crashlyticsKit);

        setContentView(R.layout.activity_main);
        final Button buttonService = (Button) findViewById(R.id.button_app_status);

        buttonService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityMain.this, ServiceEMAScheduler.class);
                if (Apps.isServiceRunning(ActivityMain.this, ServiceEMAScheduler.class.getName())) {
                    stopService(intent);
                } else {
                    startService(intent);
                }
            }
        });
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_test:
                intent = new Intent(this, ActivityTest.class);
                startActivity(intent);
                break;
            case R.id.action_report:
                //TODO: add report
                break;
            case R.id.action_about:
                intent = new Intent(this, ActivityAbout.class);
                try {
                    intent.putExtra(org.md2k.utilities.Constants.VERSION_CODE, String.valueOf(this.getPackageManager().getPackageInfo(getPackageName(), 0).versionCode));
                    intent.putExtra(org.md2k.utilities.Constants.VERSION_NAME, this.getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    Crashlytics.log(Log.ERROR, "EMA_Scheduler", "action_about intent incorrect");
                    Crashlytics.logException(e);
                }
                startActivity(intent);
                break;
            case R.id.action_copyright:
                intent = new Intent(this, ActivityCopyright.class);
                startActivity(intent);
                break;
            case R.id.action_settings:
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    public void onResume() {
        mHandler.post(runnable);
        createTable();
        updateTable();
        super.onResume();
    }

    public void onPause() {
        mHandler.removeCallbacks(runnable);
        super.onPause();
    }

    void updateTable() {
        if (!DataKitAPI.getInstance(getApplicationContext()).isConnected()) return;
        long curTime = DateTime.getDateTime();
        LogInfo logInfo;
        String time, type, msg;
        LoggerManager loggerManager = LoggerManager.getInstance(getApplicationContext());
        for (int i = loggerManager.getLogInfos().size() - 1; i >= 0; i--) {
            logInfo = loggerManager.getLogInfos().get(i);
            if (curTime - logInfo.getTimestamp() >= 4 * 60 * 60 * 1000) continue;
            time = formatTime(logInfo.getTimestamp());
            if (logInfo.getOperation() != null && logInfo.getId() != null) {
                type = logInfo.getOperation().toLowerCase() + ":" + logInfo.getId().toLowerCase()+":"+logInfo.getStatus();
                msg = logInfo.getMessage().toLowerCase();
                addRow(time, type, msg);
            }
        }
    }

    String formatTime(long timestamp) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            Date currenTimeZone = calendar.getTime();
            return sdf.format(currenTimeZone);
        } catch (Exception e) {
        }
        return "";
    }

    void createTable() {
        TableLayout ll = (TableLayout) findViewById(R.id.tableLayout);
        ll.removeAllViews();
        ll.addView(createDefaultRow());
    }

    void addRow(String time, String type, String msg) {
        TableLayout ll = (TableLayout) findViewById(R.id.tableLayout);
//        ll.setColumnShrinkable(1,true);
//        ll.setColumnShrinkable(2,true);
//        ll.setColumnShrinkable(0, false);
        TableRow row = new TableRow(this);
        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
        row.setLayoutParams(lp);
        TextView tvDate = new TextView(this);
        tvDate.setText(time);
        TextView tvType = new TextView(this);
        tvType.setText(type);
        TextView tvStatus = new TextView(this);
        tvStatus.setText(msg);
        row.addView(tvDate);
        row.addView(tvType);
        row.addView(tvStatus);
        row.setBackgroundResource(R.drawable.border);
        ll.addView(row);

    }

    TableRow createDefaultRow() {
        TableRow row = new TableRow(this);
        TextView tvDate = new TextView(this);
        tvDate.setText("Date/Time");
        tvDate.setTypeface(null, Typeface.BOLD);
        tvDate.setTextColor(getResources().getColor(R.color.teal_A700));
        TextView tvType = new TextView(this);
        tvType.setText("Type");
        tvType.setTypeface(null, Typeface.BOLD);
        tvType.setTextColor(getResources().getColor(R.color.teal_A700));
        TextView tvStatus = new TextView(this);
        tvStatus.setText("Status");
        tvStatus.setTypeface(null, Typeface.BOLD);
        tvStatus.setTextColor(getResources().getColor(R.color.teal_A700));
        row.addView(tvDate);
        row.addView(tvType);
        row.addView(tvStatus);
        return row;
    }
}
