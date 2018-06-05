/*
 * Copyright (c) 2018, The University of Memphis, MD2K Center of Excellence
 *
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

package org.md2k.ema_scheduler;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import org.md2k.ema_scheduler.configuration.Configuration;
import org.md2k.ema_scheduler.configuration.EMAType;

/**
 * Defines the activity for administering an EMA test.
 */
public class ActivityTest extends AppCompatActivity {
    private static final String TAG = ActivityTest.class.getSimpleName();
    private Configuration configuration;

    /**
     * If this activity is started by the user, an intent is created to start an EMA. Otherwise,
     * the EMA for the initiating package is loaded.
     * @param savedInstanceState Previous state of this activity, if it existed.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().hasExtra("package_name")) {
            configuration = Configuration.getInstance();
            String packageName = getIntent().getStringExtra("package_name");
            EMAType emaType = findEMAType(packageName);
            if (emaType != null) {
                Intent intent = new Intent(Constants.INTENT_USER);
                intent.putExtra(EMAType.class.getSimpleName(), emaType);
                intent.putExtra("is_notify", false);
                intent.putExtra("type", "USER");
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                finish();
            }
        } else {
            setContentView(R.layout.activity_test);
            getFragmentManager().beginTransaction().replace(R.id.layout_preference_fragment,
                    new PrefsFragmentTest()).commit();
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(true);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    /**
     * Provides actions for menu items.
     * @param item Menu item that was selected.
     * @return Whether the action was successful.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Returns the EMA type of the given package.
     * @param packageName Name of the package to get the EMA type for.
     * @return The EMA type of the given package.
     */
    private EMAType findEMAType(String packageName) {
        if (packageName == null)
            return null;
        if (packageName.length() == 0)
            return null;
        EMAType emaTypes[] = configuration.getEma_types();
        for (EMAType emaType : emaTypes) {
            if (emaType.getApplication() == null)
                continue;
            if (emaType.getApplication().getPackage_name().equals(packageName))
                return emaType;
        }
        return null;
    }
}
