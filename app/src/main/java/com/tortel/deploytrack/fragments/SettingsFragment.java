/*
 * Copyright (C) 2023 Scott Warner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tortel.deploytrack.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseUser;
import com.tortel.deploytrack.Log;
import com.tortel.deploytrack.MainActivity;
import com.tortel.deploytrack.R;
import com.tortel.deploytrack.data.DatabaseManager;
import com.tortel.deploytrack.databinding.FragmentSettingsBinding;
import com.tortel.deploytrack.dialog.ScreenShotModeDialog;
import com.tortel.deploytrack.dialog.WelcomeDialog;
import com.tortel.deploytrack.provider.WidgetProvider;

/**
 * Fragment that handles settings
 */
public class SettingsFragment extends Fragment {
    private static final String KEY_THEME = "pref_light_theme";
    private static final String KEY_WELCOME = "pref_show_welcome";
    private static final String KEY_ABOUT_SCREENSHOT = "pref_show_about_screenshot";
    private static final String KEY_SYNC = "pref_sync_info";
    private static final String KEY_ANALYTICS = "pref_analytics";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentSettingsBinding binding = FragmentSettingsBinding.inflate(inflater, container, false);

        binding.toolbar.setOnMenuItemClickListener((MenuItem item) -> {
            if (item.getItemId() == android.R.id.home) {
                NavHostFragment.findNavController(this).navigateUp();
                return true;
            }
            return super.onOptionsItemSelected(item);
        });
        binding.toolbar.setNavigationOnClickListener((View v) -> {
            NavHostFragment.findNavController(this).navigateUp();
        });

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, new PreferenceFragment());
        transaction.commit();

        return binding.getRoot();
    }

    @Override
    public void onStop() {
        super.onStop();
        sendWidgetUpdateBroadcast();
    }

    /**
     * Send a broadcast to force the widgets to update
     */
    private void sendWidgetUpdateBroadcast(){
        Log.v("Sending widget update broadcast");
        // Force the widgets to update
        Intent updateWidgetIntent = new Intent(WidgetProvider.UPDATE_INTENT);
        getContext().sendBroadcast(updateWidgetIntent);
    }

    /**
     * Fragment which shows the actual settings
     */
    public static class PreferenceFragment extends PreferenceFragmentCompat
            implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);
            setSyncStatus();
        }

        /**
         * Set the title of the sync preference to show the current email address
         */
        private void setSyncStatus(){
            FirebaseUser currentUser = DatabaseManager.getInstance(getActivity()).getFirebaseUser();
            Preference syncPref = getPreferenceScreen().findPreference(KEY_SYNC);
            if(currentUser != null){
                // Show that sync is enabled
                syncPref.setTitle(getString(R.string.pref_sync_enabled, currentUser.getEmail()));
            } else {
                // Show that sync is disabled
                syncPref.setTitle(R.string.pref_sync_not_enabled);
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            setSyncStatus();

            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);

        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            if (preference.hasKey()) {
                DialogFragment dialog;
                FragmentManager fragMan = getParentFragmentManager();
                switch (preference.getKey()) {
                    case KEY_WELCOME:
                        dialog = new WelcomeDialog();
                        dialog.show(fragMan, "welcome");
                        return true;
                    case KEY_ABOUT_SCREENSHOT:
                        dialog = new ScreenShotModeDialog();
                        dialog.show(fragMan, "screenshot");
                        return true;
                    case KEY_SYNC:
                        NavHostFragment.findNavController(this)
                                .navigate(SettingsFragmentDirections.settingsToSync());
                        return true;
                }
            }
            return super.onPreferenceTreeClick(preference);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            // For theme changes, restart the app to apply it right away
            switch (key) {
                case KEY_THEME:
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    getActivity().startActivity(intent);
                    break;
                case KEY_ANALYTICS:
                    // Turn analytics on/off
                    boolean value = sharedPreferences.getBoolean(KEY_ANALYTICS, true);
                    Log.d("Analytics collection: "+value);
                    FirebaseAnalytics.getInstance(getActivity()).setAnalyticsCollectionEnabled(value);
                    break;
            }
        }
    }
}
