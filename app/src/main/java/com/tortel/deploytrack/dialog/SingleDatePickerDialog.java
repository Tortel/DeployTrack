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
import android.support.v4.content.LocalBroadcastManager;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.tortel.deploytrack.Log;

/**
 * DatePickerDialog that keeps track if a dialog is open or not
 */
public class SingleDatePickerDialog extends DatePickerDialog implements DatePickerDialog.OnDateSetListener {
    public static final String ACTION_DATE_SELECTED = "com.tortel.deploytrack.DATE_SELECTED";
    public static final String EXTRA_YEAR = "year";
    public static final String EXTRA_MONTH = "month";
    public static final String EXTRA_DAY = "day";
    public static final String EXTRA_TYPE = "mType";

    public static final int TYPE_START = 0;
    public static final int TYPE_END = 1;

    private static boolean isActive = false;
    private int mType;

    public SingleDatePickerDialog() {
        super();
        setOnDateSetListener(this);
        isActive = true;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if(bundle != null){
            mType = bundle.getInt(EXTRA_TYPE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putInt(EXTRA_TYPE, mType);
    }

    /**
     * Initialize the picker
     */
    public void initialize(int type, int year, int month, int day){
        mType = type;
        initialize(this, year, month, day, true);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        isActive = false;
    }

    /**
     * Returns if a SingleDatePickerDialog is visible/active or not
     */
    public static boolean isActive(){
        return isActive;
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
        Log.v("Date selcted: "+day+"/"+month+"/"+year);

        // Build/sent intent
        Intent intent = new Intent(ACTION_DATE_SELECTED);
        intent.putExtra(EXTRA_DAY, day);
        intent.putExtra(EXTRA_MONTH, month);
        intent.putExtra(EXTRA_YEAR, year);
        intent.putExtra(EXTRA_TYPE, mType);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
    }
}
