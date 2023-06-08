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
import com.tortel.deploytrack.data.ormlite.ORMLiteDeployment;
import com.tortel.deploytrack.data.ormlite.ORMLiteWidgetInfo;

/**
 * Class to save info dialog_about a homescreen widget
 */
@SuppressWarnings("unused")
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

    /**
     * Get the newer WidgetInfo class with all the data here
     */
    public ORMLiteWidgetInfo getUpdatedObject(ORMLiteDeployment deployment){
        ORMLiteWidgetInfo updated = new ORMLiteWidgetInfo();

        updated.setId(id);
        updated.setDeployment(deployment);
        updated.setLightText(lightText);
        updated.setMinWidth(minWidth);
        updated.setMinHeight(minHeight);
        updated.setMaxWidth(maxWidth);
        updated.setMaxHeight(maxHeight);

        return updated;
    }
}
