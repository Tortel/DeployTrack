/*
 * Copyright (C) 2013 Scott Warner
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

import com.tortel.deploytrack.Log;

import java.sql.SQLException;
import java.util.List;

/**
 * Clas to manage interaction with the database
 */
public class DatabaseManager {
    private static DatabaseManager instance;

    private DatabaseHelper helper;

    public static DatabaseManager getInstance(Context context){
        if(instance == null){
            instance = new DatabaseManager(context);
        }
        return instance;
    }

    private DatabaseManager(Context context){
        helper = new DatabaseHelper(context.getApplicationContext());
    }

    public Deployment saveDeployment(Deployment deployment){
        try{
            helper.getDeploymentDao().createOrUpdate(deployment);
        } catch(SQLException e){
            Log.e("Error saving Deployment", e);
        }
        return deployment;
    }

    /**
     * Get all the saved GeoEvents
     * @return
     */
    public List<Deployment> getAllDeployments(){
        List<Deployment> list = null;
        try{
            list = helper.getDeploymentDao().queryForAll();
        } catch(SQLException e){
            Log.e("Exception getting all Deployments", e);
        }
        return list;
    }
    
    /**
     * Get a specific Deployment from the database
     * @param id
     * @return
     */
    public Deployment getDeployment(int id){
    	Deployment tmp = null;
    	try{
    		tmp = helper.getDeploymentDao().queryForId(id);
    	} catch(SQLException e){
    		Log.e("Exception getting Deployment", e);
    	}
    	return tmp;
    }
    
    /**
     * Delete a specific Deployment from the database
     * @param id
     * @return
     */
    public void deleteDeployment(int id){
    	try{
    		helper.getDeploymentDao().deleteById(id);
    	} catch(SQLException e){
    		Log.e("Exception getting Deployment", e);
    	}
    }
    
    /**
     * Gets the widget information for the specified ID
     * @param id
     * @return
     */
    public WidgetInfo getWidgetInfo(int id){
        try{
            WidgetInfo info = helper.getWidgetInfoDao().queryForId(id);
            if(info != null){
                helper.getDeploymentDao().refresh(info.getDeployment());
            }
            return info; 
        } catch(SQLException e){
            Log.e("Exception getting widget info", e);
            return null;
        }
    }

    /**
     * Saves the WidgetInfo
     * @param info
     */
    public void saveWidgetInfo(WidgetInfo info){
        try{
            helper.getWidgetInfoDao().createOrUpdate(info);
        } catch(SQLException e){
            Log.e("Exception getting widget info", e);
        }
    }
    
    /**
     * Deletes the widget information from the database
     * @param id
     */
    public void deleteWidgetInfo(int id){
        try{
            helper.getWidgetInfoDao().deleteById(id);
        } catch(SQLException e){
            Log.e("Error deleting widget info", e);
        }
    }
    
    /**
     * Clear the entire database
     */
    public void clear(){
        helper.onUpgrade(helper.getWritableDatabase(), 1, 2);
    }

}