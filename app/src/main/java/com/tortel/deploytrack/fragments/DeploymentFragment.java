/*
 * Copyright (C) 2013-2023 Scott Warner
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
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.events.DecoEvent;
import com.tortel.deploytrack.Prefs;
import com.tortel.deploytrack.R;
import com.tortel.deploytrack.data.DatabaseManager;
import com.tortel.deploytrack.data.Deployment;
import com.tortel.deploytrack.databinding.FragmentDeploymentBinding;

/**
 * Fragment that displays the fancy deployment graph and info
 */
public class DeploymentFragment extends Fragment {
	/**
	 * Delay (In MS) before the circle starts its show animation
	 */
	private static final long ANI_SHOW_DELAY = 50;
	/**
	 * Length (In MS) of the show animation
	 */
	private static final long ANI_SHOW_DURATION = 500;
	/**
	 * Length (In MS) of the percent animation
	 */
	private static final long ANI_PERCENT_DURATION = 1000;
	
	private Deployment mDeployment;
	private SeriesItem mCompletedSeries;
	private TextView mPercentView;
	private TextView mCompletedView;
	private TextView mRemainingView;
	private int mCompletedIndex;

	private FragmentDeploymentBinding binding;

    private int mAnimatorType;

	/**
	 * Creates a new DeploymentFragment with the provided
	 * Deployment
	 */
	public static DeploymentFragment newInstance(Deployment deployment) {
		DeploymentFragment fragment = new DeploymentFragment();
		fragment.mDeployment = deployment;
		fragment.setRetainInstance(false);
		return fragment;
	}

	/**
	 * Get the deployment displayed by this fragment
	 * @return
	 */
	public Deployment getDeployment() {
		return mDeployment;
	}

	/**
	 * Get the UUID of the deployment shown, or null
	 * @return
	 */
	public String getDeploymentId() {
		return mDeployment == null ? null : mDeployment.getUuid();
	}
	
