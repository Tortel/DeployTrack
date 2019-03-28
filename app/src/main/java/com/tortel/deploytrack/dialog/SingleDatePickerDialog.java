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

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.tortel.deploytrack.Log;
import com.tortel.deploytrack.R;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.Calendar;

/**
 * DatePickerDialog that keeps track if a dialog is open or not
 */
public class SingleDatePickerDialog extends DatePickerDialog implements DatePickerDialog.OnDateSetListener {
    public static final String ACTION_DATE_SELECTED = "com.tortel.deploytrack.DATE_SELECTED";
    public static final String EXTRA_YEAR = "year";
    public static final String EXTRA_MONTH = "month";
    public static final String EXTRA_DAY = "day";
    public static final String EXTRA_TYPE = "mType";

    public enum PickerType {
        START,
        END
    };

    private PickerType mType;

    public SingleDatePickerDialog() {
        super();
        setOnDateSetListener(this);
        dismissOnPause(true);
        mType = PickerType.START;
    }

    public void setType(PickerType type) {
        mType = type;
    }

    public void initialize(Calendar calendar) {
        this.initialize(this, calendar);
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setAccentColor(getResources().getColor(R.color.primary));
        if(bundle != null){
            mType = bundle.getInt(EXTRA_TYPE) == 0 ? PickerType.START : PickerType.END;
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putInt(EXTRA_TYPE, mType == PickerType.START ? 0 : 1);
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
        Log.v("Date selcted: "+day+"/"+month+"/"+year);

        // Build/sent intent
        Intent intent = new Intent(ACTION_DATE_SELECTED);
        intent.putExtra(EXTRA_DAY, day);
        intent.putExtra(EXTRA_MONTH, month);
        intent.putExtra(EXTRA_YEAR, year);
        intent.putExtra(EXTRA_TYPE, mType == PickerType.START ? 0 : 1);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
    }
}
