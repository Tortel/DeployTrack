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
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Deployment... deployments);

    @Insert(onConflict =  OnConflictStrategy.REPLACE)
    void insert(WidgetInfo... widgetInfos);

    @Delete
    int delete(Deployment deployment);

    @Query("DELETE FROM deployment WHERE uuid = :uuid")
    int deleteByUuid(String uuid);

    @Delete
    int delete(WidgetInfo widgetInfo);

    @Query("DELETE from widgetinfo WHERE id = :id")
    int deleteById(int id);

    @Query("SELECT * FROM deployment")
    List<Deployment> getAllDeployments();

    @Query("SELECT * FROM widgetinfo")
    List<WidgetInfo> getAllWidgetInfo();

    @Query("SELECT * FROM widgetinfo WHERE id = :id")
    WidgetInfo getWidgetInfo(int id);

    @Query("SELECT * FROM deployment WHERE uuid = :uuid")
    Deployment getDeployment(@NonNull String uuid);
}
