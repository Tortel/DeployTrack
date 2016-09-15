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
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.events.DecoEvent;
import com.tortel.deploytrack.Log;
import com.tortel.deploytrack.Prefs;
import com.tortel.deploytrack.R;
import com.tortel.deploytrack.data.DatabaseManager;
import com.tortel.deploytrack.data.Deployment;

/**
 * Fragment that displays the fancy deployment graph and info
 */
public class DeploymentFragment extends Fragment {
	/**
	 * Delay (In MS) before the circle starts its show animation
	 */
	private static final long ANI_SHOW_DELAY = 200;
	/**
	 * Length (In MS) of the show animation
	 */
	private static final long ANI_SHOW_DURATION = 500;
	/**
	 * Length (In MS) of the percent animation
	 */
	private static final long ANI_PERCENT_DURATION = 1000;
	
	private Deployment mDeployment;
	private TextView mPercentView;
	private TextView mCompletedView;
	private TextView mDateRangeView;
	private TextView mRemainingView;
	private Resources mResources;
	private DecoView mArcView;
	private SeriesItem mCompletedSeries;
	private int mCompletedIndex;

    private int mAnimatorType;

	/**
	 * Creates a new DeploymentFragment with the provided
	 * Deployment
	 * @param deployment
	 * @return
	 */
	public static DeploymentFragment newInstance(Deployment deployment){
		DeploymentFragment fragment = new DeploymentFragment();
		fragment.mDeployment = deployment;
		return fragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState){
		mResources = getActivity().getResources();
		
		View view = inflater.inflate(R.layout.fragment_deployment, container, false);

        // Set up the views
        setUpTextViews(view);

		mDateRangeView.setText(mResources.getString(R.string.date_range,
                mDeployment.getFormattedStart(), mDeployment.getFormattedEnd()));
		mCompletedView.setText(mResources.getQuantityString(R.plurals.days_complete, mDeployment.getCompleted(), mDeployment.getCompleted()));
        mRemainingView.setText(mResources.getQuantityString(R.plurals.days_remaining, mDeployment.getRemaining(), mDeployment.getRemaining()));
		
		// Hide or display percent
		if(Prefs.hidePercent()){
		    mPercentView.setVisibility(View.GONE);
		} else {
		    mPercentView.setText(mDeployment.getPercentage() + "%");
		}
		
		// Fill the graph
		mArcView = (DecoView) view.findViewById(R.id.graph);

		// Get the metrics
		DisplayMetrics metrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
		// Get the smaller dimension
		float size = (float) Math.min(metrics.widthPixels, metrics.heightPixels);

		SeriesItem.Builder backgroundBuilder = new SeriesItem.Builder(mDeployment.getRemainingColor())
			.setRange(0, mDeployment.getLength(), mDeployment.getLength())
			.setLineWidth(size / 6f)
			.setInitialVisibility(false);

		mArcView.addSeries(backgroundBuilder.build());

		mCompletedSeries = new SeriesItem.Builder(mDeployment.getCompletedColor())
				.setRange(0, mDeployment.getLength(), 0)
				.setLineWidth(size / 5f)
				.setInitialVisibility(false)
				.build();

		// Set up the listener to animate everything else
		mCompletedSeries.addArcSeriesItemListener(new SeriesItem.SeriesItemListener() {
			@Override
			public void onSeriesItemAnimationProgress(float percentComplete, float currentPosition) {
				float progress =  ((currentPosition - mCompletedSeries.getMinValue()) / (mCompletedSeries.getMaxValue() - mCompletedSeries.getMinValue()));
				switch (mAnimatorType) {
					case Prefs.ViewTypes.REMAINING:
						int remaining = mDeployment.getLength() - (int) (progress * mCompletedSeries.getMaxValue());
						setRemaining(remaining);
						break;
					case Prefs.ViewTypes.COMPLETE:
						int completed = (int) (progress * mCompletedSeries.getMaxValue());
						setCompleted(completed);
						break;
					default:
						int percent = (int) (progress * 100);
						setPercent(percent);
						break;
				}
			}

			@Override
			public void onSeriesItemDisplayProgress(float percentComplete) {
				// Do nothing
			}
		});

		mCompletedIndex = mArcView.addSeries(mCompletedSeries);

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
            mAnimatorType = Prefs.ViewTypes.PERCENT;
			mPercentView = main;
			mCompletedView = second;
			mRemainingView = third;
			break;
		case Prefs.ViewTypes.COMPLETE:
            mAnimatorType = Prefs.ViewTypes.COMPLETE;
			mCompletedView = main;
			mCompletedView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mCompletedView.getTextSize() - density * 20f);
			mPercentView = second;
			mRemainingView = third;
			break;
		case Prefs.ViewTypes.REMAINING:
            mAnimatorType = Prefs.ViewTypes.REMAINING;
			mRemainingView = main;
			mRemainingView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mRemainingView.getTextSize() - density * 10f);
			mPercentView = second;
			mCompletedView = third;
			break;
		}

        if(Prefs.hideDate()){
            mDateRangeView.setVisibility(View.GONE);
        }

        // Just hide days
		if(Prefs.hideDays() && !Prefs.hidePercent()){
            mPercentView = main;
            mAnimatorType = Prefs.ViewTypes.PERCENT;

            // Make sure nothing else is set to main
            mCompletedView = second;
            mRemainingView = second;
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
		if(mArcView != null){
			// Clear all animations
			mArcView.executeReset();
		}
	}
	
	private void animate(){
		if(mArcView == null){
			return;
		}

		if(!Prefs.isAnimationEnabled()){
			mArcView.addEvent(new DecoEvent.Builder(DecoEvent.EventType.EVENT_SHOW, true)
					.setDelay(0)
					.setDuration(0)
					.build());

			mArcView.addEvent(new DecoEvent.Builder(mDeployment.getCompleted())
					.setIndex(mCompletedIndex)
					.setDelay(0)
					.setDuration(0)
					.build());

			setPercent(mDeployment.getPercentage());
			return;
		}
		// Remove all animation events
		mArcView.executeReset();

		mArcView.addEvent(new DecoEvent.Builder(DecoEvent.EventType.EVENT_SHOW, true)
				.setDelay(ANI_SHOW_DELAY)
				.setDuration(ANI_SHOW_DURATION)
				.build());

		mArcView.addEvent(new DecoEvent.Builder(mDeployment.getCompleted())
				.setIndex(mCompletedIndex)
				.setDelay(ANI_SHOW_DELAY + ANI_SHOW_DURATION)
				.setDuration(ANI_PERCENT_DURATION)
				.build());
	}
	
	private void setCompleted(int days){
		if(mCompletedView != null && mResources != null){
			mCompletedView.setText(mResources.getQuantityString(R.plurals.days_complete, days, days));
		}
	}
	
	private void setRemaining(int days){
		if(mRemainingView != null && mResources != null){
			mRemainingView.setText(mResources.getQuantityString(R.plurals.days_remaining, days, days));
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
