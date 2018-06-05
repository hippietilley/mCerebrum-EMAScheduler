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

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.messagehandler.OnConnectionListener;
import org.md2k.ema_scheduler.incentive.IncentiveManager;
import org.md2k.utilities.UI.AlertDialogs;

import java.util.Locale;

/**
 * Preference Fragment for <code>ActivityIncentiveSettings</code>.
 */
public class PrefsFragmentIncentiveSettings extends PreferenceFragment {
    private static final String TAG = PrefsFragmentIncentiveSettings.class.getSimpleName();
    private static final String KEY_CURRENT_INCENTIVE = "key_current_incentive";
    private static final String KEY_UPDATE_INCENTIVE = "key_update_incentive";
    private boolean isAlreadyConnected=false;
    private DataKitAPI dataKitAPI;
    private IncentiveManager incentiveManager;

    /**
     * Sets up the view and connects to <code>DataKitAPI</code>.
     * @param savedInstanceState Previous state of this activity, if it existed.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_incentive_settings);
        setCloseButton();
        try {
            dataKitAPI = DataKitAPI.getInstance(getActivity());
            isAlreadyConnected = dataKitAPI.isConnected();
            if(!isAlreadyConnected){
                    dataKitAPI.connect(new OnConnectionListener() {
                        /**
                         * Initializes a new <code>IncentiveManager</code> and
                         * calls <code>updatePreferenceScreen()</code>.
                         */
                        @Override
                        public void onConnected() {
                            try {
                                incentiveManager = new IncentiveManager(getActivity());
                                updatePreferenceScreen();
                            } catch (DataKitException e) {
                                getActivity().finish();
                            }
                        }
                    });
            }else{
                incentiveManager = new IncentiveManager(getActivity());
                updatePreferenceScreen();
            }
        } catch (DataKitException e) {
            getActivity().finish();
        }
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
     * Sets up the preference screen.
     */
    private void updatePreferenceScreen() {
        Preference preference = findPreference(KEY_CURRENT_INCENTIVE);
        if(dataKitAPI.isConnected()){
            double totalIncentive = 0;
            try {
                totalIncentive = incentiveManager.getLastTotalIncentive();
            } catch (DataKitException e) {
                getActivity().finish();
            }
            preference.setSummary(String.format(Locale.ENGLISH,"$%.2f",totalIncentive));
        }else{
            preference.setSummary("not connected");
        }
        EditTextPreference editTextPreference = (EditTextPreference) findPreference(KEY_UPDATE_INCENTIVE);
        editTextPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            /**
             * Parses the new value and verifies with the user before attempting to save the new value
             * with <code>DataKit</code>.
             * @param preference Preference changed.
             * @param newValue New value for the given preference.
             * @return Always returns false.
             */
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                try {
                    final double incentive;
                    double totalIncentive, currentIncentive;
                    incentive = Double.parseDouble(newValue.toString());
                    currentIncentive = incentiveManager.getLastTotalIncentive();
                    totalIncentive = currentIncentive + incentive;
                    String totalIncentiveStr = String.format(Locale.ENGLISH, "%.2f", totalIncentive);
                    String incentiveStr = String.format(Locale.ENGLISH, "%.2f", incentive);
                    String currentIncentiveStr = String.format(Locale.ENGLISH, "%.2f", currentIncentive);
                    String msg = "Total Incentive will be $" + currentIncentive;
                    if(incentive>0)
                        msg += "+$" + incentiveStr;
                    else
                        msg += "$" + incentiveStr;
                    msg += "=$" + totalIncentiveStr;
                    AlertDialogs.AlertDialog(getActivity(), "Add $" + incentiveStr + " to incentive?",
                            msg, R.drawable.ic_info_teal_48dp, "Yes", "Cancel", null, new DialogInterface.OnClickListener() {
                                /**
                                 * Saves the incentive to <code>DataKit</code>.
                                 * @param dialog Dialog clicked.
                                 * @param which Button clicked.
                                 */
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(which == Dialog.BUTTON_POSITIVE){
                                try {
                                    incentiveManager.saveIncentiveAdminToDataKit(incentive);
                                    updatePreferenceScreen();
                                } catch (DataKitException e) {
                                    getActivity().finish();
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    Toast.makeText(getActivity(),"!!!ERROR: Can't be done, please try again..",Toast.LENGTH_LONG).show();
                }
                return false;
            }
        });
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

    /**
     * Disconnects <code>DataKitAPI</code> upon destruction.
     */
    @Override
    public void onDestroy(){
        if(!isAlreadyConnected && dataKitAPI.isConnected())
            dataKitAPI.disconnect();
        super.onDestroy();
    }
}
