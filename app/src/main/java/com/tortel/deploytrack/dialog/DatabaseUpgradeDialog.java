package com.tortel.deploytrack.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.SparseArray;

import com.afollestad.materialdialogs.MaterialDialog;
import com.j256.ormlite.dao.Dao;
import com.tortel.deploytrack.Log;
import com.tortel.deploytrack.MainActivity;
import com.tortel.deploytrack.R;
import com.tortel.deploytrack.data.*;
import com.tortel.deploytrack.data.depricated.*;

import java.util.List;
import java.util.UUID;

/**
 * Dialog that runs the database upgrade
 */
public class DatabaseUpgradeDialog extends DialogFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setCancelable(false);

        // Start the upgrade
        new DatabaseUpgradeTask(getActivity()).execute();
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        builder.autoDismiss(false);
        builder.progress(true, 0);
        builder.content(R.string.db_upgrade);

        return builder.build();
    }

    /**
     * Restart the main activity
     */
    private void restartMainActivity(){
        try {
            dismissAllowingStateLoss();
        } catch(Exception e){
            // Ignore, being careful here.
        }

        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        getActivity().startActivity(intent);
    }

    /**
     * Class which handles the upgrade in the background
     */
    private class DatabaseUpgradeTask extends AsyncTask<Void, Void, Void> {
        private DatabaseManager dbManager;
        private OldDatabaseHelper oldDbHelper;
        private Context context;

        public DatabaseUpgradeTask(Context context){
            context = context.getApplicationContext();
            dbManager = DatabaseManager.getInstance(context);
            oldDbHelper = new OldDatabaseHelper(context);
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Log.d("Starting DB upgrade in background thread");
            try{
                Dao<OldDeployment, Integer> oldDeploymentDao = oldDbHelper.getDao(OldDeployment.class);
                Dao<OldWidgetInfo, Integer> oldWidgetDao = oldDbHelper.getDao(OldWidgetInfo.class);
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

            } catch (Exception e){
                Log.e("Exception during database upgrade", e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.d("Upgrade complete, deleting old database file");
            OldDatabaseHelper.deleteDbFile(context);

            // Re-start the main activity
            restartMainActivity();
        }
    }
}
