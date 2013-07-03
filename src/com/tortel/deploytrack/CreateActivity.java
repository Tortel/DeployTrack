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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.squareup.timessquare.CalendarPickerView;
import com.squareup.timessquare.CalendarPickerView.SelectionMode;

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
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create);
		
		format = new SimpleDateFormat("MMM dd, yyyy");
		
		//TODO: Get the views
	}
	
	public void onClick(View v){
		switch(v.getId()){
		case R.id.button_cancel:
			this.finish();
			return;
		case R.id.button_save:
			//TODO: Save and finish
			return;
		case R.id.button_start:
			//TODO: Open datepicker
			return;
		case R.id.button_end:
			//TODO: datepicker
			return;
		}
	}
	
	public void setDate(DatePickType type, Date date){
		if(type == DatePickType.START){
			start = date;
			startButton.setText(getResources().getString(R.string.start_date)+
					"\n"+format.format(start));
			endButton.setEnabled(true);
		} else {
			end = date;
			startButton.setText(getResources().getString(R.string.end_date)+
					"\n"+format.format(end));
			saveButton.setEnabled(true);
		}
	}
	
	public enum DatePickType{
		START, END
	}
	
	@SuppressLint("ValidFragment")
	public static class DatePickDialog extends SherlockDialogFragment{
		private CalendarPickerView calendar;
		private CreateActivity mActivity;
		private Date min;
		private DatePickType type;
		
		public DatePickDialog(){
			this(new Date());
		}
		
		public DatePickDialog(Date min){
			this.min = min;
		}
		
		public void onAttach(Activity activity){
			super.onAttach(activity);
			mActivity = (CreateActivity) activity;
		}
		
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
	        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	        // Get the layout inflater
	        LayoutInflater inflater = getActivity().getLayoutInflater();
	        Calendar start = Calendar.getInstance();
	        Calendar end = Calendar.getInstance();
	        
	        //If a date is specified, use that
	        if(min != null){
	        	start.setTimeInMillis(min.getTime());
	        	end.setTimeInMillis(start.getTimeInMillis());
	        } else {
	        	start.add(Calendar.YEAR, -2);
	        }
	        
	        end.add(Calendar.YEAR, 2);
	        
	        View view = inflater.inflate(R.layout.dialog_datepicker, null);
	        calendar = (CalendarPickerView) view.findViewById(R.id.calendar_view);
	        
	        //4 year range, with today selected
	        calendar.init(start.getTime(), end.getTime())
	        	.inMode(SelectionMode.SINGLE)
	        	.withSelectedDate(new Date());

	        // Inflate and set the layout for the dialog
	        // Pass null as the parent view because its going in the dialog layout
	        builder.setView(view)
	        // Add action buttons
	               .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
	                   @Override
	                   public void onClick(DialogInterface dialog, int id) {
	                       // Pass the date on up to the activity
	                	   mActivity.setDate(type, calendar.getSelectedDate());
	                	   DatePickDialog.this.getDialog().dismiss();
	                   }
	               })
	               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {
	                	   DatePickDialog.this.getDialog().cancel();
	                   }
	               });      
	        return builder.create();
	    }
	}
}
