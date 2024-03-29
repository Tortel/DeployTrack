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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.tortel.deploytrack.Log;

import java.io.File;
import java.sql.SQLException;

/**
 * Database helper class for the old data structures
 */
public class OldDatabaseHelper extends OrmLiteSqliteOpenHelper {
    private static final String DATABASE_NAME = "data.sqlite";

    private static final int DATABASE_VERSION = 5;

    private Dao<OldDeployment, Integer> deploymentDao;
    private Dao<OldWidgetInfo, Integer> widgetInfoDao;

    public OldDatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase databse, ConnectionSource connectionSource){
        try{
            TableUtils.createTable(connectionSource, OldDeployment.class);
            TableUtils.createTable(connectionSource, OldWidgetInfo.class);
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
                TableUtils.dropTable(connectionSource, OldDeployment.class, true);
                TableUtils.dropTable(connectionSource, OldWidgetInfo.class, true);
                return;
            }
            if(oldVersion == 1){
                //For version 2, I added the WidgetInfo table
                TableUtils.createTable(connectionSource, OldWidgetInfo.class);
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
            if(oldVersion == 4){
                // Add the UUID field
                db.execSQL("ALTER TABLE `deployment` ADD COLUMN uuid VARCHAR DEFAULT NULL");
            }
        } catch(SQLException e){
            Log.e("Error while recreating database", e);
            throw new RuntimeException(e);
        }
    }

    public Dao<OldDeployment, Integer> getDeploymentDao(){
        if(deploymentDao == null){
            try{
            	deploymentDao = getDao(OldDeployment.class);
            } catch(SQLException e){
                Log.e("Error getting Deployment DAO", e);
            }
        }
        return deploymentDao;
    }
    
    public Dao<OldWidgetInfo, Integer> getWidgetInfoDao(){
        if(widgetInfoDao == null){
            try{
                widgetInfoDao = getDao(OldWidgetInfo.class);
            } catch(SQLException e){
                Log.e("Error getting widget info DAO", e);
            }
        }
        return widgetInfoDao;
    }

    /**
     * Check if the old database file is present
     */
    public static boolean oldDatabaseExists(Context context){
        File databaseFile = context.getDatabasePath(DATABASE_NAME);
        return databaseFile.exists();
    }

    /**
     * Delete the database file
     */
    public static void deleteDbFiles(Context context){
        if(!context.getDatabasePath(DATABASE_NAME).delete()){
            context.getDatabasePath(DATABASE_NAME).deleteOnExit();
        }
        if(!context.getDatabasePath(DATABASE_NAME+"-journal").delete()){
            context.getDatabasePath(DATABASE_NAME+"-journal").deleteOnExit();
        }
    }
}
