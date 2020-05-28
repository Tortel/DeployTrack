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

import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Contains constants related to analytics
 */
public class Analytics {
    /**
     * Event when a user edits a deployment
     */
    public static final String EVENT_EDITED_DEPLOYMENT = "deploy_edit";
    /**
     * Event when a user creates a deployment
     */
    public static final String EVENT_CREATED_DEPLOYMENT = "deploy_edit";
    /**
     * Event when a user deletes a deployment
     */
    public static final String EVENT_DELETED_DEPLOYMENT = "deploy_delete";

    /**
     * How many deployments the user has
     */
    public static final String PROPERTY_DEPLOYMENT_COUNT = "deployment_count";
    /**
     * How many WidgetInfo objects are saved
     */
    public static final String PROPERTY_WIDGET_COUNT = "widget_count";
    /**
     * The user's theme (light/dark)
     */
    public static final String PROPERTY_THEME = "theme";
    /**
     * The user's main view
     */
    public static final String PROPERTY_MAIN_VIEW = "main_view";
    /**
     * If the user has the dates hidden
     */
    public static final String PROPERTY_HIDE_DATES = "hide_dates";
    /**
     * If the user has the days completed/remaining hidden
     */
    public static final String PROPERTY_HIDE_DAY_COUNTS = "hide_counts";
    /**
     * If the user has the percentage hidden
     */
    public static final String PROPERTY_HIDE_PERCENT = "hide_percent";

    /**
     * Record the user's preferences to the analytics service
     * @param analytics FB analytics object
     */
    public static void recordPreferences(FirebaseAnalytics analytics, boolean screenshotMode){
        // The user's theme
        if(Prefs.useLightTheme()){
            analytics.setUserProperty(PROPERTY_THEME, "light");
        } else {
            analytics.setUserProperty(PROPERTY_THEME, "dark");
        }

        // The main view type
        switch (Prefs.getMainDisplayType()){
            case Prefs.ViewTypes.COMPLETE:
                analytics.setUserProperty(PROPERTY_MAIN_VIEW, "complete");
                break;
            case Prefs.ViewTypes.REMAINING:
                analytics.setUserProperty(PROPERTY_MAIN_VIEW, "remaining");
                break;
            case Prefs.ViewTypes.PERCENT:
                analytics.setUserProperty(PROPERTY_MAIN_VIEW, "percent");
                break;
        }

        // Hide views
        // Disable setting this if screenshot mode is enabled
        if(!screenshotMode) {
            analytics.setUserProperty(PROPERTY_HIDE_DATES, "" + Prefs.hideDate());
            analytics.setUserProperty(PROPERTY_HIDE_DAY_COUNTS, "" + Prefs.hideDays());
            analytics.setUserProperty(PROPERTY_HIDE_PERCENT, "" + Prefs.hidePercent());
        }
    }

    // Hide the constructor
    private Analytics(){}
}
