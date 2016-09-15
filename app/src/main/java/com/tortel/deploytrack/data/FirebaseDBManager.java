package com.tortel.deploytrack.data;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tortel.deploytrack.Log;

import java.util.List;

/**
 * Handles interaction with the firebase database
 */
class FirebaseDBManager implements ChildEventListener {
    private DatabaseManager mDbManager;
    private DatabaseReference mDatabase;
    private FirebaseUser mUser;
    private LocalBroadcastManager mBradcastManager;

    public FirebaseDBManager(DatabaseManager dbManager, Context context){
        // Enable persistence
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDbManager = dbManager;
        mBradcastManager = LocalBroadcastManager.getInstance(context.getApplicationContext());
    }

    /**
     * Set the firebase user.
     * Until the user is set, no operations will work.
     * @param user
     */
    public void setUser(FirebaseUser user) {
        this.mUser = user;
        // Register for changes
        getDeploymentNode().addChildEventListener(this);
        // Sync it when offline
        getDeploymentNode().keepSynced(true);
    }

    /**
     * Remove a deployment object from Firebase by the ID
     * @param uuid
     */
    public void deleteDeployment(String uuid){
        if(mUser == null){
            return;
        }

        // Remove the deployment by UUID
        Log.d("Deleting deployment with UUID "+uuid+" from Firebase");
        getDeploymentNode().child(uuid).removeValue();
    }

    /**
     * Save a deployment to Firebase
     * @param deployment
     */
    public void saveDeployment(Deployment deployment){
        if(mUser == null){
            return;
        }

        Log.d("Saving deployment with UUID "+deployment.getUuid()+" to Firebase");
        getDeploymentNode().child(deployment.getUuid()).setValue(deployment);
    }

    /**
     * Sync the local and remote information
     */
    public void syncAll(){
        if(mUser == null){
            return;
        }

        List<Deployment> localDeployments = mDbManager.getAllDeployments();
        DatabaseReference deploymentNode = getDeploymentNode();
        for(Deployment deployment : localDeployments){
            checkForOrAdd(deploymentNode, deployment);
        }

    }

    /**
     * Check if the deployment is saved in firebase. If not, add it
     * @param rootNode
     * @param deployment
     */
    private void checkForOrAdd(final DatabaseReference rootNode, final Deployment deployment){
        rootNode.child(deployment.getUuid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Log.d("Deployment with UUID "+deployment.getUuid()+" already on firebase");
                    // Do nothing?
                } else {
                    // Add it
                    Log.d("Deployment with UUID "+deployment.getUuid()+" not on firebase, adding");
                    rootNode.child(deployment.getUuid()).setValue(deployment);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Not needed?
            }
        });
    }

    /**
     * Get a reference to the root deployment node
     * @return
     */
    private DatabaseReference getDeploymentNode(){
        return mDatabase.child("users").child(mUser.getUid()).child("deployments");
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
        Deployment newDeployment = dataSnapshot.getValue(Deployment.class);
        Log.d("FB onChildAdded: Deployment with UUID "+newDeployment.getUuid());

        // Check if this new deployment is present locally
        Deployment localDeployment = mDbManager.getDeployment(newDeployment.getUuid());
        // Add it if it is not
        if(localDeployment == null){
            Log.d("DB onChildAdded: Deployment with UUID "+newDeployment.getUuid()+" not present, saving locally");
            mDbManager.saveDeployment(newDeployment);
            // Update the UI
            mBradcastManager.sendBroadcast(new Intent(DatabaseManager.DATA_ADDED));
        }
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
        Deployment updatedDeployment = dataSnapshot.getValue(Deployment.class);
        Deployment localDeployment = mDbManager.getDeployment(updatedDeployment.getUuid());
        Log.d("FB onChildChanged: Deployment with UUID "+updatedDeployment.getUuid());

        if(localDeployment == null){
            // Just save the updated deployment
            localDeployment = updatedDeployment;
            Log.d("FB onChildChanged: Deployment with UUID "+updatedDeployment.getUuid()+" not present locally");
        } else {
            // Update all the fields
            localDeployment.updateData(updatedDeployment);
            Log.d("FB onChildChanged: Deployment with UUID "+updatedDeployment.getUuid()+" - updating local version");
        }

        // Save it
        mDbManager.saveDeployment(updatedDeployment);
        // Update the UI
        mBradcastManager.sendBroadcast(new Intent(DatabaseManager.DATA_CHANGED));
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        Deployment removedDeployment = dataSnapshot.getValue(Deployment.class);
        Deployment localDeployment = mDbManager.getDeployment(removedDeployment.getUuid());
        Log.d("FB onChildRemoved: Deployment with UUID "+removedDeployment.getUuid()+" removed from firebase");

        if(localDeployment != null){
            Log.d("FB onChildRemoved: Deployment with UUID "+removedDeployment.getUuid()+" removing locally");
            mDbManager.deleteDeployment(localDeployment.getUuid(), false);

            // Update the UI
            mBradcastManager.sendBroadcast(new Intent(DatabaseManager.DATA_DELETED));
        }
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        Log.d("FB onChildMoved");
        // Probably not needed
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        Log.d("FB onCancelled");
        // Probably not needed
    }
}
