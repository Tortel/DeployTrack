/*
 * Copyright (C) 2013-2023 Scott Warner
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
package com.tortel.deploytrack.data.ormlite;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.Days;

import android.annotation.SuppressLint;
import androidx.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Deployment implements Comparable<Deployment> {
	/**
	 * Use a circle-style display
	 */
	public static final int DISPLAY_CIRCLE = 0;
	/**
	 * Use a bar-style display
	 */
	public static final int DISPLAY_BAR = 1;

	private static SimpleDateFormat format;

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
	private int displayType = DISPLAY_CIRCLE;
    @DatabaseField(id = true)
    private String uuid;

	/**
	 * Update the fields to match the other deployment
     */
	void updateData(Deployment other){
		this.name = other.name;
		this.startDate = other.startDate;
		this.endDate = other.endDate;
		this.completedColor = other.completedColor;
		this.remainingColor = other.remainingColor;
		this.displayType = other.displayType;
	}

	@Override
	public int compareTo(@NonNull Deployment other) {
		/*
		 * Compare start dates, and if they are the same, use end dates
		 */
		int startCompare = startDate.compareTo(other.startDate);
		if(startCompare == 0){
			return endDate.compareTo(other.endDate);
		}
		return startCompare;
	}

	@Override
	public String toString(){
		return "[Deployment "+uuid+" "+name+"]";
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
	DateTime getStart(){
		return new DateTime(startDate);
	}

	@Exclude
	DateTime getEnd(){
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
	 */
	@Exclude
	public int getRemaining(){
		return getLength() - getCompleted();
	}
	
	/**
	 * Gets the percentage completed, as a whole number (0-100)
	 */
	@Exclude
	public int getPercentage(){
		return (int) ((double) getCompleted() / (double) getLength() * 100);
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


	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Deployment){
			Deployment o = (Deployment) obj;
			return uuid != null &&
					uuid.equals(o.uuid);
		}
		return false;
	}
}
