package com.tortel.deploytrack;

import java.util.List;

import com.tortel.deploytrack.data.*;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class DeploymentFragmentAdapter extends FragmentStatePagerAdapter {
	private List<Deployment> deployments;
	private DatabaseManager db;
	
	public DeploymentFragmentAdapter(Context context, FragmentManager fm){
		super(fm);
		db = DatabaseManager.getInstance(context);
		deployments = db.getAllDeployments();
	}
	
	public void reload(){
		Log.d("Reloading data");
		deployments = db.getAllDeployments();
		notifyDataSetChanged();
	}
	
	/**
	 * Get the ID of the deployment at the provided position.
	 * Returns -1 if it is the info fragment
	 * @param position
	 * @return
	 */
	public int getId(int position){
		if(position == deployments.size()){
			return -1;
		}
		return deployments.get(position).getId();
	}

	@Override
	public Fragment getItem(int position) {
		if(deployments.size() == position){
			return new NoDataFragment();
		}
		return DeploymentFragment.newInstance(deployments.get(position));
	}

	@Override
	public int getCount() {
		return deployments.size() + 1;
	}
	
	@Override
	public CharSequence getPageTitle(int position){
		if(deployments.size() == position){
			return "Welcome";
		}
		return deployments.get(position).getName();
	}

}
