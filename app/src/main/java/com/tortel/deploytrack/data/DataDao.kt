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

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg deployments: Deployment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg widgetInfos: WidgetInfo)

    @Delete
    fun delete(deployment: Deployment): Int

    @Query("DELETE FROM deployment WHERE uuid = :uuid")
    fun deleteByUuid(uuid: String): Int

    @Delete
    fun delete(widgetInfo: WidgetInfo): Int

    @Query("DELETE from widgetinfo WHERE id = :id")
    fun deleteById(id: Int): Int

    @get:Query("SELECT * FROM deployment")
    val allDeployments: LiveData<List<Deployment>>

    @get:Query("SELECT * FROM deployment")
    val allDeploymentsSync: List<Deployment>

    @get:Query("SELECT * FROM widgetinfo")
    val allWidgetInfo: List<WidgetInfo>

    @Query("SELECT * FROM widgetinfo WHERE id = :id")
    fun getWidgetInfo(id: Int): WidgetInfo?

    @Query("SELECT * FROM deployment WHERE uuid = :uuid")
    fun getDeployment(uuid: String): LiveData<Deployment>?

    @Query("SELECT * FROM deployment WHERE uuid = :uuid")
    fun getDeploymentSync(uuid: String): Deployment?
}
