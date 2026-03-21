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
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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
        if (Prefs.useLightTheme()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
		androidx.activity.EdgeToEdge.enable(this);
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		View rootView = findViewById(R.id.rootView);

        // Apply the insets listener
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (view, windowInsets) -> {
            // Get the heights of the system bars (status bar, navigation bar, etc.)
            androidx.core.graphics.Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Apply the insets as padding to the view
            // This pushes the content of the view inwards so it doesn't overlap the system bars
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            // Return CONSUMED if you don't want the insets to be passed down to child views
            // Or return windowInsets if you want children to also have a chance to handle them.
            return WindowInsetsCompat.CONSUMED; 
        });

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
