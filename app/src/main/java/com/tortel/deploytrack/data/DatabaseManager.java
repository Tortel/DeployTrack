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

import com.tortel.deploytrack.Log;

import java.sql.SQLException;
import java.util.List;

/**
 * Class to manage interaction with the database
 */
public class DatabaseManager {
    private static DatabaseManager instance;

    private DatabaseHelper helper;
    
    private List<Deployment> deploymentCache;

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
            
            if(deploymentCache != null){
                for(int i =0; i < deploymentCache.size(); i++){
                    if(deploymentCache.get(i).getId() == deployment.getId()){
                        deploymentCache.set(i, deployment);
                        return deployment;
                    }
                }
                deploymentCache.add(deployment);
            }
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
        if(deploymentCache != null){
            return deploymentCache;
        }
        List<Deployment> list = null;
        try{
            list = helper.getDeploymentDao().queryForAll();
        } catch(SQLException e){
            Log.e("Exception getting all Deployments", e);
        }
        deploymentCache = list;
        return list;
    }
    
    /**
     * Get a specific Deployment from the database
     * @param id
     * @return
     */
    public Deployment getDeployment(int id){
        if(deploymentCache != null){
            for(Deployment deployment : deploymentCache){
                if(deployment.getId() == id){
                    return deployment;
                }
            }
        }
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
    	Log.v("Deleting deployment "+id);
    	try{
    		helper.getDeploymentDao().deleteById(id);
    	} catch(SQLException e){
    		Log.e("Exception getting Deployment", e);
    	}
    	if(deploymentCache !=null){
        	for(int i=0; i < deploymentCache.size(); i++){
        	    if(deploymentCache.get(i).getId() == id){
        	        deploymentCache.remove(i);
        	        return;
        	    }
        	}
    	}
    }
    
    /**
     * Gets the widget information for the specified ID
     * @return
     */
    public List<WidgetInfo> getAllWidgetInfo(){
    	Log.v("Getting all widget information");
        try{
            List<WidgetInfo> list = helper.getWidgetInfoDao().queryForAll();
            if(list != null){
            	for(WidgetInfo info : list){
            		helper.getDeploymentDao().refresh(info.getDeployment());
            	}
            }
            return list; 
        } catch(SQLException e){
            Log.e("Exception getting widget info", e);
            return null;
        }
    }
    
    /**
     * Gets the widget information for the specified ID
     * @param id
     * @return
     */
    public WidgetInfo getWidgetInfo(int id){
    	Log.v("Getting widget info for "+id);
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
    	Log.v("Saving widget info for "+info.getId());
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
    	Log.v("Deleting widget info "+id);
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
        helper.onUpgrade(helper.getWritableDatabase(), 0, 1);
        deploymentCache = null;
    }

}