package org.md2k.ema_scheduler.incentive;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.md2k.ema_scheduler.R;

public class ActivityIncentive extends AppCompatActivity {
    private static final String TAG = ActivityIncentive.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incentive);
        setCancelButton();
        String messages[]=getIntent().getStringArrayExtra("messages");
        double total_incentive=getIntent().getDoubleExtra("total_incentive",0);
        if(messages[0]!=null && messages[0].length()!=0)
            ((TextView)findViewById(R.id.textView_message_1)).setText(messages[0]);
        if(messages[1]!=null && messages[1].length()!=0)
            ((TextView)findViewById(R.id.textView_message_2)).setText(messages[1]);
        if(messages[2]!=null && messages[2].length()!=0)
            ((TextView)findViewById(R.id.textView_message_3)).setText(messages[2]+" "+String.format("%.2f",total_incentive));
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
        }
        return true;
    }
    private void setCancelButton() {
        final Button button = (Button) findViewById(R.id.button_1);
        button.setText("Close");
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }

}
