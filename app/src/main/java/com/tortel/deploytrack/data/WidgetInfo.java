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
package com.tortel.deploytrack.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Class to save info dialog_about a homescreen widget
 */
@Entity(foreignKeys = @ForeignKey(entity = Deployment.class,
        parentColumns = "uuid",
        childColumns = "deploymentId",
        onDelete = ForeignKey.CASCADE),
    indices = {@Index("deploymentId")})
public class WidgetInfo {
    @PrimaryKey
    private int id;

    @Ignore
    private Deployment deployment;
    private String deploymentId;
    private boolean lightText = false;
    private int minWidth = 0;
    private int minHeight = 0;
    private int maxWidth = 0;
    private int maxHeight = 0;

    public WidgetInfo() {
        // For Room
    }

    @Ignore
    public WidgetInfo(int id, @NonNull Deployment deployment) {
        this.id = id;
        this.deployment = deployment;
        this.deploymentId = deployment.getUuid();
    }

    @Ignore
    public boolean isWide() {
        return (minWidth > 0 && minHeight > 0) && (double) minWidth / (double) minHeight > 1.5;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !o.getClass().equals(getClass())) {
            return false;
        }
        WidgetInfo other = (WidgetInfo) o;
        return other.id == id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Ignore
    public Deployment getDeployment() {
        return deployment;
    }

    @Ignore
    public void setDeployment(Deployment deployment) {
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

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }
}
