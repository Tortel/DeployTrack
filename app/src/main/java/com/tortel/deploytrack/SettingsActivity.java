/*
 * Copyright (C) 2013-2016 Scott Warner
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
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import com.tortel.deploytrack.dialog.ScreenShotModeDialog;
import com.tortel.deploytrack.dialog.WelcomeDialog;
import com.tortel.deploytrack.provider.WidgetProvider;


public class SettingsActivity extends ActionBarActivity {
    private static final String KEY_THEME = "pref_light_theme";
    private static final String KEY_WELCOME = "pref_show_welcome";
    private static final String KEY_ABOUT_SCREENSHOT = "pref_show_about_screenshot";

	@Override
	public void onCreate(Bundle savedInstanceState){
        // Check for light theme
        Prefs.load(this);
        if(Prefs.useLightTheme()){
            setTheme(R.style.Theme_DeployThemeLight);
        }
		super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
	
	public void onStop(){
	    super.onStop();
	    sendWidgetUpdateBroadcast();
	}
	
	private void sendWidgetUpdateBroadcast(){
	    Log.v("Sending widget update broadcast");
	    // Force the widgets to update
        Intent updateWidgetIntent = new Intent(WidgetProvider.UPDATE_INTENT);
        sendBroadcast(updateWidgetIntent);
	}

    public static class SettingsFragment extends PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);
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
            FragmentManager fragMan = ((ActionBarActivity) getActivity()).getSupportFragmentManager();
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
            }
        }
    }
}
