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
package com.tortel.deploytrack;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.tortel.deploytrack.data.DatabaseManager;

/**
 * Activity for setting up syncing with Firebase
 */
public class SyncSetupActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {
    private static final int RC_SIGN_IN = 321;

    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private FirebaseAnalytics mFirebaseAnalytics;
    private TextView mStatusTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Check for light theme
        Prefs.load(this);
        if(Prefs.useLightTheme()){
            setTheme(R.style.Theme_DeployThemeLight);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_setup);
        setTitle(R.string.menu_sync);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mStatusTextView = (TextView) findViewById(R.id.sync_status);
        // Set up the click listener
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);
        // Make the privacy policy link clickable
        ((TextView) findViewById(R.id.sync_details)).setMovementMethod(LinkMovementMethod.getInstance());

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

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
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        showSyncStatus();
    }

    /**
     * Update the mStatusTextView to reflect the current sync settings
     */
    private void showSyncStatus(){
        FirebaseUser currentUser = DatabaseManager.getInstance(this).getFirebaseUser();
        if(currentUser != null){
            // If sync is enabled, show the email address it is using
            mStatusTextView.setText(getString(R.string.pref_sync_enabled, currentUser.getEmail()));
        } else {
            mStatusTextView.setText(R.string.pref_sync_not_enabled);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            //Finish on the icon 'up' pressed
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d("handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            // Handle Firebase login
            firebaseAuthWithGoogle(acct);
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        Log.d("firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    Log.d("signInWithCredential:onComplete:" + task.isSuccessful());

                    // If sign in fails, display a message to the user. If sign in succeeds
                    // the auth state listener will be notified and logic to handle the
                    // signed in user can be handled in the listener.
                    if (!task.isSuccessful()) {
                        Log.e("signInWithCredential", task.getException());
                        Toast.makeText(SyncSetupActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // Log it
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, null);

                        // Set the user to start syncing
                        DatabaseManager.getInstance(getApplicationContext())
                                .setFirebaseUser(task.getResult().getUser());

                        // Let the user know
                        Toast.makeText(SyncSetupActivity.this, getString(R.string.signed_in, acct.getEmail()),
                                Toast.LENGTH_LONG).show();

                        showSyncStatus();
                        // Record that sync is enabled
                        Prefs.setSyncEnabled(getApplicationContext(), true);
                    }
                });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.sign_in_button:
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
                break;
            case R.id.sign_out_button:
                // Sign out
                mAuth.signOut();
                // Clear the user from the database manager
                DatabaseManager.getInstance(this).setFirebaseUser(null);
                // Update the UI
                showSyncStatus();
                // Un-set the preference
                Prefs.setSyncEnabled(getApplicationContext(), false);

                // Toast it
                Toast.makeText(SyncSetupActivity.this, R.string.signed_out, Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("Google API connection failed");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mGoogleApiClient != null){
            mGoogleApiClient.disconnect();
        }
    }
}
