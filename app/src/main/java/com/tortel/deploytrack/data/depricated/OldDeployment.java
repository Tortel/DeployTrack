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
package com.tortel.deploytrack.data.depricated;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.tortel.deploytrack.data.Deployment;

import java.util.Date;
import java.util.UUID;

@SuppressWarnings("unused")
@DatabaseTable(tableName = "deployment")
public class OldDeployment {

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
    public String getUuid() {
        return uuid;
    }
	public void setUuid(UUID uuid) {
		this.uuid = uuid.toString();
	}

	/**
	 * Create a new Deployment object with all the information
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
