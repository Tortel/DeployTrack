/*
 * Copyright (C) 2013-2015 Scott Warner
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

/**
 * Class to assist with preferences
 */
public class Prefs {
	public static final String KEY_ANIMATION = "pref_animate";
	public static final String KEY_MAIN_VIEW = "pref_main_view";
	public static final String KEY_HIDE_DATE = "pref_hide_date";
	public static final String KEY_HIDE_PERCENT = "pref_hide_percent";
	public static final String KEY_LIGHT_THEME = "pref_light_theme";
	
	private static boolean animationEnabled;
	private static int mainDisplayType;
	private static boolean hideDate;
	private static boolean hidePercent;
	private static boolean lightTheme;
	
	public static void load(Context context){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		animationEnabled = prefs.getBoolean(KEY_ANIMATION, true);
		mainDisplayType = Integer.valueOf(prefs.getString(KEY_MAIN_VIEW, "0"));
		hideDate = prefs.getBoolean(KEY_HIDE_DATE, false);
		hidePercent = prefs.getBoolean(KEY_HIDE_PERCENT, false);
		lightTheme = prefs.getBoolean(KEY_LIGHT_THEME, false);
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
	
	public static boolean hidePercent(){
		return hidePercent;
	}
	
	public static boolean hideAll(){
	    return hidePercent && hideDate;
	}
	
	public static boolean useLightTheme() {
        return lightTheme;
    }

    public static void setScreenshotMode(boolean screenshotMode, Context context){
        if(screenshotMode){
            hideDate = true;
            mainDisplayType = ViewTypes.PERCENT;
        } else {
            load(context);
        }
    }

    public class ViewTypes{
		public static final int PERCENT = 0;
		public static final int COMPLETE = 1;
		public static final int REMAINING = 2;
	}
}
