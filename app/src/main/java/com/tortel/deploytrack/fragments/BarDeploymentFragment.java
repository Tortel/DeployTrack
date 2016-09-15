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
import com.nineoldandroids.animation.ObjectAnimator;
import com.tortel.deploytrack.Prefs;
import com.tortel.deploytrack.Prefs.ViewTypes;
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
	private static final int ANI_DURATION = 3000;
	
	private Deployment mDeployment;
	private TextView mMainView;
	private TextView mDateRangeView;
	private TextView mSecondRowView;
	private Resources mResources;
	private HorizontalStackBarChartView mBarChartView;
	private ObjectAnimator mAnimator;

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

		// Display the information
		mDateRangeView.setText(mResources.getString(R.string.date_range,
                mDeployment.getFormattedStart(), mDeployment.getFormattedEnd()));
		fillSecondView();

		// Hide or display percent
		if(Prefs.hidePercent()){
		    mMainView.setVisibility(View.GONE);
		} else {
		    mMainView.setText(mDeployment.getPercentage() + "%");
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

		return view;
	}
	
	@SuppressLint("CutPasteId")
	private void setUpTextViews(View view){
		float density = getResources().getDisplayMetrics().density;
		mDateRangeView = (TextView) view.findViewById(R.id.daterange);

        mMainView = (TextView) view.findViewById(R.id.main);
		mSecondRowView = (TextView) view.findViewById(R.id.second_row);

		// Adjust the main view text size, if needed
		switch(Prefs.getMainDisplayType()){
		case ViewTypes.PERCENT:
			if(Prefs.hidePercent()){
				mMainView.setVisibility(View.GONE);
			}
			break;
		case ViewTypes.COMPLETE:
			if(Prefs.hideDays()){
				mMainView.setVisibility(View.GONE);
			} else {
				mMainView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mMainView.getTextSize() - density * 20f);
			}
			break;
		case ViewTypes.REMAINING:
			if(Prefs.hideDays()){
				mMainView.setVisibility(View.GONE);
			} else {
				mMainView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mMainView.getTextSize() - density * 10f);
			}
			break;
		}

		// Hide the date range
        if(Prefs.hideDate()){
            mDateRangeView.setVisibility(View.GONE);
        }

        // Hide everything
        if(Prefs.hideDays() && Prefs.hidePercent()){
            mMainView.setVisibility(View.GONE);
            mSecondRowView.setVisibility(View.GONE);
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
			mBarChartView.clearAnimation();
		}
		if(mAnimator != null){
			mAnimator.cancel();
		}
	}
	
	private void animate(){
		if(mBarChartView == null){
			return;
		}

		// No animation, set the value and be done
		if(!Prefs.isAnimationEnabled()){
			// Show the bar
			mBarChartView.show();

			switch (Prefs.getMainDisplayType()){
				case ViewTypes.PERCENT:
					setMainView(mDeployment.getPercentage());
					return;
				case ViewTypes.COMPLETE:
					setMainView(mDeployment.getCompleted());
					return;
				case ViewTypes.REMAINING:
					setMainView(mDeployment.getRemaining());
					return;
			}
		}

		// Set up the animation
		switch(Prefs.getMainDisplayType()){
			case ViewTypes.COMPLETE:
				mAnimator = ObjectAnimator.ofInt(this, "mainView", 0, mDeployment.getCompleted());
				break;
			case ViewTypes.REMAINING:
				mAnimator = ObjectAnimator.ofInt(this, "mainView", mDeployment.getLength(), mDeployment.getRemaining());
				break;
			default:
				mAnimator = ObjectAnimator.ofInt(this, "mainView", 0, mDeployment.getPercentage());
				break;
		}

		mAnimator.setDuration(ANI_DURATION / 2);

		// Animate the chart
		Animation anim = new Animation()
				.setEasing(new ExpoEase())
				.setDuration(ANI_DURATION);

		mBarChartView.clearAnimation();
		mBarChartView.show(anim);
		mAnimator.start();
	}

	private void setMainView(int value){
		switch(Prefs.getMainDisplayType()){
			case ViewTypes.PERCENT:
				mMainView.setText(value+"%");
				return;
			case ViewTypes.COMPLETE:
				mMainView.setText(mResources.getQuantityString(R.plurals.days_complete, value, value));
				return;
			case ViewTypes.REMAINING:
				mMainView.setText(mResources.getQuantityString(R.plurals.days_remaining, value, value));
		}
	}

	private void fillSecondView(){
		String text = "";
		switch (Prefs.getMainDisplayType()){
			case ViewTypes.PERCENT:
				text =  mResources.getQuantityString(R.plurals.days_complete, mDeployment.getCompleted(), mDeployment.getCompleted())
						+ ", " +
						mResources.getQuantityString(R.plurals.days_remaining, mDeployment.getRemaining(), mDeployment.getRemaining());
				break;
			case ViewTypes.COMPLETE:
				if(!Prefs.hidePercent()){
					text = mDeployment.getPercentage()
							+ "%";
				}
				if(!Prefs.hideDays()) {
					if(text.length() != 0){
						text += ", ";
					}
					text += mResources.getQuantityString(R.plurals.days_remaining, mDeployment.getRemaining(), mDeployment.getRemaining());
				}
				break;
			case ViewTypes.REMAINING:
				if(!Prefs.hidePercent()){
					text = mDeployment.getPercentage()
							+ "%";
				}
				if(!Prefs.hideDays()) {
					if(text.length() != 0){
						text += ", ";
					}
					text += mResources.getQuantityString(R.plurals.days_complete, mDeployment.getCompleted(), mDeployment.getCompleted());
				}
				break;
		}

		mSecondRowView.setText(text);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(savedInstanceState != null){
			String id = savedInstanceState.getString("id");
			mDeployment = DatabaseManager.getInstance(getActivity()).getDeployment(id);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("id", mDeployment.getUuid());
	}
}
