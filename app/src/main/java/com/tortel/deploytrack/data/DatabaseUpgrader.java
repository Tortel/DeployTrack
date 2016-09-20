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
import android.util.SparseArray;

import com.google.firebase.crash.FirebaseCrash;
import com.j256.ormlite.dao.Dao;
import com.tortel.deploytrack.Log;
import com.tortel.deploytrack.data.depricated.*;

import java.util.List;
import java.util.UUID;

/**
 * Class for handing the major database upgrade
 */
public class DatabaseUpgrader {
    // Hide
    private DatabaseUpgrader(){}

    /**
     * Checks if the database needs to be upgraded
     */
    public static boolean needsUpgrade(Context context){
        return OldDatabaseHelper.oldDatabaseExists(context);
    }

    /**
     * Run the database upgrade.
     * @param context the context
     */
    public static boolean doDatabaseUpgrade(Context context){
        DatabaseManager dbManager = DatabaseManager.getInstance(context);
        OldDatabaseHelper oldDbHelper = new OldDatabaseHelper(context);
        Log.d("Starting DB upgrade");
        try{
            Dao<OldDeployment, Integer> oldDeploymentDao = oldDbHelper.getDeploymentDao();
            Dao<OldWidgetInfo, Integer> oldWidgetDao = oldDbHelper.getWidgetInfoDao();
            SparseArray<Deployment> deploymentById = new SparseArray<>();

            // Get all the data from the old database and shove it in the new one
            List<OldDeployment> oldDeployments = oldDeploymentDao.queryForAll();
            for(OldDeployment cur : oldDeployments){
                Log.d("Updating old deployment with ID "+cur.getId());
                // Make sure the UUID is set
                if(cur.getUuid() == null){
                    cur.setUuid(UUID.randomUUID());
                }
                Deployment updated = cur.getUpdatedObject();

                // Add it to our map of deployment objects for updating any WidgetInfo objects
                deploymentById.put(cur.getId(), updated);

                // Save it
                dbManager.saveDeployment(updated);
            }

            Log.d("Deployment objects updated");

            List<OldWidgetInfo> oldWidgetInfo = oldWidgetDao.queryForAll();
            for(OldWidgetInfo cur : oldWidgetInfo){
                Log.d("Updating WidgetInfo with ID "+cur.getId());

                Deployment deployment = deploymentById.get(cur.getDeployment().getId());
                WidgetInfo updated = cur.getUpdatedObject(deployment);

                dbManager.saveWidgetInfo(updated);
            }

            Log.d("Upgrade complete, deleting old database files");
            OldDatabaseHelper.deleteDbFiles(context);
            // All done
            return true;
        } catch (Exception e){
            Log.e("Exception during database upgrade", e);
            // Report this to firebase
            FirebaseCrash.report(new Exception("Exception during database upgrade", e));
            // Uh-oh
            return false;
        }
    }
}
