/*
 * Copyright (C) 2013-2015 Scott Warner
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
package com.tortel.deploytrack.data;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.Days;

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
	@DatabaseField
	private int completedColor;
	@DatabaseField
	private int remainingColor;
	
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
	
	/**
	 * Returns the length of the deployment, in days
	 * @return the length
	 */
	public int getLength(){
		return Days.daysBetween(getStart(), getEnd()).getDays();
	}
	
	/**
	 * Get the number of days completed so far
	 * @return
	 */
	public int getCompleted(){
		DateTime start = getStart();
		//Check if it has even started
		if(start.isAfterNow()){
			return 0;
		}
		return Math.min(
				Days.daysBetween(start, new DateTime()).getDays(),
				getLength());
	}
	
	/**
	 * Get the remaining time, in days
	 * @return
	 */
	public int getRemaining(){
		return getLength() - getCompleted();
	}
	
	/**
	 * Gets the percentage completed, as a whole number (0-100)
	 * @return
	 */
	public int getPercentage(){
		return (int) ((double) getCompleted() / (double) getLength() * 100);
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
	public int getCompletedColor() {
		return completedColor;
	}
	public void setCompletedColor(int completedColor) {
		this.completedColor = completedColor;
	}
	public int getRemainingColor() {
		return remainingColor;
	}
	public void setRemainingColor(int remainingColor) {
		this.remainingColor = remainingColor;
	}
}
