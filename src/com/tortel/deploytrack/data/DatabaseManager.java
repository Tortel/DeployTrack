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
     * Clear the entire database
     */
    public void clear(){
        helper.onUpgrade(helper.getWritableDatabase(), 1, 2);
    }

}