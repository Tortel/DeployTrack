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
package com.tortel.deploytrack;

import org.joda.time.DateTime;
import org.joda.time.Days;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.echo.holographlibrary.PieGraph;
import com.echo.holographlibrary.PieSlice;
import com.tortel.deploytrack.data.DatabaseManager;
import com.tortel.deploytrack.data.Deployment;

/**
 * Fragment that displays the fancy deployment graph and info
 */
public class DeploymentFragment extends SherlockFragment {
	private Deployment deployment;
	
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
		
		//Name
		TextView name = (TextView) view.findViewById(R.id.name);
		name.setText(deployment.getName());
		
		//Date range
		TextView dateRange = (TextView) view.findViewById(R.id.daterange);
		dateRange.setText(deployment.getFormattedStart()
				+resources.getString(R.string.to)
				+deployment.getFormattedEnd());
		
		//Figure out the percentage
		DateTime now = new DateTime();
		int days = Days.daysBetween(deployment.getStart(), deployment.getEnd()).getDays();
		int percent = 0;
		int completed = 0;
		
		//Check if its started
		if(now.compareTo(deployment.getStart()) > 0){
			completed = Days.daysBetween(deployment.getStart(), now).getDays();
			percent = (int) ((double) completed / (double) days * 100);
		}
		
		//Extra check for completed events
		if(now.compareTo(deployment.getEnd()) >= 0) {
			completed = days;
			percent = 100;
		}
		
		//Days completed, days left
		TextView stats = (TextView) view.findViewById(R.id.time_stats);
		stats.setText(completed+resources.getString(R.string.days_complete)+
				(days - completed)+resources.getString(R.string.days_left));
		
		//Percentage
		TextView percentage = (TextView) view.findViewById(R.id.percentage);
		percentage.setText(percent+"%");
		
		//Fill the graph
		PieGraph pie = (PieGraph) view.findViewById(R.id.graph);
		
		PieSlice completedSlice = new PieSlice();
		completedSlice.setColor(deployment.getCompletedColor());
		completedSlice.setValue(completed);
		pie.addSlice(completedSlice);
		PieSlice togoSlice = new PieSlice();
		togoSlice.setColor(deployment.getRemainingColor());
		togoSlice.setValue(days - completed);
		pie.addSlice(togoSlice);
		
		return view;
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
