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
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.tortel.deploytrack.Analytics;
import com.tortel.deploytrack.Log;
import com.tortel.deploytrack.R;
import com.tortel.deploytrack.data.DatabaseManager;
import com.tortel.deploytrack.data.Deployment;

/**
 * Dialog confirming deletion
 */
public class DeleteDialog extends DialogFragment {
    public static final String KEY_ID = "id";

    private String mId;
    private String mName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mId = getArguments().getString(KEY_ID);
        Deployment deployment = DatabaseManager.getInstance(getActivity()).getDeployment(mId);
        mName = deployment.getName();
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
        builder.content(R.string.confirm, mName);
        builder.title(R.string.delete);
        builder.positiveText(R.string.delete);
        builder.negativeText(R.string.cancel);
        builder.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                Log.v("Deleting " + mId);
                //Delete it
                DatabaseManager.getInstance(getActivity()).deleteDeployment(mId);
                // Notify the app
                Intent deleteIntent = new Intent(DatabaseManager.DATA_DELETED);
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(deleteIntent);
                // Log the event
                FirebaseAnalytics.getInstance(getActivity())
                        .logEvent(Analytics.EVENT_DELETED_DEPLOYMENT, null);
            }
        });

        builder.onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                dismiss();
            }
        });

        return builder.build();
    }
}
