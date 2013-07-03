package com.tortel.deploytrack;

import java.util.List;

import com.tortel.deploytrack.data.*;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class DeploymentFragmentAdapter extends FragmentPagerAdapter {
	private List<Deployment> deployments;
	private DatabaseManager db;
	
	public DeploymentFragmentAdapter(Context context, FragmentManager fm){
		super(fm);
		db = DatabaseManager.getInstance(context);
		deployments = db.getAllDeployments();
	}

	@Override
	public Fragment getItem(int position) {
		return DeploymentFragment.newInstance(deployments.get(position));
	}

	@Override
	public int getCount() {
		return deployments.size();
	}
	
	@Override
	public CharSequence getPageTitle(int position){
		return deployments.get(position).getName();
	}

}
