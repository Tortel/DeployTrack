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

/**
 * Contains constants releated to analytics
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

    // Hide the constructor
    private Analytics(){}
}
