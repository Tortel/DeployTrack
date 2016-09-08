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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Class to assist with preferences
 */
public class Prefs {
    private static final String KEY_ANIMATION = "pref_animate";
    private static final String KEY_MAIN_VIEW = "pref_main_view";
    private static final String KEY_HIDE_DATE = "pref_hide_date";
    private static final String KEY_HIDE_DAYS = "pref_hide_days";
    private static final String KEY_HIDE_PERCENT = "pref_hide_percent";
    private static final String KEY_LIGHT_THEME = "pref_light_theme";
    private static final String KEY_WELCOME = "welcome_2.0";
    private static final String KEY_SCREENSHOT = "about_screenshot";
    private static final String KEY_TOKEN = "token";
	
	private static boolean animationEnabled;
	private static int mainDisplayType;
	private static boolean hideDate;
    private static boolean hideDays;
	private static boolean hidePercent;
	private static boolean lightTheme;
    private static boolean welcomeShown;
    private static boolean aboutScreenShotShown;
	
	public static void load(Context context){
        SharedPreferences prefs = getPrefs(context);
		
		animationEnabled = prefs.getBoolean(KEY_ANIMATION, true);
		mainDisplayType = Integer.valueOf(prefs.getString(KEY_MAIN_VIEW, "0"));
		hideDate = prefs.getBoolean(KEY_HIDE_DATE, false);
        hideDays = prefs.getBoolean(KEY_HIDE_DAYS, false);
		hidePercent = prefs.getBoolean(KEY_HIDE_PERCENT, false);
		lightTheme = prefs.getBoolean(KEY_LIGHT_THEME, false);
        welcomeShown = prefs.getBoolean(KEY_WELCOME, false);
        aboutScreenShotShown = prefs.getBoolean(KEY_SCREENSHOT, false);
	}

    /**
     * Get the SharedPreferences
     * @param context
     * @return
     */
    private static SharedPreferences getPrefs(Context context){
        context = context.getApplicationContext();
        return PreferenceManager.getDefaultSharedPreferences(context);
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

    public static boolean hideDays(){
        return hideDays;
    }

	public static boolean hidePercent(){
		return hidePercent;
	}
	
	public static boolean useLightTheme() {
        return lightTheme;
    }

    public static boolean isWelcomeShown(){
        return welcomeShown;
    }

    public static boolean isAboutScreenShotShown(){
        return aboutScreenShotShown;
    }

    public static void setWelcomeShown(Context context){
        SharedPreferences prefs = getPrefs(context);
        prefs.edit().putBoolean(KEY_WELCOME, true).apply();
        welcomeShown = true;
    }

    public static void setAboutScreenShotShown(Context context){
        SharedPreferences prefs = getPrefs(context);
        prefs.edit().putBoolean(KEY_SCREENSHOT, true).apply();
        aboutScreenShotShown = true;
    }

    public static void setScreenShotMode(boolean screenShotMode, Context context){
        if(screenShotMode){
            hideDate = true;
            hideDays = true;
            hidePercent = true;
        } else {
            load(context);
        }
    }

    /**
     * Set the Google account token
     * @param token
     * @param context
     */
    public static void setToken(String token, Context context){
        SharedPreferences prefs = getPrefs(context);
        prefs.edit().putString(KEY_TOKEN, token).apply();
    }

    /**
     * Get the Google account token
     * @param context
     * @return
     */
    public static String getToken(Context context){
        return getPrefs(context).getString(KEY_TOKEN, null);
    }

    public class ViewTypes{
		public static final int PERCENT = 0;
		public static final int COMPLETE = 1;
		public static final int REMAINING = 2;
	}
}
