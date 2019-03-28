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
package com.tortel.deploytrack;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import com.google.android.material.textfield.TextInputLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.ColorPicker.OnColorChangedListener;
import com.larswerkman.holocolorpicker.SVBar;
import com.tortel.deploytrack.data.*;
import com.tortel.deploytrack.dialog.SingleDatePickerDialog;

/**
 * Activity for creating and editing a Deployment
 */
public class CreateActivity extends AppCompatActivity implements View.OnClickListener, View.OnFocusChangeListener {
	private static final String TAG_DATE_DIALOG = "datePicker";

	private static final String KEY_TIME_START = "start";
	private static final String KEY_TIME_END = "end";
	private static final String KEY_NAME = "name";
	private static final String KEY_COLOR_COMPLETED = "completed";
	private static final String KEY_COLOR_REMAINING = "remaining";
	private static final String KEY_DISPLAY_TYPE = "display";
	
	private EditText mNameEdit;
	private TextInputLayout mNameWrapper;
	private EditText mStartInput;
	private TextInputLayout mStartWrapper;
	private EditText mEndInput;
	private TextInputLayout mEndWrapper;

	private RadioButton mBarButton;
	
	private SimpleDateFormat mDateFormat;
	
	//Colors
	private int mCompletedColor;
	private int mRemainingColor;
	
	//Date range
	private Calendar mStartDate;
	private Calendar mEndDate;
	
	//The data to save;
	private Deployment mDeployment;

	private FirebaseAnalytics mFirebaseAnalytics;
	
	@SuppressLint("SimpleDateFormat")
	@Override
	public void onCreate(Bundle savedInstanceState){
        // Check for light theme
        Prefs.load(this);
        if(Prefs.useLightTheme()){
            setTheme(R.style.Theme_DeployThemeLight);
        }

		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_create);
		if(getSupportActionBar() != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}

		mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
		
		mDateFormat = new SimpleDateFormat("MMM dd, yyyy");
		
		mNameEdit = (EditText) findViewById(R.id.name);
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
		mNameWrapper = (TextInputLayout) findViewById(R.id.name_wraper);

		mStartInput = (EditText) findViewById(R.id.button_start);
		mStartInput.setOnClickListener(this);
		mStartInput.setOnFocusChangeListener(this);
		mStartWrapper = (TextInputLayout) findViewById(R.id.start_wrapper);

		mEndInput = (EditText) findViewById(R.id.button_end);
		mEndInput.setOnClickListener(this);
		mEndInput.setOnFocusChangeListener(this);
		mEndWrapper = (TextInputLayout) findViewById(R.id.end_wrapper);

		mBarButton = (RadioButton) findViewById(R.id.layout_bar);
		RadioButton circleButton = (RadioButton) findViewById(R.id.layout_circle);
		
		//Color pickers
		ColorPicker completedPicker = (ColorPicker) findViewById(R.id.picker_completed);
		ColorPicker remainingPicker = (ColorPicker) findViewById(R.id.picker_remain);
		
		SVBar completedBar = (SVBar) findViewById(R.id.sv_completed);
		SVBar remainingBar = (SVBar) findViewById(R.id.sv_remain);
		
		String id = getIntent().getStringExtra("id");
		if(id != null){
			//Starting in edit mode, load the old data
			mDeployment = DatabaseManager.getInstance(this).getDeployment(id);
			
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
			
			getSupportActionBar().setTitle(R.string.edit);
		} else {
			mDeployment = new Deployment();
			
			mStartDate = Calendar.getInstance();
			mEndDate = (Calendar) mStartDate.clone();
			
			mCompletedColor = Color.GREEN;
			mRemainingColor = Color.RED;
			
			getSupportActionBar().setTitle(R.string.add_new);
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

		// Register for date changes
		LocalBroadcastManager.getInstance(this).registerReceiver(mDateChangeReceiver,
				new IntentFilter(SingleDatePickerDialog.ACTION_DATE_SELECTED));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Unregister our date change receiver
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mDateChangeReceiver);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_create, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		//Finish on the icon 'up' pressed
		case android.R.id.home:
			this.finish();
			return true;
			case R.id.menu_save:
				saveDeployment();
				return true;
		}
		return super.onOptionsItemSelected(item);
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

		//Set the values
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
		//Save it
		DatabaseManager.getInstance(this).saveDeployment(mDeployment);
		// Log the event
		if(getIntent().hasExtra("id")){
			mFirebaseAnalytics.logEvent(Analytics.EVENT_EDITED_DEPLOYMENT, null);
		} else {
			mFirebaseAnalytics.logEvent(Analytics.EVENT_CREATED_DEPLOYMENT, null);
		}
		//End
		finish();
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
		FragmentManager fm = getSupportFragmentManager();
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
		FragmentManager fm = getSupportFragmentManager();
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
	private class CompletedColorChangeListener implements OnColorChangedListener{
		@Override
		public void onColorChanged(int color) {
			mCompletedColor = color;
		}
	}
	
	private class RemainingColorChangeListener implements OnColorChangedListener{
		@Override
		public void onColorChanged(int color) {
			mRemainingColor = color;
		}
	}

	private BroadcastReceiver mDateChangeReceiver = new BroadcastReceiver() {
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
