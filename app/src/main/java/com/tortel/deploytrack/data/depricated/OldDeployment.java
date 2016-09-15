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
package com.tortel.deploytrack.data.depricated;

import android.annotation.SuppressLint;

import com.google.firebase.database.Exclude;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.tortel.deploytrack.data.Deployment;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@DatabaseTable(tableName = "deployment")
public class OldDeployment {
	/**
	 * Use a circle-style display
	 */
	public static final int DISPLAY_CIRCLE = 0;
	/**
	 * Use a bar-style display
	 */
	public static final int DISPLAY_BAR = 1;

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
	@DatabaseField
	private int displayType;
    @DatabaseField
    private String uuid;

	/**
	 * Update the fields to match the other deployment
	 * @param other
     */
	public void updateData(OldDeployment other){
		this.name = other.name;
		this.startDate = other.startDate;
		this.endDate = other.endDate;
		this.completedColor = other.completedColor;
		this.remainingColor = other.remainingColor;
		this.displayType = other.displayType;
	}

	@Exclude
	public String getFormattedStart(){
		if(format == null){
			getFormat();
		}
		return format.format(startDate);
	}

	@Exclude
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

	@Exclude
	public DateTime getStart(){
		return new DateTime(startDate);
	}

	@Exclude
	public DateTime getEnd(){
		return new DateTime(endDate);
	}
	
	/**
	 * Returns the length of the deployment, in days
	 * @return the length
	 */
	@Exclude
	public int getLength(){
		return Days.daysBetween(getStart(), getEnd()).getDays();
	}
	
	/**
	 * Get the number of days completed so far
	 * @return
	 */
	@Exclude
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
	@Exclude
	public int getRemaining(){
		return getLength() - getCompleted();
	}
	
	/**
	 * Gets the percentage completed, as a whole number (0-100)
	 * @return
	 */
	@Exclude
	public int getPercentage(){
		return (int) ((double) getCompleted() / (double) getLength() * 100);
	}
	@Exclude
	public int getId() {
		return id;
	}
	@Exclude
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
	public int getDisplayType() {
		return displayType;
	}
	public void setDisplayType(int displayType) {
		this.displayType = displayType;
	}

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
	@Exclude
	public void setUuid(UUID uuid) {
		this.uuid = uuid.toString();
	}

	/**
	 * Create a new Deployment object with all the information
	 * @return
     */
	public Deployment getUpdatedObject(){
		Deployment updated = new Deployment();
		updated.setUuid(uuid);
		updated.setName(name);
		updated.setCompletedColor(completedColor);
		updated.setRemainingColor(remainingColor);
		updated.setStartDate(startDate);
		updated.setEndDate(endDate);
		updated.setDisplayType(displayType);

		return updated;
	}
}
