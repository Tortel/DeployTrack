package com.tortel.deploytrack.task;

import android.content.Context;
import android.os.AsyncTask;

import com.tortel.deploytrack.Prefs;
import com.tortel.deploytrack.data.DatabaseManager;
import com.tortel.deploytrack.data.Deployment;

import java.util.List;
import java.util.UUID;

/**
 * Task that makes sure that all deployments have UUIDs saved
 */
public class GenerateUuidTask extends AsyncTask<Void, Void, Void> {
    private DatabaseManager dbManager;
    private Context context;

    public GenerateUuidTask(Context context){
        context = context.getApplicationContext();
        this.context = context;
        dbManager = DatabaseManager.getInstance(context);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        // Make sure each deployment has a UUID set
        List<Deployment> deployments = dbManager.getAllDeployments();
        for(Deployment deployment : deployments){
            // If there isnt one, generate it
            if(deployment.getUuid() == null){
                deployment.setUuid(UUID.randomUUID());
                dbManager.saveDeployment(deployment);
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        Prefs.setGeneratedUuids(context);
    }
}
