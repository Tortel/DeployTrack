/*
 * Copyright (C) 2020 Scott Warner
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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Check for light theme
        Prefs.load(this);
        if(Prefs.useLightTheme()){
            setTheme(R.style.Theme_DeployThemeLight);
        }
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about);

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setOnMenuItemClickListener((MenuItem item) -> {
            if (item.getItemId() == android.R.id.home) {
                finish();
                return true;
            }
            return super.onOptionsItemSelected(item);
        });
        toolbar.setNavigationOnClickListener((View v) -> {
            this.finish();
        });

        RecyclerView recyclerView = findViewById(R.id.libraries);
        // Disable nested scrolling
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new LibraryAdapter());

        TextView versionView = findViewById(R.id.version);
        versionView.setText(getString(R.string.version, BuildConfig.VERSION_NAME));
    }

    public void itemClicked(View v) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        switch (v.getId()) {
            case R.id.license:
                intent.setData(Uri.parse("https://github.com/Tortel/DeployTrack/blob/master/LICENSE.txt"));
                break;
            case R.id.source:
                intent.setData(Uri.parse("https://github.com/Tortel/DeployTrack"));
                break;
            case R.id.privacy:
                intent.setData(Uri.parse("https://github.com/Tortel/DeployTrack/blob/master/PrivacyPolicy.md"));
                break;
        }
        startActivity(intent);
    }

    private class LibraryAdapter extends RecyclerView.Adapter<LibraryAdapter.LibraryViewHolder> {
        final String[] libraryNames = {
                "AndroidX Support Libraries",
                "Material Components for Android",
                "DecoView-Charting",
                "Material DateTime Picker",
                "Holo ColorPicker",
                "JodaTime",
                "OrmLite",
                "Firebase Android SDK"
        };
        final String[] libraryURLs = {
                "https://developer.android.com/jetpack/androidx",
                "https://material.io/develop/android/",
                "https://github.com/bmarrdev/android-DecoView-charting",
                "https://github.com/wdullaer/MaterialDateTimePicker",
                "https://github.com/LarsWerkman/HoloColorPicker",
                "http://www.joda.org/joda-time/",
                "https://ormlite.com/",
                "https://firebase.google.com/",
        };
        final String[] libraryLicense = {
                "Apache 2",
                "Apache 2",
                "Apache 2",
                "Apache 2",
                "Apache 2",
                "Apache 2",
                "ISC License",
                "Apache 2",
        };


        @NonNull
        @Override
        public LibraryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_library, parent, false);
            return new LibraryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull LibraryViewHolder holder, int position) {
            holder.setPosition(position);
        }

        @Override
        public int getItemCount() {
            return libraryNames.length;
        }

        public class LibraryViewHolder extends RecyclerView.ViewHolder {
            int position = -1;
            TextView mTitle;
            TextView mSubtitle;

            public LibraryViewHolder(@NonNull View itemView) {
                super(itemView);

                mTitle = itemView.findViewById(R.id.library_title);
                mSubtitle = itemView.findViewById(R.id.library_subtitle);

                itemView.findViewById(R.id.library_row).setOnClickListener((View v) -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(libraryURLs[position]));
                    AboutActivity.this.startActivity(intent);
                });
            }

            public void setPosition(int position) {
                this.position = position;
                mTitle.setText(libraryNames[position]);
                mSubtitle.setText(libraryLicense[position]);
            }
        }
    }


}
