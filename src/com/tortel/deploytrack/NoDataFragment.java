package com.tortel.deploytrack;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * A fragment that is shown if there is no other data
 */
public class NoDataFragment extends SherlockFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		TextView view = new TextView(getActivity());
		view.setText(getActivity().getResources().getString(R.string.howto_add));
		view.setGravity(Gravity.CENTER);
		return view;
	}
	


}
