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

import com.crashlytics.android.Crashlytics;
import com.tortel.deploytrack.data.*;
import com.tortel.deploytrack.fragments.*;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

class DeploymentFragmentAdapter extends FragmentStatePagerAdapter {

	private List<Deployment> mDeploymentList;
	private Context mContext;
	private SparseArray<DeploymentFragment> mFragmentList;
	
	DeploymentFragmentAdapter(Context context, FragmentManager fm){
		super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
		this.mContext = context.getApplicationContext();
		mDeploymentList = DatabaseManager.getInstance(mContext).getAllDeployments();
		mFragmentList = new SparseArray<>();
	}

	/**
	 * Reload all the deployments from the database
	 */
	public void reloadData(){
		mDeploymentList = DatabaseManager.getInstance(mContext).getAllDeployments();
		mFragmentList.clear();
		this.notifyDataSetChanged();
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

	/**
	 * Remove the fragment with the ID provided
	 * @param uuid
	 */
	public void deploymentDeleted(String uuid) {
		if (uuid == null){
			return;
		}
		if(mFragmentList != null) {
			for(int i = 0; i < mFragmentList.size(); i++) {
				if (uuid.equals(mFragmentList.valueAt(i).getDeploymentId())) {
					mFragmentList.removeAt(i);
					return;
				}
			}
		}
	}

	@Override
	public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
		try {
			super.destroyItem(container, position, object);
		} catch (IndexOutOfBoundsException e) {
		    // Hopefully this wont screw up the state too much
            Crashlytics.logException(new Exception("OutOfBoundsException during destroyItem", e));
		}
	}

	@NonNull
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
		return Math.max(mDeploymentList.size(), 1);
	}
	
	@Override
	public CharSequence getPageTitle(int position){
		if(mDeploymentList.size() == 0){
			return mContext.getText(R.string.info);
		}
		return mDeploymentList.get(position).getName();
	}

	@Override
	public int getItemPosition(@NonNull Object object) {
		Log.v("Checking position of " + object);
		int position = POSITION_NONE;
		if (mDeploymentList != null && object instanceof DeploymentFragment) {
			DeploymentFragment fragment = (DeploymentFragment) object;
			Deployment deployment = fragment.getDeployment();
			position = mDeploymentList.indexOf(deployment);
			if (position < 0) {
				position = POSITION_NONE;
			}
		}
		Log.v("Returning position "+position);
		// Default to none
		return position;
	}

}
