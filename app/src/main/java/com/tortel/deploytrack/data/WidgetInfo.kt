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
package com.tortel.deploytrack.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Class to save info dialog_about a homescreen widget
 */
@Entity(foreignKeys = [ForeignKey(entity = Deployment::class, parentColumns = ["uuid"], childColumns = ["deploymentId"], onDelete = CASCADE)], indices = [Index("deploymentId")])
data class WidgetInfo(@PrimaryKey var id: Int, var deploymentId: String, var isLightText: Boolean, var minWidth: Int, var minHeight: Int, var maxWidth: Int, var maxHeight: Int) {

    @Ignore
    var deployment: Deployment? = null

    @get:Ignore
    val isWide: Boolean
        get() = minWidth > 0 && minHeight > 0 && minWidth.toDouble() / minHeight.toDouble() > 1.5

    override fun equals(other: Any?): Boolean {
        if (other is WidgetInfo) {
            return id == other.id
        }
        return false
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + deploymentId.hashCode()
        return result
    }

}
