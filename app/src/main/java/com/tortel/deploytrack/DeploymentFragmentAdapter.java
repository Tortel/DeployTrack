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
package com.tortel.deploytrack;

import java.util.List;

import com.tortel.deploytrack.data.*;
import com.tortel.deploytrack.fragments.*;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;

class DeploymentFragmentAdapter extends FragmentStatePagerAdapter {

	private List<Deployment> mDeploymentList;
	private Context mContext;
	private SparseArray<DeploymentFragment> mFragmentList;
	
	DeploymentFragmentAdapter(Context context, FragmentManager fm){
		super(fm);
		this.mContext = context.getApplicationContext();
		mDeploymentList = DatabaseManager.getInstance(mContext).getAllDeployments();
		mFragmentList = new SparseArray<>();
	}

	/**
	 * Reload all the deployments from the database
	 */
	public void reloadData(){
		mDeploymentList = DatabaseManager.getInstance(mContext).getAllDeployments();
		this.notifyDataSetChanged();
		mFragmentList.clear();
	}
	
	/**
	 * Get the ID of the deployment at the provided position.
	 * Returns -1 if it is the info fragment
	 */
	public String getId(int position){
		if(position >= mDeploymentList.size()){
			return null;
		}

		return mDeploymentList.get(position).getUuid();
	}

	@Override
	public Fragment getItem(int position) {
		if(position >= mDeploymentList.size()){
			return new NoDataFragment();
		}
		if(mFragmentList.get(position) == null ||
				mFragmentList.get(position).getDeployment() == null){
			Deployment deployment = mDeploymentList.get(position);
			mFragmentList.put(position, DeploymentFragment.newInstance(deployment));
		}
		return mFragmentList.get(position);
	}

	@Override
	public int getCount() {
		if(mDeploymentList.size() == 0){
			return 1;
		}
		return mDeploymentList.size();
	}
	
	@Override
	public CharSequence getPageTitle(int position){
		if(mDeploymentList.size() == 0){
			return mContext.getText(R.string.info);
		}
		return mDeploymentList.get(position).getName();
	}

}
