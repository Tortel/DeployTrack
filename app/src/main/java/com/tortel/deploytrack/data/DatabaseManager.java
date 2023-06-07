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
package com.tortel.deploytrack.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.tortel.deploytrack.Log;

import java.util.Collections;
import java.util.List;

/**
 * Class to manage interaction with the database
 */
public class DatabaseManager {
    public static final String DATA_ADDED = "com.tortel.deploytrack.DATA_ADDED";
    public static final String DATA_DELETED = "com.tortel.deploytrack.DATA_DELETED";
    public static final String DATA_CHANGED = "com.tortel.deploytrack.DATA_CHANGED";

    private static final String DATABASE_NAME = "data.2023";

    private static DatabaseManager instance;
    private final AppDatabase mAppDatabase;
    private final DataDao mDao;
    private final FirebaseDBManager mFirebaseDBManager;

    public static DatabaseManager getInstance(@NonNull Context context) {
        if (instance == null) {
            instance = new DatabaseManager(context);
        }
        return instance;
    }

    private DatabaseManager(@NonNull Context context) {
        mAppDatabase = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, DATABASE_NAME).build();
        mDao = mAppDatabase.getDao();
        mFirebaseDBManager = FirebaseDBManager.getInstance(this, context.getApplicationContext());
    }

    /**
     * Save a deployment object to the database
     */
    public void saveDeployment(Deployment deployment) {
        try {
            mDao.insert(deployment);
            mFirebaseDBManager.saveDeployment(deployment);
        } catch (Exception e) {
            Log.e("Error saving Deployment", e);
            // Report this to firebase
            FirebaseCrashlytics.getInstance().recordException(new Exception("Exception saving deployment", e));
        }
    }

    /**
     * Set the Firebase user object, enabling Firebase sync
     */
    public void setFirebaseUser(FirebaseUser fbUser) {
        mFirebaseDBManager.setUser(fbUser);
        // Sync it
        mFirebaseDBManager.syncAll();
    }

    /**
     * Get the current Firebase user, if present
     */
    public FirebaseUser getFirebaseUser() {
        return mFirebaseDBManager.getUser();
    }

    /**
     * Get all the saved GeoEvents
     */
    public List<Deployment> getAllDeployments() {
        try {
            List<Deployment> list = mDao.getAllDeployments();
            Collections.sort(list);

            return list;
        } catch (Exception e) {
            Log.e("Exception getting all Deployments", e);
            // Report this to firebase
            FirebaseCrashlytics.getInstance().recordException(new Exception("Exception getting all deployments", e));
        }
        return null;
    }

    /**
     * Get a specific Deployment from the database
     */
    public Deployment getDeployment(String uuid) {
        Deployment tmp = null;
        try {
            return mDao.getDeployment(uuid);
        } catch (Exception e) {
            Log.e("Exception getting Deployment", e);
            // Report this to firebase
            FirebaseCrashlytics.getInstance().recordException(new Exception("Exception getting deployment", e));
        }
        return null;
    }

    /**
     * Delete a specific Deployment from the database
     */
    public void deleteDeployment(String uuid) {
        deleteDeployment(uuid, true);
    }

    /**
     * Delete a deployment
     */
    void deleteDeployment(String uuid, boolean includeFirebase) {
        Log.v("Deleting deployment " + uuid);
        try {
            if (includeFirebase) {
                mFirebaseDBManager.deleteDeployment(uuid);
            }
            mDao.deleteByUuid(uuid);
        } catch (Exception e) {
            Log.e("Exception deleting Deployment", e);
            // Report this to firebase
            FirebaseCrashlytics.getInstance().recordException(new Exception("Exception deleting deployment", e));
        }
    }

    /**
     * Gets the widget information for the specified ID
     */
    public List<WidgetInfo> getAllWidgetInfo() {
        Log.v("Getting all widget information");
        try {
            return mDao.getAllWidgetInfo();
        } catch (Exception e) {
            Log.e("Exception getting widget info", e);
            // Report this to firebase
            FirebaseCrashlytics.getInstance().recordException(new Exception("Exception getting all widget info", e));
            return null;
        }
    }

    /**
     * Gets the widget information for the specified ID
     */
    public WidgetInfo getWidgetInfo(int id) {
        Log.v("Getting widget info for " + id);
        try {
            return mDao.getWidgetInfo(id);
        } catch (Exception e) {
            Log.e("Exception getting widget info", e);
            // Report this to firebase
            FirebaseCrashlytics.getInstance().recordException(new Exception("Exception getting all widget info", e));
            return null;
        }
    }

    /**
     * Saves the WidgetInfo
     */
    public void saveWidgetInfo(@NonNull WidgetInfo info) {
        Log.v("Saving widget info for " + info.getId());
        try {
            mDao.insert(info);
        } catch (Exception e) {
            Log.e("Exception saving widget info", e);
            // Report this to firebase
            FirebaseCrashlytics.getInstance().recordException(new Exception("Exception saving widget info", e));
        }
    }

    /**
     * Deletes the widget information from the database
     */
    public void deleteWidgetInfo(int id) {
        Log.v("Deleting widget info " + id);
        try {
            mDao.deleteById(id);
        } catch (Exception e) {
            Log.e("Error deleting widget info", e);
            // Report this to firebase
            FirebaseCrashlytics.getInstance().recordException(new Exception("Exception deleting eidget info", e));
        }
    }

}