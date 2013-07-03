package com.tortel.deploytrack.data;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.DateTime;

import android.annotation.SuppressLint;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Deployment {
	private static SimpleDateFormat format;
	
	@DatabaseField(generatedId = true)
	private int id;
	@DatabaseField
	private String name;
	@DatabaseField
	private Date startDate;
	@DatabaseField
	private Date endDate;
	
	//TODO: Maybe custom colors later?
	
	public String getFormattedStart(){
		if(format == null){
			getFormat();
		}
		return format.format(startDate);
	}
	
	public String getFormattedEnd(){
		if(format == null){
			getFormat();
		}
		return format.format(endDate);
	}
	
	@SuppressLint("SimpleDateFormat")
	private void getFormat(){
		format = new SimpleDateFormat("MMM dd, yyyy");
	}
	
	public DateTime getStart(){
		return new DateTime(startDate);
	}
	
	public DateTime getEnd(){
		return new DateTime(endDate);
	}
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
}
