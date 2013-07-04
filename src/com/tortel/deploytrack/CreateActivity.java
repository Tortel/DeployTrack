package com.tortel.deploytrack;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.fourmob.datetimepicker.date.DatePickerDialog.OnDateSetListener;
import com.larswerkman.colorpicker.ColorPicker;
import com.larswerkman.colorpicker.ColorPicker.OnColorChangedListener;
import com.tortel.deploytrack.data.*;

/**
 * Activity for creating a new Deployment
 */
public class CreateActivity extends SherlockFragmentActivity {
	private Calendar start;
	private Calendar end;
	private EditText nameEdit;
	private Button startButton;
	private Button endButton;
	private Button saveButton;
	
	//Colors
	private int completedColor;
	private int remainingColor;
	
	private SimpleDateFormat format;
	
	//The data to save;
	private Deployment deployment;
	
	@SuppressLint("SimpleDateFormat")
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create);
		
		format = new SimpleDateFormat("MMM dd, yyyy");
		
		nameEdit = (EditText) findViewById(R.id.name);
		startButton = (Button) findViewById(R.id.button_start);
		endButton = (Button) findViewById(R.id.button_end);
		saveButton = (Button) findViewById(R.id.button_save);
		
		//Color pickers
		ColorPicker completedPicker = (ColorPicker) findViewById(R.id.picker_completed);
		ColorPicker remainingPicker = (ColorPicker) findViewById(R.id.picker_remain);
		
		int id = getIntent().getIntExtra("id", -1);
		if(id >= 0){
			//Starting in edit mode, load the old data
			deployment = DatabaseManager.getInstance(this).getDeployment(id);
			
			//Set the colors
			completedColor = deployment.getCompletedColor();
			remainingColor = deployment.getRemainingColor();
			
			//Set the dates
			start = Calendar.getInstance();
			end = Calendar.getInstance();
			
			start.setTimeInMillis(deployment.getStartDate().getTime());
			end.setTimeInMillis(deployment.getEndDate().getTime());
			
			//Set the buttons
			startButton.setText(getResources().getString(R.string.start_date)+
					"\n"+format.format(start.getTime()));
			endButton.setText(getResources().getString(R.string.end_date)+
					"\n"+format.format(end.getTime()));
			
			//Set the name
			nameEdit.setText(deployment.getName());
			
			getSupportActionBar().setTitle(R.string.edit);
		} else {
			deployment = new Deployment();
			disableButton(endButton);
			disableButton(saveButton);
			
			start = Calendar.getInstance();
			end = (Calendar) start.clone();
			end.add(Calendar.YEAR, 20);
			
			completedColor = Color.GREEN;
			remainingColor = Color.RED;
			
			getSupportActionBar().setTitle(R.string.add_new);
		}
		
		remainingPicker.setColor(remainingColor);
		remainingPicker.setOnColorChangedListener(new RemainingColorChangeListener());
		completedPicker.setColor(completedColor);
		completedPicker.setOnColorChangedListener(new CompletedColorChangeListener());
	}
	
	private void disableButton(Button button){
		button.setEnabled(false);
		button.setTextColor(Color.DKGRAY);
	}
	
	private void enableButton(Button button){
		button.setEnabled(true);
		button.setTextColor(Color.WHITE);
	}
	
	public void onClick(View v){
		FragmentManager fm = getSupportFragmentManager();
		
		switch(v.getId()){
		case R.id.button_cancel:
			this.finish();
			return;
		case R.id.button_save:
			String name = nameEdit.getText().toString().trim();
			if("".equals(name)){
				Toast.makeText(this, R.string.invalid_name, Toast.LENGTH_SHORT).show();
				return;
			}
			//Set the values
			deployment.setStartDate(start.getTime());
			deployment.setEndDate(end.getTime());
			deployment.setName(name);
			deployment.setCompletedColor(completedColor);
			deployment.setRemainingColor(remainingColor);
			//Save it
			DatabaseManager.getInstance(this).saveDeployment(deployment);
			//End
			finish();
			return;
		case R.id.button_start:
			DatePickerDialog startPicker = new DatePickerDialog();
			startPicker.initialize(new OnDateSetListener(){
				public void onDateSet(DatePickerDialog dialog, int year, int month, int day){
					start.set(year, month, day, 0, 0);
					if(start.compareTo(end) < 0){
						enableButton(endButton);
						startButton.setText(getResources().getString(R.string.start_date)+
								"\n"+format.format(start.getTime())); 
					} else {
						Toast.makeText(CreateActivity.this, R.string.invalid_start, Toast.LENGTH_SHORT).show();
						disableButton(saveButton);
					}
				}
			}, start.get(Calendar.YEAR), start.get(Calendar.MONTH), start.get(Calendar.DAY_OF_MONTH));
			startPicker.show(fm, "startPicker");
			return;
		case R.id.button_end:
			DatePickerDialog endPicker = new DatePickerDialog();
			endPicker.initialize(new OnDateSetListener(){
				public void onDateSet(DatePickerDialog dialog, int year, int month, int day){
					end.set(year, month, day, 0, 0);
					if(end.compareTo(start) > 0){
						enableButton(saveButton);
						endButton.setText(getResources().getString(R.string.end_date)+
								"\n"+format.format(end.getTime())); 
					} else {
						Toast.makeText(CreateActivity.this, R.string.invalid_end, Toast.LENGTH_SHORT).show();
						disableButton(saveButton);
					}
				}
			}, start.get(Calendar.YEAR), start.get(Calendar.MONTH), start.get(Calendar.DAY_OF_MONTH));
			endPicker.show(fm, "endPicker");
			return;
		}
	}
	
	/*
	 * Classes to listen for color changes
	 */
	private class CompletedColorChangeListener implements OnColorChangedListener{
		@Override
		public void onColorChanged(int color) {
			completedColor = color;
		}
	}
	
	private class RemainingColorChangeListener implements OnColorChangedListener{
		@Override
		public void onColorChanged(int color) {
			remainingColor = color;
		}
	}
}
