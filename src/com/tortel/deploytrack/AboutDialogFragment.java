package com.tortel.deploytrack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class AboutDialogFragment extends SherlockDialogFragment {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.about, null);
		TextView text = (TextView) view.findViewById(R.id.about_view);
		
		text.setText(Html.fromHtml(readRawTextFile(R.raw.about)));
		Linkify.addLinks(text, Linkify.ALL);
		text.setMovementMethod(LinkMovementMethod.getInstance());
		
		builder.setView(view);
		builder.setTitle(R.string.about);
		builder.setPositiveButton(R.string.close, null);
		
		return builder.create();
	}
	
	private String readRawTextFile(int id) {
		InputStream inputStream = getActivity().getResources().openRawResource(id);
		InputStreamReader in = new InputStreamReader(inputStream);
		BufferedReader buf = new BufferedReader(in);
		String line;
		StringBuilder text = new StringBuilder();
		try {

			while ((line = buf.readLine()) != null)
				text.append(line);
		} catch (IOException e) {
			return null;
		}
		return text.toString();
	}

}
