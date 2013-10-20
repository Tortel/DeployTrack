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
package com.tortel.deploytrack.fragments;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.echo.holographlibrary.PieSlice;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.tortel.deploytrack.Prefs;
import com.tortel.deploytrack.R;
import com.tortel.deploytrack.data.DatabaseManager;
import com.tortel.deploytrack.data.Deployment;
import com.tortel.deploytrack.view.CustomPieGraph;

/**
 * Fragment that displays the fancy deployment graph and info
 */
public class DeploymentFragment extends SherlockFragment {
	private Deployment deployment;
	private TextView percentage;
	private CustomPieGraph pie;
	
	/**
	 * Creates a new DeploymentFragment with the provided
	 * Deployment
	 * @param deployment
	 * @return
	 */
	public static DeploymentFragment newInstance(Deployment deployment){
		DeploymentFragment fragment = new DeploymentFragment();
		fragment.deployment = deployment;
		return fragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState){
		Resources resources = getActivity().getResources();
		
		View view = inflater.inflate(R.layout.fragment_deployment, container, false);
		
		//Date range
		TextView dateRange = (TextView) view.findViewById(R.id.daterange);
		dateRange.setText(resources.getString(R.string.date_range,
						deployment.getFormattedStart(), deployment.getFormattedEnd()));
		
		//Get the needed values
		int completed = deployment.getCompleted();
		int remaining = deployment.getRemaining();
		
		//Days completed, days left
		TextView stats = (TextView) view.findViewById(R.id.secondary);
		stats.setText(resources.getString(R.string.date_stats, completed, remaining));
		
		//Percentage
		percentage = (TextView) view.findViewById(R.id.main);
		percentage.setText("0%");
		
		//Fill the graph
		pie = (CustomPieGraph) view.findViewById(R.id.graph);
		
		PieSlice completedSlice = new PieSlice();
		completedSlice.setColor(deployment.getCompletedColor());
		completedSlice.setValue(completed);
		if(completed > 0){
			pie.addSlice(completedSlice);
		}
		PieSlice togoSlice = new PieSlice();
		togoSlice.setColor(deployment.getRemainingColor());
		togoSlice.setValue(remaining);
		if(remaining > 0){
			pie.addSlice(togoSlice);
		}
		
		return view;
	}
	
	@Override
	public void onResume(){
		super.onResume();
		animate();
	}
	
	@Override
	public void onPause(){
		super.onPause();
		if(pie != null){
			pie.setPercent(0);
		}
	}
	
	public void animate(){
		if(pie == null || !Prefs.isAnimationEnabled()){
			setPercent(deployment.getPercentage());
			return;
		}
		AnimatorSet set = new AnimatorSet();
		set.playTogether(
				ObjectAnimator.ofFloat(pie, "percent", 0, 100),
				ObjectAnimator.ofInt(this, "percent", 0, deployment.getPercentage())
		);
		set.setDuration(1000);
		set.start();
	}
	
	public void setPercent(int percent){
		if(percentage != null){
			percentage.setText(percent+"%");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(savedInstanceState != null){
			int id = savedInstanceState.getInt("id");
			deployment = DatabaseManager.getInstance(getActivity()).getDeployment(id);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("id", deployment.getId());
	}
}
