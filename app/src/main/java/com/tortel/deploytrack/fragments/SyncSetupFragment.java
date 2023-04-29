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

import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.tortel.deploytrack.Log;
import com.tortel.deploytrack.Prefs;
import com.tortel.deploytrack.R;
import com.tortel.deploytrack.data.DatabaseManager;
import com.tortel.deploytrack.databinding.ActivitySyncSetupBinding;

/**
 * Fragment that handles setting up Firebase sync
 */
public class SyncSetupFragment extends Fragment {

    private GoogleSignInClient mSignInClient;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private FirebaseAnalytics mFirebaseAnalytics;

    private ActivitySyncSetupBinding binding;

    ActivityResultLauncher<Intent> mLoginLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // The Task returned from this call is always completed, no need to attach
                // a listener.
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                handleSignInResult(task);
            });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mSignInClient = GoogleSignIn.getClient(requireContext(), gso);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                // User is signed in
                Log.d("onAuthStateChanged:signed_in:" + user.getUid());
            } else {
                // User is signed out
                Log.d("onAuthStateChanged:signed_out");
            }
        };
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ActivitySyncSetupBinding.inflate(inflater, container, false);

        binding.toolbar.setOnMenuItemClickListener((MenuItem item) -> {
            if (item.getItemId() == android.R.id.home) {
                NavHostFragment.findNavController(this).navigateUp();
                return true;
            }
            return super.onOptionsItemSelected(item);
        });
        binding.toolbar.setNavigationOnClickListener((View v) -> {
            NavHostFragment.findNavController(this).navigateUp();
        });

        // Set up the click listener
        binding.signInButton.setOnClickListener(signInView -> {
            Intent signInIntent = mSignInClient.getSignInIntent();
            mLoginLauncher.launch(signInIntent);
        });
        binding.signInButton.setSize(SignInButton.SIZE_STANDARD);

        binding.signOutButton.setOnClickListener(signOutView -> {
            // Sign out
            mAuth.signOut();
            mSignInClient.signOut();
            // Clear the user from the database manager
            DatabaseManager.getInstance(getContext()).setFirebaseUser(null);
            // Update the UI
            showSyncStatus();
            // Un-set the preference
            Prefs.setSyncEnabled(getContext(), false);

            // Toast it
            Toast.makeText(getContext(), R.string.signed_out, Toast.LENGTH_LONG).show();
        });
        // Make the privacy policy link clickable
        binding.syncDetails.setMovementMethod(LinkMovementMethod.getInstance());

        showSyncStatus();

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            firebaseAuthWithGoogle(account);
        } catch (ApiException e) {
            Log.e("Google Sign-in failed with status code " + e.getStatusCode(), e);
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        Log.d("firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), task -> {
                    Log.d("signInWithCredential:onComplete:" + task.isSuccessful());

                    // If sign in fails, display a message to the user. If sign in succeeds
                    // the auth state listener will be notified and logic to handle the
                    // signed in user can be handled in the listener.
                    if (!task.isSuccessful()) {
                        Log.e("signInWithCredential", task.getException());
                        Toast.makeText(getContext(), "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // Log it
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, null);

                        // Set the user to start syncing
                        DatabaseManager.getInstance(getContext())
                                .setFirebaseUser(task.getResult().getUser());

                        // Let the user know
                        Toast.makeText(getContext(), getString(R.string.signed_in, acct.getEmail()),
                                Toast.LENGTH_LONG).show();

                        showSyncStatus();
                        // Record that sync is enabled
                        Prefs.setSyncEnabled(getContext(), true);
                    }
                });
    }

    /**
     * Update the mStatusTextView to reflect the current sync settings
     */
    private void showSyncStatus(){
        FirebaseUser currentUser = DatabaseManager.getInstance(getContext()).getFirebaseUser();
        if(currentUser != null){
            // If sync is enabled, show the email address it is using
            binding.syncStatus.setText(getString(R.string.pref_sync_enabled, currentUser.getEmail()));
            binding.signInButton.setVisibility(View.GONE);
            binding.signOutButton.setVisibility(View.VISIBLE);
        } else {
            binding.syncStatus.setText(R.string.pref_sync_not_enabled);
            binding.signOutButton.setVisibility(View.GONE);
            binding.signInButton.setVisibility(View.VISIBLE);
        }
    }
}
