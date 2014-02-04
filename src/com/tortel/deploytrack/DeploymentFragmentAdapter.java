/*
 * Copyright (C) 2013-2014 Scott Warner
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
package com.tortel.deploytrack;

import java.util.List;

import com.tortel.deploytrack.data.*;
import com.tortel.deploytrack.fragments.DeploymentFragment;
import com.tortel.deploytrack.fragments.NoDataFragment;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;

public class DeploymentFragmentAdapter extends FragmentStatePagerAdapter {
	private List<Deployment> deployments;
	private DatabaseManager db;
	private Context context;
	SparseArray<Fragment> fragmentList;
	
	public DeploymentFragmentAdapter(Context context, FragmentManager fm){
		super(fm);
		this.context = context.getApplicationContext();
		db = DatabaseManager.getInstance(context);
		deployments = db.getAllDeployments();
		fragmentList = new SparseArray<Fragment>();
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
		if(deployments.size() == 0){
			return -1;
		}

		return deployments.get(position).getId();
	}

	@Override
	public Fragment getItem(int position) {
		if(deployments.size() == position){
			return new NoDataFragment();
		}
		if(fragmentList.get(position) == null){
			fragmentList.put(position, DeploymentFragment.newInstance(deployments.get(position)));
		}
		return fragmentList.get(position);
	}

	@Override
	public int getCount() {
		if(deployments.size() == 0){
			return 1;
		}
		return deployments.size();
	}
	
	@Override
	public CharSequence getPageTitle(int position){
		if(deployments.size() == 0){
			return context.getText(R.string.info);
		}
		return deployments.get(position).getName();
	}

}
