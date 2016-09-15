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
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.fourmob.datetimepicker.date.DatePickerDialog.OnDateSetListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.ColorPicker.OnColorChangedListener;
import com.larswerkman.holocolorpicker.SVBar;
import com.tortel.deploytrack.data.*;

/**
 * Activity for creating and editing a Deployment
 */
public class CreateActivity extends AppCompatActivity {
	private static final String KEY_TIME_START = "start";
	private static final String KEY_TIME_END = "end";
	private static final String KEY_SET_START = "startset";
	private static final String KEY_SET_END = "endset";
	private static final String KEY_NAME = "name";
	private static final String KEY_COLOR_COMPLETED = "completed";
	private static final String KEY_COLOR_REMAINING = "remaining";
	private static final String KEY_DISPALY_TYPE = "display";
	
	private EditText mNameEdit;
	private Button mStartButton;
	private Button mEndButton;
	private Button mSaveButton;

	private RadioButton mBarButton;
	
	private SimpleDateFormat mDateFormat;
	
	//Colors
	private int mCompletedColor;
	private int mRemainingColor;
	
	//Date range
	private Calendar mStartDate;
	private Calendar mEndDate;
	
	//Flags for dates
	private boolean mStartSet;
	private boolean mEndSet;
	
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
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
		
		mDateFormat = new SimpleDateFormat("MMM dd, yyyy");
		
		mNameEdit = (EditText) findViewById(R.id.name);
		mStartButton = (Button) findViewById(R.id.button_start);
		mEndButton = (Button) findViewById(R.id.button_end);
		mSaveButton = (Button) findViewById(R.id.button_save);

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
			mStartSet = true;
			mEndSet = true;
			
			//Set the buttons
			mStartButton.setText(getResources().getString(R.string.start_date) +
                    "\n" + mDateFormat.format(mStartDate.getTime()));
			mEndButton.setText(getResources().getString(R.string.end_date) +
                    "\n" + mDateFormat.format(mEndDate.getTime()));

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
			mEndButton.setEnabled(false);
			mSaveButton.setEnabled(false);
			
			mStartDate = Calendar.getInstance();
			mEndDate = (Calendar) mStartDate.clone();
			mStartSet = false;
			mEndSet = false;
			
			mCompletedColor = Color.GREEN;
			mRemainingColor = Color.RED;
			
			getSupportActionBar().setTitle(R.string.add_new);
		}
		
		//If restore from rotation
		if(savedInstanceState != null){
			mStartDate.setTimeInMillis(savedInstanceState.getLong(KEY_TIME_START));
			mEndDate.setTimeInMillis(savedInstanceState.getLong(KEY_TIME_END));
			
			mStartSet = savedInstanceState.getBoolean(KEY_SET_START);
			mEndSet = savedInstanceState.getBoolean(KEY_SET_END);
			
			mNameEdit.setText(savedInstanceState.getString(KEY_NAME));
			
			mCompletedColor = savedInstanceState.getInt(KEY_COLOR_COMPLETED);
			mRemainingColor = savedInstanceState.getInt(KEY_COLOR_REMAINING);
			
			//Set the date buttons, if set
			if(mStartSet){
				mStartButton.setText(getResources().getString(R.string.start_date) +
                        "\n" + mDateFormat.format(mStartDate.getTime()));
				mEndButton.setEnabled(true);
			}
			
			if(mEndSet && mEndDate.after(mStartDate)){
				mEndButton.setText(getResources().getString(R.string.end_date) +
                        "\n" + mDateFormat.format(mEndDate.getTime()));
				mSaveButton.setEnabled(true);
			}

			int viewType = savedInstanceState.getInt(KEY_DISPALY_TYPE, Deployment.DISPLAY_CIRCLE);
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
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		//Save everything
		outState.putLong(KEY_TIME_START, mStartDate.getTimeInMillis());
		outState.putLong(KEY_TIME_END, mEndDate.getTimeInMillis());
		
		outState.putBoolean(KEY_SET_START, mStartSet);
		outState.putBoolean(KEY_SET_END, mEndSet);
		
		outState.putString(KEY_NAME, mNameEdit.getText().toString());
		
		outState.putInt(KEY_COLOR_COMPLETED, mCompletedColor);
		outState.putInt(KEY_COLOR_REMAINING, mRemainingColor);

		if(mBarButton.isChecked()){
			outState.putInt(KEY_DISPALY_TYPE, Deployment.DISPLAY_BAR);
		} else {
			outState.putInt(KEY_DISPALY_TYPE, Deployment.DISPLAY_CIRCLE);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		//Finish on the icon 'up' pressed
		case android.R.id.home:
			this.finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Method called when the buttons are clicked
	 * @param v
	 */
	public void onClick(View v){
		FragmentManager fm = getSupportFragmentManager();
		
		switch(v.getId()){
		case R.id.button_cancel:
			this.finish();
			return;
		case R.id.button_save:
			String name = mNameEdit.getText().toString().trim();
			if("".equals(name)){
				Toast.makeText(this, R.string.invalid_name, Toast.LENGTH_SHORT).show();
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
			return;
		case R.id.button_start:
			DatePickerDialog startPicker = new DatePickerDialog();
			startPicker.initialize(new OnDateSetListener(){
				public void onDateSet(DatePickerDialog dialog, int year, int month, int day){
					mStartDate.set(year, month, day, 0, 0);
					if(!mEndSet || mStartDate.before(mEndDate)){
						mStartSet = true;
						mEndButton.setEnabled(true);
						mStartButton.setText(getResources().getString(R.string.start_date) +
                                "\n" + mDateFormat.format(mStartDate.getTime()));
					} else {
						Toast.makeText(CreateActivity.this, R.string.invalid_start, Toast.LENGTH_SHORT).show();
						mSaveButton.setEnabled(false);
					}
				}
			}, mStartDate.get(Calendar.YEAR), mStartDate.get(Calendar.MONTH), mStartDate.get(Calendar.DAY_OF_MONTH), true);
			startPicker.show(fm, "startPicker");
			return;
		case R.id.button_end:
			DatePickerDialog endPicker = new DatePickerDialog();
			endPicker.initialize(new OnDateSetListener(){
				public void onDateSet(DatePickerDialog dialog, int year, int month, int day){
					mEndDate.set(year, month, day, 0, 0);
					if(mEndDate.after(mStartDate)){
						mEndSet = true;
						mSaveButton.setEnabled(true);
						mEndButton.setText(getResources().getString(R.string.end_date) +
                                "\n" + mDateFormat.format(mEndDate.getTime()));
					} else {
						Toast.makeText(CreateActivity.this, R.string.invalid_end, Toast.LENGTH_SHORT).show();
						mSaveButton.setEnabled(false);
					}
				}
			}, mStartDate.get(Calendar.YEAR), mStartDate.get(Calendar.MONTH), mStartDate.get(Calendar.DAY_OF_MONTH), true);
			endPicker.show(fm, "endPicker");
		}
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
}
