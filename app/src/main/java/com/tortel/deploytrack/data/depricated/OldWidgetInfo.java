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

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Class to save info dialog_about a homescreen widget
 */
@DatabaseTable(tableName = "widgetinfo")
public class OldWidgetInfo {
    @DatabaseField(id = true)
    private int id;
    @DatabaseField(canBeNull = false, foreign=true)
    private OldDeployment deployment;
    @DatabaseField(defaultValue="false")
    private boolean lightText;
    @DatabaseField(defaultValue="0")
    private int minWidth;
    @DatabaseField(defaultValue="0")
    private int minHeight;
    @DatabaseField(defaultValue="0")
    private int maxWidth;
    @DatabaseField(defaultValue="0")
    private int maxHeight;
    
    public OldWidgetInfo(){
        //For ORMLite
    }
    
    public OldWidgetInfo(int id, OldDeployment deployment){
        this.id = id;
        this.deployment = deployment;
    }
    
    public boolean isWide(){
        if(minWidth > 0 && minHeight > 0){
            return (double) minWidth / (double) minHeight > 1.5;
        }
        return false;
    }
    
    public boolean equals(Object o){
        if(o == null || !o.getClass().equals(getClass())){
            return false;
        }
        OldWidgetInfo other = (OldWidgetInfo) o;
        return other.id == id;
    }
    
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public OldDeployment getDeployment() {
        return deployment;
    }
    public void setDeployment(OldDeployment deployment) {
        this.deployment = deployment;
    }
    public boolean isLightText() {
        return lightText;
    }
    public void setLightText(boolean lightText) {
        this.lightText = lightText;
    }
    public int getMinWidth() {
        return minWidth;
    }
    public void setMinWidth(int minWidth) {
        this.minWidth = minWidth;
    }
    public int getMinHeight() {
        return minHeight;
    }
    public void setMinHeight(int minHeight) {
        this.minHeight = minHeight;
    }
    public int getMaxWidth() {
        return maxWidth;
    }
    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }
    public int getMaxHeight() {
        return maxHeight;
    }
    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }
}
