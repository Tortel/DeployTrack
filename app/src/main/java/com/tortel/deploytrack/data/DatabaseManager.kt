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
import androidx.lifecycle.LiveData

import androidx.room.Room.databaseBuilder

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.crashlytics.FirebaseCrashlytics

import com.tortel.deploytrack.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Class to manage interaction with the database
 */
@Singleton
class DatabaseManager @Inject constructor(@ApplicationContext context: Context) {
    private val mAppDatabase: AppDatabase
    private val mDao: DataDao
    private val mFirebaseDBManager: FirebaseDBManager

    init {
        mAppDatabase = databaseBuilder(context.applicationContext, AppDatabase::class.java, DATABASE_NAME).build()
        mDao = mAppDatabase.dao
        mFirebaseDBManager = FirebaseDBManager.getInstance(this, context.applicationContext)
    }

    /**
     * Save a deployment object to the database
     */
    fun saveDeployment(deployment: Deployment) {
        try {
            mDao.insert(deployment)
            mFirebaseDBManager.saveDeployment(deployment)
        } catch (e: Exception) {
            Log.e("Error saving Deployment", e)
            // Report this to firebase
            FirebaseCrashlytics.getInstance().recordException(Exception("Exception saving deployment", e))
        }
    }

    /**
     * Bulk save deployments
     */
    fun saveAllDeployments(vararg deployments: Deployment) {
        try {
            mDao.insert(*deployments)
        } catch (e: Exception) {
            Log.e("Exception saving deployments")
            // Report this to firebase
            FirebaseCrashlytics.getInstance().recordException(Exception("Exception saving deployments", e))
        }
    }

    /**
     * Bulk save WidgetInfo
     */
    fun saveAllWidgetInfo(vararg widgetInfos: WidgetInfo) {
        try {
            mDao.insert(*widgetInfos)
        } catch (e: Exception) {
            Log.e("Exception saving widget info")
            // Report this to firebase
            FirebaseCrashlytics.getInstance().recordException(Exception("Exception saving widget info", e))
        }
    }
    /**
     * Get the current Firebase user, if present
     */// Sync it
    /**
     * Set the Firebase user object, enabling Firebase sync
     */
    var firebaseUser: FirebaseUser?
        get() = mFirebaseDBManager.user
        set(fbUser) {
            mFirebaseDBManager.user = fbUser
            // Sync it
            mFirebaseDBManager.syncAll()
        }

    /**
     * Get all the saved GeoEvents
     */
    val allDeployments: LiveData<List<Deployment>>
        get() {
            return mDao.allDeployments
        }

    val allDeploymentsSync: List<Deployment>
        get() {
            return mDao.allDeploymentsSync
        }

    /**
     * Get a specific Deployment from the database
     */
    fun getDeployment(uuid: String): LiveData<Deployment>? {
        return mDao.getDeployment(uuid)
    }

    fun getDeploymentSync(uuid: String): Deployment? {
        return mDao.getDeploymentSync(uuid)
    }

    /**
     * Delete a specific Deployment from the database
     */
    fun deleteDeployment(uuid: String) {
        deleteDeployment(uuid, true)
    }

    /**
     * Delete a deployment
     */
    fun deleteDeployment(uuid: String, includeFirebase: Boolean) {
        Log.v("Deleting deployment $uuid")
        try {
            if (includeFirebase) {
                mFirebaseDBManager.deleteDeployment(uuid)
            }
            mDao.deleteByUuid(uuid)
        } catch (e: Exception) {
            Log.e("Exception deleting Deployment", e)
            // Report this to firebase
            FirebaseCrashlytics.getInstance().recordException(Exception("Exception deleting deployment", e))
        }
    }

    /**
     * Gets the widget information for the specified ID
     */
    val allWidgetInfo: List<WidgetInfo>?
        get() {
            Log.v("Getting all widget information")
            return try {
                mDao.allWidgetInfo
            } catch (e: Exception) {
                Log.e("Exception getting widget info", e)
                // Report this to firebase
                FirebaseCrashlytics.getInstance().recordException(Exception("Exception getting all widget info", e))
                return null
            }
        }

    /**
     * Gets the widget information for the specified ID
     */
    fun getWidgetInfo(id: Int): WidgetInfo? {
        Log.v("Getting widget info for $id")
        return try {
            val info = mDao.getWidgetInfo(id)
            // Populate the deployment object
            if (info != null) {
                val deployment = mDao.getDeployment(info.deploymentId)
                //info.deployment = deployment
            }
            info
        } catch (e: Exception) {
            Log.e("Exception getting widget info", e)
            // Report this to firebase
            FirebaseCrashlytics.getInstance().recordException(Exception("Exception getting all widget info", e))
            null
        }
    }

    /**
     * Saves the WidgetInfo
     */
    fun saveWidgetInfo(info: WidgetInfo) {
        Log.v("Saving widget info for " + info.id)
        try {
            mDao.insert(info)
        } catch (e: Exception) {
            Log.e("Exception saving widget info", e)
            // Report this to firebase
            FirebaseCrashlytics.getInstance().recordException(Exception("Exception saving widget info", e))
        }
    }

    /**
     * Deletes the widget information from the database
     */
    fun deleteWidgetInfo(id: Int) {
        Log.v("Deleting widget info $id")
        try {
            mDao.deleteById(id)
        } catch (e: Exception) {
            Log.e("Error deleting widget info", e)
            // Report this to firebase
            FirebaseCrashlytics.getInstance().recordException(Exception("Exception deleting eidget info", e))
        }
    }

    companion object {
        const val DATA_ADDED = "com.tortel.deploytrack.DATA_ADDED"
        const val DATA_DELETED = "com.tortel.deploytrack.DATA_DELETED"
        const val DATA_CHANGED = "com.tortel.deploytrack.DATA_CHANGED"
        private const val DATABASE_NAME = "data.2023"
        private var instance: DatabaseManager? = null
        @JvmStatic
        fun getInstance(context: Context): DatabaseManager {
            if (instance == null) {
                instance = DatabaseManager(context)
            }
            return instance!!
        }
    }
}
