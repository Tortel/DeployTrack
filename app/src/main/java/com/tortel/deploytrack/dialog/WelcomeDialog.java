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
package com.tortel.deploytrack.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.tortel.deploytrack.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Fragment that shows the welcome text
 */
public class WelcomeDialog extends DialogFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }
    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Context wrappedContext = new ContextThemeWrapper(getActivity(), R.style.Theme_DeployThemeLight);

        MaterialDialog.Builder builder = new MaterialDialog.Builder(wrappedContext);

        LayoutInflater inflater = getActivity().getLayoutInflater().cloneInContext(wrappedContext);
        View view = inflater.inflate(R.layout.dialog_about, null);
        TextView text = (TextView) view.findViewById(R.id.about_view);

        text.setText(Html.fromHtml(readRawTextFile(R.raw.welcome)));
        Linkify.addLinks(text, Linkify.ALL);
        text.setMovementMethod(LinkMovementMethod.getInstance());

        builder.customView(view, false);
        builder.title(R.string.welcome);
        builder.positiveText(R.string.close);

        return builder.build();
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
