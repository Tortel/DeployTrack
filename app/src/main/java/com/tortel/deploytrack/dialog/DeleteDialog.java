package com.tortel.deploytrack.dialog;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.tortel.deploytrack.Log;
import com.tortel.deploytrack.MainActivity;
import com.tortel.deploytrack.R;
import com.tortel.deploytrack.data.DatabaseManager;

/**
 * Dialog confirming deletion
 */
public class DeleteDialog extends DialogFragment {
    public static final String KEY_ID = "id";
    private int mId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mId = getArguments().getInt(KEY_ID);
    }
    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        builder.content(R.string.confirm);
        builder.title(R.string.delete);
        builder.positiveText(R.string.delete);
        builder.negativeText(R.string.cancel);

        builder.callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
                Log.v("Deleting " + mId);
                //Delete it
                DatabaseManager.getInstance(getActivity()).deleteDeployment(mId);
                // Notify the app
                Intent deleteIntent = new Intent(MainActivity.DATA_DELETED);
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(deleteIntent);
            }
        });
        return builder.build();
    }
}
