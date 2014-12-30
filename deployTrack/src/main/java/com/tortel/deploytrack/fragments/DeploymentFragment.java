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
package com.tortel.deploytrack.fragments;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.echo.holographlibrary.PieSlice;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.tortel.deploytrack.Log;
import com.tortel.deploytrack.Prefs;
import com.tortel.deploytrack.R;
import com.tortel.deploytrack.data.DatabaseManager;
import com.tortel.deploytrack.data.Deployment;
import com.tortel.deploytrack.view.CustomPieGraph;

/**
 * Fragment that displays the fancy deployment graph and info
 */
public class DeploymentFragment extends Fragment {
	private static final boolean DEBUG = false;
	
	private Deployment deployment;
	private TextView percentage;
	private TextView completed;
	private TextView dateRange;
	private TextView remaining;
	private TextView comma;
	private Resources resources;
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
		if(DEBUG){
			fragment.printAllInfo();
		}
		return fragment;
	}
	
	private void printAllInfo(){
		Log.v("Deployment id "+deployment.getId());
		Log.v("Start date: "+deployment.getFormattedStart());
		Log.v("End date: "+deployment.getFormattedEnd());
		Log.v("Days complete: "+deployment.getCompleted());
		Log.v("Days left: "+deployment.getRemaining());
		Log.v("Percentage: "+deployment.getPercentage());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState){
		resources = getActivity().getResources();
		
		View view = inflater.inflate(R.layout.fragment_deployment, container, false);

        // Set up the views
        setUpTextViews(view);

		dateRange.setText(resources.getString(R.string.date_range,
						deployment.getFormattedStart(), deployment.getFormattedEnd()));
		completed.setText(resources.getQuantityString(R.plurals.days_complete, deployment.getCompleted(), deployment.getCompleted()));
        remaining.setText(resources.getQuantityString(R.plurals.days_remaining, deployment.getRemaining(), deployment.getRemaining()));
		
		// Hide or display percent
		if(Prefs.hidePercent()){
		    percentage.setVisibility(View.GONE);
		} else {
		    percentage.setText(deployment.getPercentage()+"%");
		}
		
		// If everything is hidden, show the title to fill space
		if(Prefs.hideAll()){
		    // Use the comma view, because it isn't touched by any animators
		    comma.setText(deployment.getName());
		    comma.setVisibility(View.VISIBLE);
		    // Apply the main styling
		    comma.setTextAppearance(getActivity(), R.style.Percentage);
		}
		
		//Fill the graph
		pie = (CustomPieGraph) view.findViewById(R.id.graph);
		
		PieSlice completedSlice = new PieSlice();
		completedSlice.setColor(deployment.getCompletedColor());
		completedSlice.setValue(deployment.getCompleted());
		if(deployment.getCompleted() > 0){
			pie.addSlice(completedSlice);
		}
		PieSlice togoSlice = new PieSlice();
		togoSlice.setColor(deployment.getRemainingColor());
		togoSlice.setValue(deployment.getRemaining());
		if(deployment.getRemaining() > 0){
			pie.addSlice(togoSlice);
		}
		
		return view;
	}
	
	@SuppressLint("CutPasteId")
	private void setUpTextViews(View view){
		float density = getResources().getDisplayMetrics().density;
		comma = (TextView) view.findViewById(R.id.comma);
		dateRange = (TextView) view.findViewById(R.id.daterange);
		
		switch(Prefs.getMainDisplayType()){
		case Prefs.ViewTypes.PERCENT:
			percentage = (TextView) view.findViewById(R.id.main);
			completed = (TextView) view.findViewById(R.id.second);
			remaining = (TextView) view.findViewById(R.id.third);
			if(Prefs.hideDate()){
			    comma.setVisibility(View.GONE);
		        dateRange.setVisibility(View.GONE);
		        completed.setVisibility(View.GONE);
		        remaining.setVisibility(View.GONE);
			}
			return;
		case Prefs.ViewTypes.COMPLETE:
			completed = (TextView) view.findViewById(R.id.main);
			completed.setTextSize(TypedValue.COMPLEX_UNIT_PX, completed.getTextSize() - density * 20f);
			percentage = (TextView) view.findViewById(R.id.second);
			remaining = (TextView) view.findViewById(R.id.third);
			if(Prefs.hidePercent()){
			    comma.setVisibility(View.GONE);
			}
			return;	
		case Prefs.ViewTypes.REMAINING:
			remaining = (TextView) view.findViewById(R.id.main);
			remaining.setTextSize(TypedValue.COMPLEX_UNIT_PX, remaining.getTextSize() - density * 10f);
			percentage = (TextView) view.findViewById(R.id.second);
			completed = (TextView) view.findViewById(R.id.third);
	        if(Prefs.hidePercent()){
	            comma.setVisibility(View.GONE);
	        }
			return;
		}
		
		// If hide dates, make sure that the percent is the main view
		if(Prefs.hideDate()){
		    percentage = (TextView) view.findViewById(R.id.main);
            completed = (TextView) view.findViewById(R.id.second);
            remaining = (TextView) view.findViewById(R.id.third);
		}
	}
	
	private ObjectAnimator getFragmentAnimator(){
		switch(Prefs.getMainDisplayType()){
		case Prefs.ViewTypes.REMAINING:
			return ObjectAnimator.ofInt(this, "remaining", deployment.getLength(), deployment.getRemaining());
		case Prefs.ViewTypes.COMPLETE:
			return ObjectAnimator.ofInt(this, "completed", 0, deployment.getCompleted());
		}
		//Default is to return percent
		return ObjectAnimator.ofInt(this, "percent", 0, deployment.getPercentage());
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
				getFragmentAnimator()
		);
		set.setDuration(1000);
		set.start();
	}
	
	public void setCompleted(int days){
		if(completed != null && resources != null){
			completed.setText(resources.getQuantityString(R.plurals.days_complete, days, days));
		}
	}
	
	public void setRemaining(int days){
		if(remaining != null && resources != null){
			remaining.setText(resources.getQuantityString(R.plurals.days_remaining, days, days));
		}
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
