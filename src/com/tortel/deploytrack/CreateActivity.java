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
	
	private SimpleDateFormat format;
	
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
		
		disableButton(endButton);
		disableButton(saveButton);
		
		start = Calendar.getInstance();
		end = (Calendar) start.clone();
		end.add(Calendar.YEAR, 20);
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
			//Create it
			Deployment tmp = new Deployment();
			tmp.setStartDate(start.getTime());
			tmp.setEndDate(end.getTime());
			tmp.setName(name);
			//Save it
			DatabaseManager.getInstance(this).saveDeployment(tmp);
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
}
