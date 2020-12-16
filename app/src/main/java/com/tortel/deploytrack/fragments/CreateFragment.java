/*
 * Copyright (C) 2020 Scott Warner
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
package com.tortel.deploytrack.fragments;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SVBar;
import com.tortel.deploytrack.Analytics;
import com.tortel.deploytrack.Log;
import com.tortel.deploytrack.R;
import com.tortel.deploytrack.data.DatabaseManager;
import com.tortel.deploytrack.data.Deployment;
import com.tortel.deploytrack.dialog.SingleDatePickerDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CreateFragment extends Fragment implements View.OnClickListener, View.OnFocusChangeListener {
    private static final String TAG_DATE_DIALOG = "datePicker";

    private static final String KEY_TIME_START = "start";
    private static final String KEY_TIME_END = "end";
    private static final String KEY_NAME = "name";
    private static final String KEY_COLOR_COMPLETED = "completed";
    private static final String KEY_COLOR_REMAINING = "remaining";
    private static final String KEY_DISPLAY_TYPE = "display";

    private MaterialToolbar mToolbar;
    private EditText mNameEdit;
    private TextInputLayout mNameWrapper;
    private EditText mStartInput;
    private TextInputLayout mStartWrapper;
    private EditText mEndInput;
    private TextInputLayout mEndWrapper;

    private RadioButton mBarButton;

    private SimpleDateFormat mDateFormat;

    // Colors
    private int mCompletedColor;
    private int mRemainingColor;

    // Date range
    private Calendar mStartDate;
    private Calendar mEndDate;

    // The data to save;
    private Deployment mDeployment;
    private FirebaseAnalytics mFirebaseAnalytics;

    @SuppressLint("SimpleDateFormat")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());

        mDateFormat = new SimpleDateFormat("MMM dd, yyyy");

        // Register for date changes
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mDateChangeReceiver,
                new IntentFilter(SingleDatePickerDialog.ACTION_DATE_SELECTED));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_create, container, false);

        mToolbar = view.findViewById(R.id.topAppBar);
        mToolbar.setOnMenuItemClickListener((MenuItem item) -> {
            switch(item.getItemId()){
                case android.R.id.home:
                    NavHostFragment.findNavController(this).navigateUp();
                    return true;
                case R.id.menu_save:
                    saveDeployment();
                    return true;
            }
            return super.onOptionsItemSelected(item);
        });
        mToolbar.setNavigationOnClickListener((View v) -> {
            NavHostFragment.findNavController(this).navigateUp();
        });

        mNameEdit = view.findViewById(R.id.name);
        mNameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Clear errors on text change
                try{
                    mNameWrapper.setErrorEnabled(false);
                } catch(Exception e){
                    // Ignore
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Do nothing
            }
        });
        mNameWrapper = view.findViewById(R.id.name_wraper);

        mStartInput = view.findViewById(R.id.button_start);
        mStartInput.setOnClickListener(this);
        mStartInput.setOnFocusChangeListener(this);
        mStartWrapper = view.findViewById(R.id.start_wrapper);

        mEndInput = view.findViewById(R.id.button_end);
        mEndInput.setOnClickListener(this);
        mEndInput.setOnFocusChangeListener(this);
        mEndWrapper = view.findViewById(R.id.end_wrapper);

        mBarButton = view.findViewById(R.id.layout_bar);
        RadioButton circleButton = view.findViewById(R.id.layout_circle);

        //Color pickers
        ColorPicker completedPicker = view.findViewById(R.id.picker_completed);
        ColorPicker remainingPicker = view.findViewById(R.id.picker_remain);

        SVBar completedBar = view.findViewById(R.id.sv_completed);
        SVBar remainingBar = view.findViewById(R.id.sv_remain);

        String id = null; // TODO getIntent().getStringExtra("id");
        if (id != null) {
            //Starting in edit mode, load the old data
            mDeployment = DatabaseManager.getInstance(getContext()).getDeployment(id);

            //Set the colors
            mCompletedColor = mDeployment.getCompletedColor();
            mRemainingColor = mDeployment.getRemainingColor();

            //Set the dates
            mStartDate = Calendar.getInstance();
            mEndDate = Calendar.getInstance();

            mStartDate.setTimeInMillis(mDeployment.getStartDate().getTime());
            mEndDate.setTimeInMillis(mDeployment.getEndDate().getTime());

            //Set the buttons
            mStartInput.setText(mDateFormat.format(mStartDate.getTime()));
            mEndInput.setText(mDateFormat.format(mEndDate.getTime()));

            // Set circle/bar selected
            if(mDeployment.getDisplayType() == Deployment.DISPLAY_BAR){
                mBarButton.setChecked(true);
            } else {
                circleButton.setChecked(true);
            }

            //Set the name
            mNameEdit.setText(mDeployment.getName());

            mToolbar.setTitle(R.string.edit);
        } else {
            mDeployment = new Deployment();

            mStartDate = Calendar.getInstance();
            mEndDate = (Calendar) mStartDate.clone();

            mCompletedColor = Color.GREEN;
            mRemainingColor = Color.RED;

            mToolbar.setTitle(R.string.add_new);
        }

        //If restore from rotation
        if(savedInstanceState != null){
            mStartDate.setTimeInMillis(savedInstanceState.getLong(KEY_TIME_START));
            mEndDate.setTimeInMillis(savedInstanceState.getLong(KEY_TIME_END));

            mNameEdit.setText(savedInstanceState.getString(KEY_NAME));

            mCompletedColor = savedInstanceState.getInt(KEY_COLOR_COMPLETED);
            mRemainingColor = savedInstanceState.getInt(KEY_COLOR_REMAINING);

            //Set the date buttons, if set
            if(mStartDate != null){
                mStartInput.setText(mDateFormat.format(mStartDate.getTime()));
            }

            if(mStartDate != null && mEndDate != null && mEndDate.after(mStartDate)){
                mEndInput.setText(mDateFormat.format(mEndDate.getTime()));
            }

            int viewType = savedInstanceState.getInt(KEY_DISPLAY_TYPE, Deployment.DISPLAY_CIRCLE);
            if(viewType == Deployment.DISPLAY_BAR){
                mBarButton.setChecked(true);
            } else {
                circleButton.setChecked(true);
            }
        }

        remainingPicker.setOldCenterColor(mRemainingColor);
        remainingPicker.setNewCenterColor(mRemainingColor);
        remainingPicker.addSVBar(remainingBar);
        remainingPicker.setColor(mRemainingColor);
        remainingPicker.setShowOldCenterColor(false);
        remainingPicker.setOnColorChangedListener(new RemainingColorChangeListener());

        completedPicker.setOldCenterColor(mCompletedColor);
        completedPicker.setNewCenterColor(mCompletedColor);
        completedPicker.addSVBar(completedBar);
        completedPicker.setColor(mCompletedColor);
        completedPicker.setShowOldCenterColor(false);
        completedPicker.setOnColorChangedListener(new CompletedColorChangeListener());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister our date change receiver
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mDateChangeReceiver);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save everything
        outState.putLong(KEY_TIME_START, mStartDate.getTimeInMillis());
        outState.putLong(KEY_TIME_END, mEndDate.getTimeInMillis());

        outState.putString(KEY_NAME, mNameEdit.getText().toString());

        outState.putInt(KEY_COLOR_COMPLETED, mCompletedColor);
        outState.putInt(KEY_COLOR_REMAINING, mRemainingColor);

        if(mBarButton.isChecked()){
            outState.putInt(KEY_DISPLAY_TYPE, Deployment.DISPLAY_BAR);
        } else {
            outState.putInt(KEY_DISPLAY_TYPE, Deployment.DISPLAY_CIRCLE);
        }
    }

    /**
     * Check that everything is set, and save the deployment
     */
    private void saveDeployment(){
        boolean hasError = false;

        if(mStartDate == null || "".equals(mStartInput.getText().toString())){
            mStartWrapper.setErrorEnabled(true);
            mStartWrapper.setError(getString(R.string.invalid_start));
            hasError = true;
        } else {
            mStartWrapper.setErrorEnabled(false);
        }

        if(mEndDate == null || !mEndDate.after(mStartDate) || "".equals(mEndInput.getText().toString())){
            mEndWrapper.setErrorEnabled(true);
            mEndWrapper.setError(getString(R.string.invalid_end));
            hasError = true;
        } else {
            mEndWrapper.setErrorEnabled(false);
        }

        String name = mNameEdit.getText().toString().trim();
        if("".equals(name)){
            mNameWrapper.setErrorEnabled(true);
            mNameWrapper.setError(getString(R.string.invalid_name));
            hasError = true;
        } else {
            mNameWrapper.setErrorEnabled(false);
        }

        // Stop now if there was an error
        if(hasError){
            return;
        }

        // Set the values
        mDeployment.setStartDate(mStartDate.getTime());
        mDeployment.setEndDate(mEndDate.getTime());
        mDeployment.setName(name);
        mDeployment.setCompletedColor(mCompletedColor);
        mDeployment.setRemainingColor(mRemainingColor);
        // Set the display type
        if(mBarButton.isChecked()){
            mDeployment.setDisplayType(Deployment.DISPLAY_BAR);
        } else {
            mDeployment.setDisplayType(Deployment.DISPLAY_CIRCLE);
        }
        // Save it
        DatabaseManager.getInstance(getContext()).saveDeployment(mDeployment);
        // Log the event
        if (false) {// TODO (getIntent().hasExtra("id")) {
            mFirebaseAnalytics.logEvent(Analytics.EVENT_EDITED_DEPLOYMENT, null);
        } else {
            mFirebaseAnalytics.logEvent(Analytics.EVENT_CREATED_DEPLOYMENT, null);
        }

        // Navigate back
        NavHostFragment.findNavController(this).navigateUp();
    }

    /**
     * Method called when the buttons are clicked
     */
    @Override
    public void onClick(View view){
        Log.d("OnClick called");
        switch(view.getId()){
            case R.id.button_start:
                showStartDatePicker();
                break;
            case R.id.button_end:
                showEndDatePicker();
                break;
        }
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        Log.d("onFocusChange called with hasFocus: "+hasFocus);
        if(hasFocus){
            switch (view.getId()){
                case R.id.button_start:
                    showStartDatePicker();
                    break;
                case R.id.button_end:
                    showEndDatePicker();
                    break;
            }
            // Shift the focus off the date 'buttons'
            mNameEdit.requestFocus();
        }
    }

    /**
     * Show the start date picker, if it is not already visible
     */
    private void showStartDatePicker(){
        FragmentManager fm = getParentFragmentManager();
        Fragment startDialog = fm.findFragmentByTag(TAG_DATE_DIALOG);
        if(startDialog != null && startDialog.isVisible()){
            Log.d("Date dialog is visible, not showing");
            return;
        }

        SingleDatePickerDialog startPicker = new SingleDatePickerDialog();
        startPicker.setType(SingleDatePickerDialog.PickerType.START);
        startPicker.initialize(mStartDate == null ? Calendar.getInstance() : mStartDate);
        startPicker.show(fm, TAG_DATE_DIALOG);
    }

    /**
     * Set the start date
     */
    public void setStartDate(int year, int month, int day){
        Log.v("Setting start date to "+day+"/"+month+"/"+year);
        mStartDate.set(year, month, day, 0, 0);
        mStartInput.setText(mDateFormat.format(mStartDate.getTime()));

        mStartWrapper.setErrorEnabled(false);
    }

    /**
     * Show the end date picker, if it is not already visible
     */
    private void showEndDatePicker(){
        FragmentManager fm = getParentFragmentManager();
        Fragment endDialog = fm.findFragmentByTag(TAG_DATE_DIALOG);
        if(endDialog != null && endDialog.isVisible()){
            Log.d("End dialog is visible, not showing");
            return;
        }

        SingleDatePickerDialog endPicker = new SingleDatePickerDialog();
        endPicker.setType(SingleDatePickerDialog.PickerType.END);
        endPicker.setMinDate(mStartDate);
        endPicker.initialize(mEndDate == null? mStartDate : mEndDate);

        endPicker.show(fm, TAG_DATE_DIALOG);
    }

    /**
     * Set the end date
     */
    public void setEndDate(int year, int month, int day){
        Log.v("Setting end date to "+day+"/"+month+"/"+year);
        mEndDate.set(year, month, day, 0, 0);
        mEndInput.setText(mDateFormat.format(mEndDate.getTime()));

        mEndWrapper.setErrorEnabled(false);
    }

    /*
     * Classes to listen for color changes
     */
    private class CompletedColorChangeListener implements ColorPicker.OnColorChangedListener {
        @Override
        public void onColorChanged(int color) {
            mCompletedColor = color;
        }
    }

    private class RemainingColorChangeListener implements ColorPicker.OnColorChangedListener {
        @Override
        public void onColorChanged(int color) {
            mRemainingColor = color;
        }
    }

    private final BroadcastReceiver mDateChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int year = intent.getIntExtra(SingleDatePickerDialog.EXTRA_YEAR, -1);
            int month = intent.getIntExtra(SingleDatePickerDialog.EXTRA_MONTH, -1);
            int day = intent.getIntExtra(SingleDatePickerDialog.EXTRA_DAY, -1);
            SingleDatePickerDialog.PickerType type = intent.getIntExtra(SingleDatePickerDialog.EXTRA_TYPE, 0) == 0 ?
                    SingleDatePickerDialog.PickerType.START : SingleDatePickerDialog.PickerType.END;

            if(type  == SingleDatePickerDialog.PickerType.START){
                setStartDate(year, month, day);
            } else {
                setEndDate(year, month, day);
            }
        }
    };
}
