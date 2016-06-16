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
package com.tortel.deploytrack.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.tortel.deploytrack.Log;

import java.sql.SQLException;

/**
 *
 */
class DatabaseHelper extends OrmLiteSqliteOpenHelper {
    private static final String DATABSE_NAME = "data.sqlite";

    private static final int DATABASE_VERSION = 4;

    private Dao<Deployment, Integer> deploymentDao;
    private Dao<WidgetInfo, Integer> widgetInfoDao;

    DatabaseHelper(Context context){
        super(context, DATABSE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase databse, ConnectionSource connectionSource){
        try{
            TableUtils.createTable(connectionSource, Deployment.class);
            TableUtils.createTable(connectionSource, WidgetInfo.class);
        } catch(SQLException e) {
            Log.e("Cant create database", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion){
        try{
            // Drop everything
            if(oldVersion == 0){
                TableUtils.dropTable(connectionSource, Deployment.class, true);
                TableUtils.dropTable(connectionSource, WidgetInfo.class, true);
                return;
            }
            if(oldVersion == 1){
                //For version 2, I added the WidgetInfo table
                TableUtils.createTable(connectionSource, WidgetInfo.class);
                oldVersion = 2;
            }
            if(oldVersion == 2){
                // Add the lightText field
                db.execSQL("ALTER TABLE `widgetinfo` ADD COLUMN lightText BOOLEAN DEFAULT 1");
                db.execSQL("ALTER TABLE `widgetinfo` ADD COLUMN minWidth INTEGER DEFAULT 0");
                db.execSQL("ALTER TABLE `widgetinfo` ADD COLUMN maxWidth INTEGER DEFAULT 0");
                db.execSQL("ALTER TABLE `widgetinfo` ADD COLUMN minHeight INTEGER DEFAULT 0");
                db.execSQL("ALTER TABLE `widgetinfo` ADD COLUMN maxHeight INTEGER DEFAULT 0");
                oldVersion = 3;
            }
            if(oldVersion == 3){
                // Add the displayType field
                db.execSQL("ALTER TABLE `deployment` ADD COLUMN displayType INTEGER DEFAULT 0");
                oldVersion = 4;
            }
        } catch(SQLException e){
            Log.e("Error while recreating database", e);
            throw new RuntimeException(e);
        }
    }

    Dao<Deployment, Integer> getDeploymentDao(){
        if(deploymentDao == null){
            try{
            	deploymentDao = getDao(Deployment.class);
            } catch(SQLException e){
                Log.e("Error getting Deployment DAO", e);
            }
        }
        return deploymentDao;
    }
    
    Dao<WidgetInfo, Integer> getWidgetInfoDao(){
        if(widgetInfoDao == null){
            try{
                widgetInfoDao = getDao(WidgetInfo.class);
            } catch(SQLException e){
                Log.e("Error getting widget info DAO", e);
            }
        }
        return widgetInfoDao;
    }
}
