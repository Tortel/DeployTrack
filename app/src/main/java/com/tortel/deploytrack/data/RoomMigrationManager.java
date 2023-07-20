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

import com.tortel.deploytrack.Log;
import com.tortel.deploytrack.data.ormlite.ORMLiteDatabaseHelper;
import com.tortel.deploytrack.data.ormlite.ORMLiteDatabaseManager;
import com.tortel.deploytrack.data.ormlite.ORMLiteDeployment;
import com.tortel.deploytrack.data.ormlite.ORMLiteWidgetInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to handle the ORMLite -> Room database migration
 */
public class RoomMigrationManager {

    /**
     * Check if the migration needs to be run
     * @param context application context
     * @return if the migration needs to be run or not
     */
    public static boolean needsMigration(Context context) {
        return context.getDatabasePath(ORMLiteDatabaseHelper.DATABASE_NAME).exists();
    }

    /**
     * Migrate from ORMLite to Room
     * @param context
     */
    public static void doMigration(Context context) {
        Log.d("Starting Room database migration");
        ORMLiteDatabaseManager ormManager = ORMLiteDatabaseManager.getInstance(context);
        DatabaseManager databaseManager = DatabaseManager.getInstance(context);

        List<ORMLiteDeployment> oldDeployments = ormManager.getAllDeployments();
        List<ORMLiteWidgetInfo> oldWidgetInfos = ormManager.getAllWidgetInfo();

        List<Deployment> newDeployments = new ArrayList<>(oldDeployments.size());
        List<WidgetInfo> newWidgetInfos = new ArrayList<>(oldWidgetInfos.size());

        // Convert the old data classes to the new ones
        for (ORMLiteDeployment oldDeployment : oldDeployments) {
            newDeployments.add(convertORMDeployment(oldDeployment));
        }
        for (ORMLiteWidgetInfo oldWidgetInfo : oldWidgetInfos) {
            newWidgetInfos.add(convertORMWidgetInfo(oldWidgetInfo));
        }

        // Save it all
        databaseManager.saveAllDeployments(newDeployments.toArray(new Deployment[]{}));
        databaseManager.saveAllWidgetInfo(newWidgetInfos.toArray(new WidgetInfo[]{}));

        // Delete the old stuff
        if (!context.getDatabasePath(ORMLiteDatabaseHelper.DATABASE_NAME).delete()) {
            Log.e("Unable to delete old database");
        }
        // Done!
    }

    /**
     * Convert the old ORMLiteDeployment to the new class
     */
    @NonNull
    private static Deployment convertORMDeployment(@NonNull ORMLiteDeployment old) {
        return new Deployment(old.getName(), old.getUuid(), old.getStartDate(),
                old.getEndDate(), old.getCompletedColor(), old.getRemainingColor());
    }

    /**
     * Convert an old ORMLiteWidgetInfo to the new class
     */
    @NonNull
    private static WidgetInfo convertORMWidgetInfo(@NonNull ORMLiteWidgetInfo old) {
        return new WidgetInfo(old.getId(), old.getDeployment().getUuid(), old.isLightText(),
                old.getMinWidth(), old.getMinHeight(), old.getMaxWidth(), old.getMaxHeight());
    }
}
