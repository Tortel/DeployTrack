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
package com.tortel.deploytrack.data;

import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseUser;
import com.tortel.deploytrack.Log;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Class to manage interaction with the database
 */
public class DatabaseManager {
    public static final String DATA_ADDED = "com.tortel.deploytrack.DATA_ADDED";
    public static final String DATA_DELETED = "com.tortel.deploytrack.DATA_DELETED";
    public static final String DATA_CHANGED = "com.tortel.deploytrack.DATA_CHANGED";

    private static DatabaseManager instance;

    private final DatabaseHelper mHelper;
    private final FirebaseDBManager mFirebaseDBManager;

    public static DatabaseManager getInstance(Context context){
        if(instance == null){
            instance = new DatabaseManager(context);
        }
        return instance;
    }

    private DatabaseManager(Context context){
        mHelper = new DatabaseHelper(context.getApplicationContext());
        mFirebaseDBManager = FirebaseDBManager.getInstance(this, context.getApplicationContext());
    }

    /**
     * Save a deployment object to the database
     */
    public void saveDeployment(Deployment deployment){
        try{
            // Set a UUID if there isnt one
            if(deployment.getUuid() == null){
                deployment.setUuid(UUID.randomUUID());
            }

            mHelper.getDeploymentDao().createOrUpdate(deployment);
            mFirebaseDBManager.saveDeployment(deployment);
        } catch(SQLException e){
            Log.e("Error saving Deployment", e);
            // Report this to firebase
            Crashlytics.logException(new Exception("Exception saving deployment", e));
        }
    }

    /**
     * Set the Firebase user object, enabling Firebase sync
     */
    public void setFirebaseUser(FirebaseUser fbUser){
        mFirebaseDBManager.setUser(fbUser);
        // Sync it
        mFirebaseDBManager.syncAll();
    }

    /**
     * Get the current Firebase user, if present
     */
    public FirebaseUser getFirebaseUser(){
        return mFirebaseDBManager.getUser();
    }

    /**
     * Get all the saved GeoEvents
     */
    public List<Deployment> getAllDeployments(){
        List<Deployment> list = null;
        try{
            list = mHelper.getDeploymentDao().queryForAll();
            Collections.sort(list);
        } catch(SQLException e){
            Log.e("Exception getting all Deployments", e);
            // Report this to firebase
            Crashlytics.logException(new Exception("Exception getting all deployments", e));
        }
        return list;
    }
    
    /**
     * Get a specific Deployment from the database
     */
    public Deployment getDeployment(String uuid){
        Deployment tmp = null;
        try{
            tmp = mHelper.getDeploymentDao().queryForId(uuid);
        } catch(SQLException e){
            Log.e("Exception getting Deployment", e);
            // Report this to firebase
            Crashlytics.logException(new Exception("Exception getting deployment", e));
        }
        return tmp;
    }
    
    /**
     * Delete a specific Deployment from the database
     */
    public void deleteDeployment(String uuid){
        deleteDeployment(uuid, true);
    }

    /**
     * Delete a deployment
     */
    void deleteDeployment(String uuid, boolean includeFirebase){
        Log.v("Deleting deployment "+uuid);
        try{
            if(includeFirebase) {
                mFirebaseDBManager.deleteDeployment(uuid);
            }
            mHelper.getDeploymentDao().deleteById(uuid);
        } catch(SQLException e){
            Log.e("Exception deleting Deployment", e);
            // Report this to firebase
            Crashlytics.logException(new Exception("Exception deleting deployment", e));
        }
    }
    
    /**
     * Gets the widget information for the specified ID
     */
    public List<WidgetInfo> getAllWidgetInfo(){
        Log.v("Getting all widget information");
        try{
            List<WidgetInfo> list = mHelper.getWidgetInfoDao().queryForAll();
            if(list != null){
                for(WidgetInfo info : list){
                    mHelper.getDeploymentDao().refresh(info.getDeployment());
                }
            }
            return list; 
        } catch(SQLException e){
            Log.e("Exception getting widget info", e);
            // Report this to firebase
            Crashlytics.logException(new Exception("Exception getting all widget info", e));
            return null;
        }
    }
    
    /**
     * Gets the widget information for the specified ID
     */
    public WidgetInfo getWidgetInfo(int id){
        Log.v("Getting widget info for "+id);
        try{
            WidgetInfo info = mHelper.getWidgetInfoDao().queryForId(id);
            if(info != null){
                mHelper.getDeploymentDao().refresh(info.getDeployment());
            }
            return info; 
        } catch(SQLException e){
            Log.e("Exception getting widget info", e);
            // Report this to firebase
            Crashlytics.logException(new Exception("Exception getting all widget info", e));
            return null;
        }
    }

    /**
     * Saves the WidgetInfo
     */
    public void saveWidgetInfo(WidgetInfo info){
        Log.v("Saving widget info for "+info.getId());
        try{
            mHelper.getWidgetInfoDao().createOrUpdate(info);
        } catch(SQLException e){
            Log.e("Exception saving widget info", e);
            // Report this to firebase
            Crashlytics.logException(new Exception("Exception saving widget info", e));
        }
    }
    
    /**
     * Deletes the widget information from the database
     */
    public void deleteWidgetInfo(int id){
        Log.v("Deleting widget info "+id);
        try{
            mHelper.getWidgetInfoDao().deleteById(id);
        } catch(SQLException e){
            Log.e("Error deleting widget info", e);
            // Report this to firebase
            Crashlytics.logException(new Exception("Exception deleting eidget info", e));
        }
    }

}