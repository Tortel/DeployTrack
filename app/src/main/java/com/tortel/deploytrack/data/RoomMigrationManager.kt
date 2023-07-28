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
package com.tortel.deploytrack.data

import android.content.Context

import com.tortel.deploytrack.Log

import com.tortel.deploytrack.data.DatabaseManager.Companion.getInstance
import com.tortel.deploytrack.data.ormlite.ORMLiteDatabaseHelper
import com.tortel.deploytrack.data.ormlite.ORMLiteDatabaseManager
import com.tortel.deploytrack.data.ormlite.ORMLiteDeployment
import com.tortel.deploytrack.data.ormlite.ORMLiteWidgetInfo

/**
 * Class to handle the ORMLite -> Room database migration
 */
object RoomMigrationManager {
    /**
     * Check if the migration needs to be run
     * @param context application context
     * @return if the migration needs to be run or not
     */
    @JvmStatic
    fun needsMigration(context: Context): Boolean {
        return context.getDatabasePath(ORMLiteDatabaseHelper.DATABASE_NAME).exists()
    }

    /**
     * Migrate from ORMLite to Room
     * @param context
     */
    @JvmStatic
    fun doMigration(context: Context) {
        Log.d("Starting Room database migration")
        val ormManager = ORMLiteDatabaseManager.getInstance(context)
        val databaseManager = getInstance(context)
        val oldDeployments = ormManager.allDeployments
        val oldWidgetInfos = ormManager.allWidgetInfo
        val newDeployments: MutableList<Deployment> = ArrayList(oldDeployments.size)
        val newWidgetInfos: MutableList<WidgetInfo> = ArrayList(oldWidgetInfos.size)

        // Convert the old data classes to the new ones
        for (oldDeployment in oldDeployments) {
            newDeployments.add(convertORMDeployment(oldDeployment))
        }
        for (oldWidgetInfo in oldWidgetInfos) {
            newWidgetInfos.add(convertORMWidgetInfo(oldWidgetInfo))
        }

        // Save it all
        databaseManager.saveAllDeployments(*newDeployments.toTypedArray())
        databaseManager.saveAllWidgetInfo(*newWidgetInfos.toTypedArray())

        // Delete the old stuff
        if (!context.getDatabasePath(ORMLiteDatabaseHelper.DATABASE_NAME).delete()) {
            Log.e("Unable to delete old database")
        }
        // Done!
    }

    /**
     * Convert the old ORMLiteDeployment to the new class
     */
    private fun convertORMDeployment(old: ORMLiteDeployment): Deployment {
        return Deployment(
            old.name, old.uuid, old.startDate,
            old.endDate, old.completedColor, old.remainingColor
        )
    }

    /**
     * Convert an old ORMLiteWidgetInfo to the new class
     */
    private fun convertORMWidgetInfo(old: ORMLiteWidgetInfo): WidgetInfo {
        return WidgetInfo(
            old.id, old.deployment.uuid, old.isLightText,
            old.minWidth, old.minHeight, old.maxWidth, old.maxHeight
        )
    }
}