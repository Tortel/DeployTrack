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
import android.content.Intent

import androidx.localbroadcastmanager.content.LocalBroadcastManager

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import com.tortel.deploytrack.Log

/**
 * Handles interaction with the firebase database
 */
internal class FirebaseDBManager private constructor(dbManager: DatabaseManager, context: Context) :
    ChildEventListener {
    private var mDbManager: DatabaseManager
    private val mDatabase: DatabaseReference
    private var mUser: FirebaseUser? = null
    private val mBroadcastManager: LocalBroadcastManager

    init {
        // Enable persistence
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        } catch (e: Exception) {
            Log.e("Exception enabling persistance", e)
            // If theres an error, log it
            FirebaseCrashlytics.getInstance()
                .recordException(Exception("Exception enabling persistance", e))
        }
        mDatabase = FirebaseDatabase.getInstance().reference
        mDbManager = dbManager
        mBroadcastManager = LocalBroadcastManager.getInstance(context.applicationContext)
    }
    /**
     * Get the Firebase user object
     */// No user, clear everything// Register for changes
    // Sync it when offline
    /**
     * Set the firebase user.
     * Until the user is set, no operations will work.
     */
    var user: FirebaseUser?
        get() = mUser
        set(user) {
            mUser = user
            if (mUser != null) {
                // Register for changes
                deploymentNode.addChildEventListener(this)
                // Sync it when offline
                deploymentNode.keepSynced(true)
            } else {
                // No user, clear everything
                mDatabase.removeEventListener(this)
                mDatabase.keepSynced(false)
            }
        }

    /**
     * Remove a deployment object from Firebase by the ID
     */
    fun deleteDeployment(uuid: String) {
        if (mUser == null) {
            return
        }

        // Remove the deployment by UUID
        Log.d("Deleting deployment with UUID $uuid from Firebase")
        deploymentNode.child(uuid).removeValue()
    }

    /**
     * Save a deployment to Firebase
     */
    fun saveDeployment(deployment: Deployment) {
        if (mUser == null) {
            return
        }
        Log.d("Saving deployment with UUID " + deployment.uuid + " to Firebase")
        deploymentNode.child(deployment.uuid).setValue(deployment)
    }

    /**
     * Sync the local and remote information
     */
    fun syncAll() {
        if (mUser == null) {
            return
        }
        val localDeployments = mDbManager.allDeploymentsSync
        val deploymentNode = deploymentNode
        for (deployment in localDeployments) {
            checkForOrAdd(deploymentNode, deployment)
        }
    }

    /**
     * Check if the deployment is saved in firebase. If not, add it
     */
    private fun checkForOrAdd(rootNode: DatabaseReference, deployment: Deployment) {
        rootNode.child(deployment.uuid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d("Deployment with UUID " + deployment.uuid + " already on firebase")
                    // Do nothing?
                } else {
                    // Add it
                    Log.d("Deployment with UUID " + deployment.uuid + " not on firebase, adding")
                    rootNode.child(deployment.uuid).setValue(deployment)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Not needed?
            }
        })
    }

    /**
     * Get a reference to the root deployment node
     */
    private val deploymentNode: DatabaseReference
        get() = mDatabase.child("users").child(mUser!!.uid).child("deployments")

    override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
        val newDeployment = dataSnapshot.getValue(
            Deployment::class.java
        )
        Log.d("FB onChildAdded: Deployment with UUID " + newDeployment!!.uuid)

        // Check if this new deployment is present locally
        val localDeployment = mDbManager.getDeployment(
            newDeployment.uuid
        )
        // Add it if it is not
        if (localDeployment == null) {
            Log.d("DB onChildAdded: Deployment with UUID " + newDeployment.uuid + " not present, saving locally")
            mDbManager.saveDeployment(newDeployment)
            // Update the UI
            mBroadcastManager.sendBroadcast(Intent(DatabaseManager.DATA_ADDED))
        }
    }

    override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
        val updatedDeployment = dataSnapshot.getValue(
            Deployment::class.java
        )
        var localDeployment = mDbManager.getDeploymentSync(
            updatedDeployment!!.uuid
        )
        Log.d("FB onChildChanged: Deployment with UUID " + updatedDeployment.uuid)
        if (localDeployment == null) {
            // Just save the updated deployment
            localDeployment = updatedDeployment
            Log.d("FB onChildChanged: Deployment with UUID " + updatedDeployment.uuid + " not present locally")
        } else {
            // Update all the fields
            localDeployment = Deployment(
                updatedDeployment.name, updatedDeployment.uuid,
                updatedDeployment.startDate, updatedDeployment.endDate,
                updatedDeployment.completedColor, updatedDeployment.remainingColor
            )
            Log.d("FB onChildChanged: Deployment with UUID " + updatedDeployment.uuid + " - updating local version")
        }

        // Save it
        mDbManager.saveDeployment(localDeployment)
        // Update the UI
        mBroadcastManager.sendBroadcast(Intent(DatabaseManager.DATA_CHANGED))
    }

    override fun onChildRemoved(dataSnapshot: DataSnapshot) {
        val removedDeployment = dataSnapshot.getValue(
            Deployment::class.java
        )
        val localDeployment = mDbManager.getDeploymentSync(
            removedDeployment!!.uuid
        )
        Log.d("FB onChildRemoved: Deployment with UUID " + removedDeployment.uuid + " removed from firebase")
        if (localDeployment != null) {
            Log.d("FB onChildRemoved: Deployment with UUID " + removedDeployment.uuid + " removing locally")
            mDbManager.deleteDeployment(localDeployment.uuid, false)

            // Update the UI
            mBroadcastManager.sendBroadcast(Intent(DatabaseManager.DATA_DELETED))
        }
    }

    override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {
        Log.d("FB onChildMoved")
        // Probably not needed
    }

    override fun onCancelled(databaseError: DatabaseError) {
        Log.d("FB onCancelled")
        // Probably not needed
    }

    companion object {
        private var instance: FirebaseDBManager? = null

        /**
         * Get an instance of the Firebase DB manager
         *
         * @param dbManager
         * @param context
         * @return
         */
        fun getInstance(dbManager: DatabaseManager, context: Context): FirebaseDBManager {
            if (instance == null) {
                instance = FirebaseDBManager(dbManager, context)
            }
            // Make sure that the database manager is properly set
            if (instance!!.mDbManager != dbManager) {
                instance!!.mDbManager = dbManager
            }
            return instance!!
        }
    }
}
