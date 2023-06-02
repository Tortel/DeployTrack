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
package com.tortel.deploytrack.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.tortel.deploytrack.Log;
import com.tortel.deploytrack.MainActivity;
import com.tortel.deploytrack.R;
import com.tortel.deploytrack.data.ormlite.DatabaseUpgrader;
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
        doDatabaseUpgrade(getActivity());
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_progress, null);
        TextView messageView = view.findViewById(R.id.dialog_content);
        messageView.setText(R.string.db_upgrade);
        builder.setView(view);

        return builder.create();
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

    private void doDatabaseUpgrade(@NonNull final Context context) {
        (new Thread() {
            @Override
            public void run() {
                if (DatabaseUpgrader.doDatabaseUpgrade(context)) {
                    Handler mainHandler = new Handler(context.getMainLooper());
                    mainHandler.post(() -> {
                        Log.v("Sending widget update broadcast");
                        Intent updateWidgetIntent = new Intent(WidgetProvider.UPDATE_INTENT);
                        context.sendBroadcast(updateWidgetIntent);

                        // Re-start the main activity
                        restartMainActivity();
                    });
                } else {
                    // Uh? What now?

                }
            }
        }).start();
    }
}
