/*
 * Copyright (C) 2013-2023 Scott Warner
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

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.tortel.deploytrack.fragments.MainFragment;

/**
 * The main entrypoint to the app
 */
public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        // Check for light theme
        Prefs.load(this);
        if(Prefs.useLightTheme()){
            setTheme(R.style.DeployTheme);
        }
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(this);
		if (BuildConfig.DEBUG) {
			// Mark debug builds in crashlytics
			FirebaseCrashlytics.getInstance().setCustomKey("debug", true);
			firebaseAnalytics.setAnalyticsCollectionEnabled(false);
		}

		if (savedInstanceState == null) {
			firebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, null);
		}
	}

}
