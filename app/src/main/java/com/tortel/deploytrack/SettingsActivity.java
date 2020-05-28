/*
 * Copyright (C) 2013-2020 Scott Warner
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
package com.tortel.deploytrack;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseUser;
import com.tortel.deploytrack.data.DatabaseManager;
import com.tortel.deploytrack.dialog.ScreenShotModeDialog;
import com.tortel.deploytrack.dialog.WelcomeDialog;
import com.tortel.deploytrack.provider.WidgetProvider;

/**
 * Settings activity
 */
public class SettingsActivity extends AppCompatActivity {
    private static final String KEY_THEME = "pref_light_theme";
    private static final String KEY_WELCOME = "pref_show_welcome";
    private static final String KEY_ABOUT_SCREENSHOT = "pref_show_about_screenshot";
    private static final String KEY_SYNC = "pref_sync_info";
    private static final String KEY_ANALYTICS = "pref_analytics";

	@Override
	public void onCreate(Bundle savedInstanceState){
        // Check for light theme
        Prefs.load(this);
        if(Prefs.useLightTheme()){
            setTheme(R.style.Theme_DeployThemeLight);
        }
		super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setOnMenuItemClickListener((MenuItem item) -> {
            if (item.getItemId() == android.R.id.home) {
                finish();
                return true;
            }
            return super.onOptionsItemSelected(item);
        });
        toolbar.setNavigationOnClickListener((View v) -> {
            this.finish();
        });

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, new SettingsFragment());
        transaction.commit();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		//Finish on the icon 'up' pressed
		case android.R.id.home:
			this.finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

    @Override
	public void onStop(){
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
        sendBroadcast(updateWidgetIntent);
	}

    /**
     * Fragment which shows the actual settings
     */
    public static class SettingsFragment extends PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);

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
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            DialogFragment dialog;
            FragmentManager fragMan = ((AppCompatActivity) getActivity()).getSupportFragmentManager();
            if(KEY_WELCOME.equals(preference.getKey())){
                dialog = new WelcomeDialog();
                dialog.show(fragMan, "welcome");
                return true;
            } else if(KEY_ABOUT_SCREENSHOT.equals(preference.getKey())){
                dialog = new ScreenShotModeDialog();
                dialog.show(fragMan, "screenshot");
                return true;
            }
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            // For theme changes, restart the app to apply it right away
            if(key.equals(KEY_THEME)){
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                getActivity().startActivity(intent);
            } else if(key.equals(KEY_ANALYTICS)){
                // Turn analytics on/off
                boolean value = sharedPreferences.getBoolean(KEY_ANALYTICS, true);
                Log.d("Analytics collection: "+value);
                FirebaseAnalytics.getInstance(getActivity()).setAnalyticsCollectionEnabled(value);
            }
        }
    }
}
