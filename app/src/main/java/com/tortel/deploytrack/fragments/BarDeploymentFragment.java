/*
 * Copyright (C) 2013-2015 Scott Warner
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

import com.db.chart.Tools;
import com.db.chart.model.BarSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.HorizontalStackBarChartView;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.ExpoEase;
import com.tortel.deploytrack.Prefs;
import com.tortel.deploytrack.R;
import com.tortel.deploytrack.data.DatabaseManager;
import com.tortel.deploytrack.data.Deployment;

/**
 * Fragment that displays the fancy deployment graph and info
 * with a bar graph
 */
public class BarDeploymentFragment extends Fragment {
	/**
	 * Length (In MS) of the animation
	 */
	private static final int ANI_DURATION = 1000;
	
	private Deployment mDeployment;
	private TextView mPercentView;
	private TextView mDateRangeView;
	private TextView mSecondRowView;
	private Resources mResources;
	private HorizontalStackBarChartView mBarChartView;

	/**
	 * Creates a new DeploymentFragment with the provided
	 * Deployment
	 * @param deployment
	 * @return
	 */
	public static BarDeploymentFragment newInstance(Deployment deployment){
		BarDeploymentFragment fragment = new BarDeploymentFragment();
		fragment.mDeployment = deployment;
		return fragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState){
		mResources = getActivity().getResources();
		
		View view = inflater.inflate(R.layout.fragment_bar_deployment, container, false);

        // Set up the views
        setUpTextViews(view);

		mDateRangeView.setText(mResources.getString(R.string.date_range,
                mDeployment.getFormattedStart(), mDeployment.getFormattedEnd()));
		//mCompletedView.setText(mResources.getQuantityString(R.plurals.days_complete, mDeployment.getCompleted(), mDeployment.getCompleted()));

		// Hide or display percent
		if(Prefs.hidePercent()){
		    mPercentView.setVisibility(View.GONE);
		} else {
		    mPercentView.setText(mDeployment.getPercentage() + "%");
		}
		
		//Fill the graph
		mBarChartView = (HorizontalStackBarChartView) view.findViewById(R.id.graph);
        mBarChartView.setRoundCorners(Tools.fromDpToPx(15));
        mBarChartView.setSetSpacing(Tools.fromDpToPx(10));
        mBarChartView.setBorderSpacing(Tools.fromDpToPx(20))
                .setYLabels(AxisController.LabelPosition.NONE)
                .setXLabels(AxisController.LabelPosition.NONE)
                .setXAxis(false)
                .setYAxis(false);

        String labels[] = {""};
        float values[][] = {
                {mDeployment.getCompleted()},
                {mDeployment.getRemaining()}
        };

        BarSet completedBarSet = new BarSet(labels, values[0]);
        completedBarSet.setColor(mDeployment.getCompletedColor());
        BarSet remainingBarSet = new BarSet(labels, values[1]);
        remainingBarSet.setColor(mDeployment.getRemainingColor());

        mBarChartView.addData(completedBarSet);
        mBarChartView.addData(remainingBarSet);

        Animation anim = new Animation()
                .setEasing(new ExpoEase())
                .setDuration(3000);

        mBarChartView.show(anim);

		return view;
	}
	
	@SuppressLint("CutPasteId")
	private void setUpTextViews(View view){
		float density = getResources().getDisplayMetrics().density;
		TextView commaView = (TextView) view.findViewById(R.id.comma);
		mDateRangeView = (TextView) view.findViewById(R.id.daterange);

        TextView main = (TextView) view.findViewById(R.id.main);
        TextView second = (TextView) view.findViewById(R.id.second);
        TextView third = (TextView) view.findViewById(R.id.third);
		
		switch(Prefs.getMainDisplayType()){
		case Prefs.ViewTypes.PERCENT:
			mPercentView = main;
			break;
		case Prefs.ViewTypes.COMPLETE:
			mPercentView = second;
			break;
		case Prefs.ViewTypes.REMAINING:
			mPercentView = second;
			break;
		}

        if(Prefs.hideDate()){
            mDateRangeView.setVisibility(View.GONE);
        }

        // Just hide days
		if(Prefs.hideDays() && !Prefs.hidePercent()){
            mPercentView = main;

            // Make sure nothing else is set to main
            second.setVisibility(View.GONE);
            third.setVisibility(View.GONE);
            commaView.setVisibility(View.GONE);
            return;
        }

        // Just hide percent
        if(Prefs.hidePercent() && !Prefs.hideDays()){
            mPercentView.setVisibility(View.GONE);
            if(Prefs.getMainDisplayType() != Prefs.ViewTypes.PERCENT){
                // Hide the comma too
                commaView.setVisibility(View.GONE);
            }
            return;
        }

        // If both are hidden, hide it all
        if(Prefs.hideDays() && Prefs.hidePercent()){
            commaView.setVisibility(View.GONE);
            main.setVisibility(View.GONE);
            second.setVisibility(View.GONE);
            third.setVisibility(View.GONE);
        }
	}
	
	@Override
	public void onResume(){
		super.onResume();
		animate();
	}
	
	@Override
	public void onPause(){
		super.onPause();
		if(mBarChartView != null){
			// Clear all animations
		}
	}
	
	private void animate(){
		if(mBarChartView == null || !Prefs.isAnimationEnabled()){
			setPercent(mDeployment.getPercentage());
		}
	}
	
	private void setPercent(int percent){
		if(mPercentView != null){
			mPercentView.setText(percent+"%");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(savedInstanceState != null){
			int id = savedInstanceState.getInt("id");
			mDeployment = DatabaseManager.getInstance(getActivity()).getDeployment(id);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("id", mDeployment.getId());
	}
}
