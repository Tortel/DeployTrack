/*
 * Copyright (C) 2013-2014 Scott Warner
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

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Class to save info about a homescreen widget
 */
@DatabaseTable
public class WidgetInfo {
    @DatabaseField(id = true)
    private int id;
    @DatabaseField(canBeNull = false, foreign=true)
    private Deployment deployment;
    @DatabaseField(defaultValue="false")
    private boolean lightText;
    
    public WidgetInfo(){
        //For ORMLite
    }
    
    public WidgetInfo(int id, Deployment deployment){
        this.id = id;
        this.deployment = deployment;
    }
    
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public Deployment getDeployment() {
        return deployment;
    }
    public void setDeployment(Deployment deployment) {
        this.deployment = deployment;
    }

    public boolean equals(Object o){
        if(o == null || !o.getClass().equals(getClass())){
            return false;
        }
        WidgetInfo other = (WidgetInfo) o;
        return other.id == id;
    }

    public boolean isLightText() {
        return lightText;
    }

    public void setLightText(boolean lightText) {
        this.lightText = lightText;
    }
}
