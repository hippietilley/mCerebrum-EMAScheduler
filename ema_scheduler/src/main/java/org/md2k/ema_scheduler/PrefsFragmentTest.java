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
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import org.md2k.ema_scheduler.configuration.Application;
import org.md2k.ema_scheduler.configuration.Configuration;
import org.md2k.ema_scheduler.configuration.EMAType;

/**
 * Preference Fragment for <code>ActivityTest</code>.
 */
public class PrefsFragmentTest extends PreferenceFragment {
    private Configuration configuration;

    /**
     * Sets up the view.
     * @param savedInstanceState Previous state of this activity, if it existed.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_test);
        configuration = Configuration.getInstance();
        setupPreferenceScreen();
        setCloseButton();
    }

    /**
     * Creates the view
     * @param inflater Android LayoutInflater
     * @param container Android ViewGroup
     * @param savedInstanceState This activity's previous state, is null if this activity has never
     *                           existed.
     * @return The view this method created.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        assert v != null;
        ListView lv = (ListView) v.findViewById(android.R.id.list);
        lv.setPadding(0, 0, 0, 0);
        return v;
    }

    /**
     * Finishes the activity if the home button is pressed on the device.
     * @param item Menu item that was selected.
     * @return Whether home or back was pressed.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                getActivity().finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Sets up the preference screen.
     */
    private void setupPreferenceScreen() {
        PreferenceCategory preferenceCategory = (PreferenceCategory) findPreference("test");
        final EMAType emaTypes[] = configuration.getEma_types();
        for (EMAType emaType : emaTypes) {
            if (emaType.getApplication() == null)
                continue;
            Preference preference = new Preference(getActivity());
            preference.setTitle(emaType.getName());
            try {
                Drawable icon = null;
                icon = getActivity().getPackageManager().getApplicationIcon(emaType.getApplication().getPackage_name());
                preference.setIcon(icon);
            } catch (PackageManager.NameNotFoundException ignored) {}
            final Application application = emaType.getApplication();
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                /**
                 * Creates an intent to start an activity.
                 * @param preference Preference clicked.
                 * @return Always returns false.
                 */
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = getActivity().getPackageManager()
                            .getLaunchIntentForPackage(application.getPackage_name());
                    intent.setAction(application.getPackage_name());
                    intent.putExtra("file_name", application.getFile_name());
                    intent.putExtra("id", application.getId());
                    intent.putExtra("name", application.getName());
                    intent.putExtra("timeout", application.getTimeout());
                    getActivity().startActivity(intent);
                    return false;
                }
            });
            preferenceCategory.addPreference(preference);
        }
    }

    /**
     * Creates a button to close the activity and sets an <code>onClickListener</code>.
     */
    private void setCloseButton() {
        final Button button = (Button) getActivity().findViewById(R.id.button_1);
        button.setText(R.string.button_close);
        button.setOnClickListener(new View.OnClickListener() {
            /**
             * Closes the activity.
             * @param v Button clicked.
             */
            public void onClick(View v) {
                getActivity().finish();
            }
        });
    }
}
