/*
 * Copyright (C) 2013 Scott Warner
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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Prefs {
	public static final String KEY_ANIMATION = "pref_animate";
	public static final String KEY_MAIN_VIEW = "pref_main_view";
	public static final String KEY_HIDE_DATE = "pref_hide_date";
	
	private static boolean animationEnabled;
	private static int mainDisplayType;
	private static boolean hideDate;
	
	public static void load(Context context){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		animationEnabled = prefs.getBoolean(KEY_ANIMATION, true);
		mainDisplayType = Integer.valueOf(prefs.getString(KEY_MAIN_VIEW, "0"));
		hideDate = prefs.getBoolean(KEY_HIDE_DATE, false);
	}
	
	public static boolean isAnimationEnabled() {
		return animationEnabled;
	}

	public static int getMainDisplayType() {
		return mainDisplayType;
	}
	
	public static boolean hideDate(){
	    return hideDate;
	}
	
	public class ViewTypes{
		public static final int PERCENT = 0;
		public static final int COMPLETE = 1;
		public static final int REMAINING = 2;
	}
}
