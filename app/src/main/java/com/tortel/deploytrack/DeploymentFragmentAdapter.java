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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.SparseArray;

class DeploymentFragmentAdapter extends FragmentStatePagerAdapter {

	private List<Deployment> mDeploymentList;
	private DatabaseManager mDb;
	private Context mContext;
	private SparseArray<Fragment> mFragmentList;
	
	DeploymentFragmentAdapter(Context mContext, FragmentManager fm){
		super(fm);
		this.mContext = mContext.getApplicationContext();
		mDb = DatabaseManager.getInstance(mContext);
		mDeploymentList = mDb.getAllDeployments();
		mFragmentList = new SparseArray<>();

		LocalBroadcastManager.getInstance(mContext).registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Log.d("Got DATA_ADDED broadcast, reloading DeploymentFragmentAdapter");
				reload();
			}
		}, new IntentFilter(DatabaseManager.DATA_ADDED));
	}
	
	public void reload(){
		Log.d("Reloading data");
		mDeploymentList = mDb.getAllDeployments();
		notifyDataSetChanged();
	}
	
	/**
	 * Get the ID of the deployment at the provided position.
	 * Returns -1 if it is the info fragment
	 * @param position
	 * @return
	 */
	public int getId(int position){
		if(position >= mDeploymentList.size()){
			return -1;
		}

		return mDeploymentList.get(position).getId();
	}

	@Override
	public Fragment getItem(int position) {
		if(position >= mDeploymentList.size()){
			return new NoDataFragment();
		}
		if(mFragmentList.get(position) == null){
			Deployment deployment = mDeploymentList.get(position);
			Fragment fragment = null;
			// Get the correct type of fragment
			if(deployment.getDisplayType() == Deployment.DISPLAY_CIRCLE){
				fragment = DeploymentFragment.newInstance(deployment);
			} else {
				fragment = BarDeploymentFragment.newInstance(deployment);
			}
			mFragmentList.put(position, fragment);
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
