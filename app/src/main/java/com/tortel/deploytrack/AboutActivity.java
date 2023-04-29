/*
 * Copyright (C) 2023 Scott Warner
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tortel.deploytrack.databinding.ActivityAboutBinding;
import com.tortel.deploytrack.databinding.ListLibraryBinding;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Check for light theme
        Prefs.load(this);
        if(Prefs.useLightTheme()){
            setTheme(R.style.Theme_DeployThemeLight);
        }
        super.onCreate(savedInstanceState);

        ActivityAboutBinding binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setOnMenuItemClickListener((MenuItem item) -> {
            if (item.getItemId() == android.R.id.home) {
                finish();
                return true;
            }
            return super.onOptionsItemSelected(item);
        });
        binding.toolbar.setNavigationOnClickListener((View v) -> {
            this.finish();
        });

        // Disable nested scrolling
        binding.libraries.setNestedScrollingEnabled(false);
        binding.libraries.setLayoutManager(new LinearLayoutManager(this));
        binding.libraries.setHasFixedSize(true);
        binding.libraries.setAdapter(new LibraryAdapter());

        binding.version.setText(getString(R.string.version, BuildConfig.VERSION_NAME));
    }

    public void itemClicked(View v) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (v.getId() == R.id.license) {
            intent.setData(Uri.parse("https://github.com/Tortel/DeployTrack/blob/master/LICENSE.txt"));
        } else if (v.getId() == R.id.source) {
            intent.setData(Uri.parse("https://github.com/Tortel/DeployTrack"));
        } else if (v.getId() == R.id.privacy) {
            intent.setData(Uri.parse("https://github.com/Tortel/DeployTrack/blob/master/PrivacyPolicy.md"));
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
            ListLibraryBinding binding =
                    ListLibraryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new LibraryViewHolder(binding);
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
            private final ListLibraryBinding binding;

            public LibraryViewHolder(@NonNull ListLibraryBinding binding) {
                super(binding.getRoot());
                this.binding = binding;

                binding.libraryRow.setOnClickListener((View v) -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(libraryURLs[position]));
                    AboutActivity.this.startActivity(intent);
                });
            }

            public void setPosition(int position) {
                this.position = position;
                binding.libraryTitle.setText(libraryNames[position]);
                binding.librarySubtitle.setText(libraryLicense[position]);
            }
        }
    }


}
