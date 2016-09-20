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
package com.tortel.deploytrack.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.tortel.deploytrack.Log;
import com.tortel.deploytrack.MainActivity;
import com.tortel.deploytrack.R;
import com.tortel.deploytrack.data.*;
import com.tortel.deploytrack.provider.WidgetProvider;

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
    private class DatabaseUpgradeTask extends AsyncTask<Void, Void, Boolean> {
        private Context context;

        public DatabaseUpgradeTask(Context context){
            this.context = context.getApplicationContext();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return DatabaseUpgrader.doDatabaseUpgrade(context);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // TODO - Wtf to do if it failed?

            // Force the widgets to update
            Log.v("Sending widget update broadcast");
            Intent updateWidgetIntent = new Intent(WidgetProvider.UPDATE_INTENT);
            getActivity().sendBroadcast(updateWidgetIntent);

            // Re-start the main activity
            restartMainActivity();
        }
    }
}