	@SuppressLint("SetTextI18n")
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState){
		binding = FragmentDeploymentBinding.inflate(inflater, container, false);

		// Check for a valid deployment object
		if (mDeployment == null) {
			// Stop now
			return binding.getRoot();
		}

        // Set up the views
        setUpTextViews();

		binding.daterange.setText(getResources().getString(R.string.date_range,
                mDeployment.getFormattedStart(), mDeployment.getFormattedEnd()));
		mCompletedView.setText(getResources().getQuantityString(R.plurals.days_complete, mDeployment.getCompleted(), mDeployment.getCompleted()));
        mRemainingView.setText(getResources().getQuantityString(R.plurals.days_remaining, mDeployment.getRemaining(), mDeployment.getRemaining()));
		
		// Hide or display percent
		if(Prefs.hidePercent()){
		    mPercentView.setVisibility(View.GONE);
		} else {
		    mPercentView.setText(mDeployment.getPercentage() + "%");
		}

		// Get the metrics
		DisplayMetrics metrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
		// Get the smaller dimension
		float size = (float) Math.min(metrics.widthPixels, metrics.heightPixels);

		SeriesItem.Builder backgroundBuilder = new SeriesItem.Builder(mDeployment.getRemainingColor())
			.setRange(0, mDeployment.getLength(), mDeployment.getLength())
			.setLineWidth(size / 6f)
			.setInitialVisibility(false);

		binding.graph.addSeries(backgroundBuilder.build());

		mCompletedSeries = new SeriesItem.Builder(mDeployment.getCompletedColor())
				.setRange(0, mDeployment.getLength(), 0)
				.setLineWidth(size / 5f)
				.setCapRounded(false)
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

		mCompletedIndex = binding.graph.addSeries(mCompletedSeries);

		return binding.getRoot();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
		mPercentView = null;
		mCompletedView = null;
		mRemainingView = null;
	}

	private void setUpTextViews(){
		float density = getResources().getDisplayMetrics().density;
		
		switch(Prefs.getMainDisplayType()){
		case Prefs.ViewTypes.PERCENT:
            mAnimatorType = Prefs.ViewTypes.PERCENT;
			mPercentView = binding.main;
			mCompletedView = binding.second;
			mRemainingView = binding.third;
			break;
		case Prefs.ViewTypes.COMPLETE:
            mAnimatorType = Prefs.ViewTypes.COMPLETE;
			mCompletedView = binding.main;
			mCompletedView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mCompletedView.getTextSize() - density * 20f);
			mPercentView = binding.second;
			mRemainingView = binding.third;
			break;
		case Prefs.ViewTypes.REMAINING:
            mAnimatorType = Prefs.ViewTypes.REMAINING;
			mRemainingView = binding.main;
			mRemainingView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mRemainingView.getTextSize() - density * 10f);
			mPercentView = binding.second;
			mCompletedView = binding.third;
			break;
		}

        if (Prefs.hideDate()) {
            binding.daterange.setVisibility(View.GONE);
        }

        // Just hide days
		if (Prefs.hideDays() && !Prefs.hidePercent()) {
            mPercentView = binding.main;
            mAnimatorType = Prefs.ViewTypes.PERCENT;

            // Make sure nothing else is set to main
            mCompletedView = binding.second;
            mRemainingView = binding.third;
            mCompletedView.setVisibility(View.GONE);
            mRemainingView.setVisibility(View.GONE);
            binding.comma.setVisibility(View.GONE);
            return;
        }

        // Just hide percent
        if (Prefs.hidePercent() && !Prefs.hideDays()) {
            mPercentView.setVisibility(View.GONE);
            if (Prefs.getMainDisplayType() != Prefs.ViewTypes.PERCENT) {
                // Hide the comma too
                binding.comma.setVisibility(View.GONE);
            }
            return;
        }

        // If both are hidden, hide it all
        if(Prefs.hideDays()) {
            binding.comma.setVisibility(View.GONE);
            binding.main.setVisibility(View.GONE);
            binding.second.setVisibility(View.GONE);
            binding.third.setVisibility(View.GONE);
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
		if (binding != null) {
			// Clear all animations
			binding.graph.executeReset();
		}
	}

	private void animate() {
		if (binding == null || mDeployment == null) {
			return;
		}

		if (!Prefs.isAnimationEnabled()) {
			binding.graph.addEvent(new DecoEvent.Builder(DecoEvent.EventType.EVENT_SHOW, true)
					.setDelay(0)
					.setDuration(0)
					.build());

			binding.graph.addEvent(new DecoEvent.Builder(mDeployment.getCompleted())
					.setIndex(mCompletedIndex)
					.setDelay(0)
					.setDuration(0)
					.build());

			setPercent(mDeployment.getPercentage());
			return;
		}
		// Remove all animation events
		binding.graph.executeReset();

		binding.graph.addEvent(new DecoEvent.Builder(DecoEvent.EventType.EVENT_SHOW, true)
				.setDelay(ANI_SHOW_DELAY)
				.setDuration(ANI_SHOW_DURATION)
				.build());

		binding.graph.addEvent(new DecoEvent.Builder(mDeployment.getCompleted())
				.setIndex(mCompletedIndex)
				.setDelay(ANI_SHOW_DELAY + ANI_SHOW_DURATION)
				.setDuration(ANI_PERCENT_DURATION)
				.build());
	}
	
	private void setCompleted(int days) {
		if (mCompletedView != null) {
			mCompletedView.setText(getResources().getQuantityString(R.plurals.days_complete, days, days));
		}
	}
	
	private void setRemaining(int days) {
		if (mRemainingView != null) {
			mRemainingView.setText(getResources().getQuantityString(R.plurals.days_remaining, days, days));
		}
	}
	
	@SuppressLint("SetTextI18n")
	private void setPercent(int percent) {
		if (mPercentView != null) {
			mPercentView.setText(percent+"%");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(savedInstanceState != null){
			String id = savedInstanceState.getString("id");
			if (id != null) {
				mDeployment = DatabaseManager.getInstance(getActivity()).getDeployment(id);
			}
		}
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mDeployment != null) {
			outState.putString("id", mDeployment.getUuid());
		}
	}
}
