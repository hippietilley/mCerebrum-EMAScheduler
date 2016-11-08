package org.md2k.ema_scheduler.incentive;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.md2k.ema_scheduler.R;
/*
 * Copyright (c) 2016, The University of Memphis, MD2K Center
 * - Syed Monowar Hossain <monowar.hossain@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

public class ActivityIncentive extends AppCompatActivity {
    private static final String TAG = ActivityIncentive.class.getSimpleName();
    private Handler handler;
    private Runnable runnableClose=new Runnable() {
        @Override
        public void run() {
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incentive);
        handler=new Handler();
        setCancelButton();
        String messages[]=getIntent().getStringArrayExtra("messages");
        double total_incentive=getIntent().getDoubleExtra("total_incentive",0);
        if(messages[0]!=null && messages[0].length()!=0)
            ((TextView)findViewById(R.id.textView_message_1)).setText(messages[0]);
        else ((TextView)findViewById(R.id.textView_message_1)).setText("");
        if(messages[1]!=null && messages[1].length()!=0)
            ((TextView)findViewById(R.id.textView_message_2)).setText(messages[1]);
        else
            ((TextView)findViewById(R.id.textView_message_2)).setText("");
        if(messages[2]!=null && messages[2].length()!=0)
            ((TextView)findViewById(R.id.textView_message_3)).setText(messages[2]+" "+String.format("%.2f",total_incentive));
        else
            ((TextView)findViewById(R.id.textView_message_3)).setText("");
        handler.postDelayed(runnableClose, 60000);
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
                handler.removeCallbacks(runnableClose);
                finish();
            }
        });
    }

}
