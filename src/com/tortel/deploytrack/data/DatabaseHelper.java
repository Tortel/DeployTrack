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
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
    private static final String DATABSE_NAME = "data.sqlite";

    private static final int DATABASE_VERSION = 1;

    private Dao<Deployment, Integer> deploymentDao;

    public DatabaseHelper(Context context){
        super(context, DATABSE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase databse, ConnectionSource connectionSource){
        try{
            TableUtils.createTable(connectionSource, Deployment.class);
        } catch(SQLException e) {
            Log.e("Cant create database", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion){
        try{
            TableUtils.dropTable(connectionSource, Deployment.class, true);
        } catch(SQLException e){
            Log.e("Error while recreating database", e);
            throw new RuntimeException(e);
        }
    }

    public Dao<Deployment, Integer> getDeploymentDao(){
        if(deploymentDao == null){
            try{
            	deploymentDao = getDao(Deployment.class);
            } catch(SQLException e){
                Log.e("Error getting Deployment DAO", e);
            }
        }
        return deploymentDao;
    }
}
