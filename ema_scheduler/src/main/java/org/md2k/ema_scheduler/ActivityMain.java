package org.md2k.ema_scheduler;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.md2k.datakitapi.time.DateTime;
import org.md2k.ema_scheduler.scheduler.ServiceEMAScheduler;
import org.md2k.utilities.Apps;
import org.md2k.utilities.UI.ActivityAbout;
import org.md2k.utilities.UI.ActivityCopyright;

public class ActivityMain extends AppCompatActivity {
    private static final String TAG = ActivityMain.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button buttonService = (Button) findViewById(R.id.button_app_status);

        buttonService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ServiceEMAScheduler.class);
                if (Apps.isServiceRunning(getBaseContext(), ServiceEMAScheduler.class.getName())) {
                    stopService(intent);
                } else {
                    startService(intent);
                }
            }
        });
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 //       Intent intent = new Intent(this, ServiceEMARunner.class);
//        startService(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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
        //Todo Update Table
    }

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

    void createTable() {
        TableLayout ll = (TableLayout) findViewById(R.id.tableLayout);
        ll.removeAllViews();
        ll.addView(createDefaultRow());
    }

    void addRow() {
        TableLayout ll = (TableLayout) findViewById(R.id.tableLayout);
        TableRow row = new TableRow(this);
        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
        row.setLayoutParams(lp);
        TextView tvDate = new TextView(this);
//        tvDate.setText(platform.toLowerCase());
        TextView tvTime = new TextView(this);
//        tvTime.setText("0");
        TextView tvType = new TextView(this);
        //tvType.setText("0");
        TextView tvStatus = new TextView(this);
        //tvStatus.setText("0");
        row.addView(tvDate);
        row.addView(tvTime);
        row.addView(tvType);
        row.addView(tvStatus);
        row.setBackgroundResource(R.drawable.border);
        ll.addView(row);

    }

    TableRow createDefaultRow() {
        TableRow row = new TableRow(this);
        TextView tvDate = new TextView(this);
        tvDate.setText("Date");
        tvDate.setTypeface(null, Typeface.BOLD);
        tvDate.setTextColor(getResources().getColor(R.color.teal_A700));
        TextView tvTime = new TextView(this);
        tvTime.setText("Time");
        tvTime.setTypeface(null, Typeface.BOLD);
        tvTime.setTextColor(getResources().getColor(R.color.teal_A700));
        TextView tvType = new TextView(this);
        tvType.setText("Type");
        tvType.setTypeface(null, Typeface.BOLD);
        tvType.setTextColor(getResources().getColor(R.color.teal_A700));
        TextView tvStatus = new TextView(this);
        tvStatus.setText("Status");
        tvStatus.setTypeface(null, Typeface.BOLD);
        tvStatus.setTextColor(getResources().getColor(R.color.teal_A700));
        row.addView(tvDate);
        row.addView(tvTime);
        row.addView(tvType);
        row.addView(tvStatus);
        return row;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
