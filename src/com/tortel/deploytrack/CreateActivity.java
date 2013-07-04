package com.tortel.deploytrack;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.squareup.timessquare.CalendarPickerView;
import com.squareup.timessquare.CalendarPickerView.SelectionMode;
import com.tortel.deploytrack.data.*;

/**
 * Activity for creating a new Deployment
 */
public class CreateActivity extends SherlockFragmentActivity {
	private Date start;
	private Date end;
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
			tmp.setStartDate(start);
			tmp.setEndDate(end);
			tmp.setName(name);
			//Save it
			DatabaseManager.getInstance(this).saveDeployment(tmp);
			//End
			finish();
			return;
		case R.id.button_start:

			return;
		case R.id.button_end:

			return;
		}
	}
}
