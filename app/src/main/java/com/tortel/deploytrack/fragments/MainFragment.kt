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
package com.tortel.deploytrack.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels

import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.NavHostFragment

import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth

import com.tortel.deploytrack.Analytics
import com.tortel.deploytrack.DeploymentFragmentAdapter
import com.tortel.deploytrack.Log
import com.tortel.deploytrack.Prefs
import com.tortel.deploytrack.R
import com.tortel.deploytrack.data.DatabaseManager
import com.tortel.deploytrack.data.RoomMigrationManager
import com.tortel.deploytrack.databinding.FragmentMainBinding
import com.tortel.deploytrack.dialog.DatabaseUpgradeDialog
import com.tortel.deploytrack.dialog.DeleteDialog
import com.tortel.deploytrack.dialog.ScreenShotModeDialog
import com.tortel.deploytrack.dialog.WelcomeDialog
import com.tortel.deploytrack.model.MainFragmentModel
import com.tortel.deploytrack.provider.WidgetProvider

/**
 * Main fragment that displays the charts and such
 */
class MainFragment : Fragment() {
    private val viewModel: MainFragmentModel by viewModels()
    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    private var mAdapter: DeploymentFragmentAdapter? = null
    private var mCurrentPosition = 0
    private var mScreenShotMode = false
    private var binding: FragmentMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())

        val lbm = LocalBroadcastManager.getInstance(requireContext())
        lbm.registerReceiver(mChangeListener, IntentFilter(DatabaseManager.DATA_DELETED))
        lbm.registerReceiver(mChangeListener, IntentFilter(DatabaseManager.DATA_ADDED))
        lbm.registerReceiver(mChangeListener, IntentFilter(DatabaseManager.DATA_CHANGED))

        if (savedInstanceState != null) {
            mCurrentPosition = savedInstanceState.getInt(KEY_POSITION)
            mScreenShotMode = savedInstanceState.getBoolean(KEY_SCREENSHOT, false)
            if (mScreenShotMode) {
                Prefs.setScreenShotMode(true, context)
            }
        } else {

            // Check if we need to upgrade the database
            if (RoomMigrationManager.needsMigration(requireContext())) {
                val upgradeDialog = DatabaseUpgradeDialog()
                upgradeDialog.show(parentFragmentManager, "upgrade")
            } else {
                // Only show the welcome dialog if its the first time the app is opened,
                // and the DB doesn't need to be upgraded
                if (!Prefs.isWelcomeShown()) {
                    Prefs.setWelcomeShown(requireContext())
                    val dialog = WelcomeDialog()
                    dialog.show(parentFragmentManager, "welcome")
                }
            }
            mCurrentPosition = 0
            // Sync should only need to be set up once
            setupSync()
        }
    }

    override fun onPause() {
        super.onPause()
        binding?.tabs?.removeOnTabSelectedListener(mTabSelectedListener)
    }

    override fun onResume() {
        super.onResume()
        Prefs.load(context)
        if (mScreenShotMode) {
            Prefs.setScreenShotMode(true, context)
        }
        reload()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_POSITION, mCurrentPosition)
        outState.putBoolean(KEY_SCREENSHOT, mScreenShotMode)
    }

    override fun onDestroy() {
        super.onDestroy()
        val lbm = LocalBroadcastManager.getInstance(requireContext())
        lbm.unregisterReceiver(mChangeListener)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        binding!!.toolbar.setOnMenuItemClickListener { item: MenuItem ->
            val intent: Intent
            val mDeploymentId = mAdapter!!.getId(mCurrentPosition)
            val itemId = item.itemId
            when (itemId) {
                R.id.menu_create_new -> {
                    NavHostFragment.findNavController(this)
                            .navigate(MainFragmentDirections.mainToCreateAction())
                    return@setOnMenuItemClickListener true
                }
                R.id.menu_edit -> {
                    // If its the info fragment, ignore
                    if (mDeploymentId == null) {
                        return@setOnMenuItemClickListener true
                    }
                    val editAction = MainFragmentDirections.mainToCreateAction()
                    editAction.id = mDeploymentId
                    NavHostFragment.findNavController(this).navigate(editAction)
                    return@setOnMenuItemClickListener true
                }
                R.id.menu_delete -> {
                    // If its the info fragment, ignore
                    if (mDeploymentId == null) {
                        return@setOnMenuItemClickListener true
                    }
                    val dialog = DeleteDialog()
                    val args = Bundle()
                    args.putString(DeleteDialog.KEY_ID, mDeploymentId)
                    dialog.arguments = args
                    dialog.show(parentFragmentManager, "delete")
                    return@setOnMenuItemClickListener true
                }
                R.id.menu_feedback -> {
                    intent = Intent(Intent.ACTION_SEND)
                    intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("Swarner.dev@gmail.com"))
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Deployment Tracker Feedback")
                    intent.type = "plain/text"
                    if (isAvailable(intent)) {
                        startActivity(intent)
                    }
                    return@setOnMenuItemClickListener true
                }
                R.id.menu_screenshot -> {
                    if (!Prefs.isAboutScreenShotShown()) {
                        val aboutDialog = ScreenShotModeDialog()
                        aboutDialog.show(parentFragmentManager, "screenshot")
                        Prefs.setAboutScreenShotShown(context)
                    }
                    mScreenShotMode = !mScreenShotMode
                    Prefs.setScreenShotMode(mScreenShotMode, context)

                    // Propagate screen shot mode to the widgets
                    val updateWidgetIntent = Intent(WidgetProvider.UPDATE_INTENT)
                    updateWidgetIntent.putExtra(WidgetProvider.KEY_SCREENSHOT_MODE, mScreenShotMode)
                    requireActivity().sendBroadcast(updateWidgetIntent)
                    reload()
                    return@setOnMenuItemClickListener true
                }
                R.id.menu_settings -> {
                    NavHostFragment.findNavController(this)
                            .navigate(MainFragmentDirections.mainToSettingsAction())
                    return@setOnMenuItemClickListener true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }
        return binding!!.getRoot()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun reload() {
        Log.v("Reloading data")
        if (mAdapter == null) {
            mAdapter = DeploymentFragmentAdapter(activity, childFragmentManager)
        }
        mAdapter?.reloadData()

        // Make sure that the position does not go past the end
        if (mCurrentPosition >= mAdapter!!.count) {
            mCurrentPosition = 0.coerceAtLeast(mCurrentPosition - 1)
        }

        // Re-set the adapter and position
        binding?.pager?.adapter = mAdapter
        binding?.pager?.currentItem = mCurrentPosition
        binding?.tabs?.setupWithViewPager(binding?.pager)
        binding?.tabs?.addOnTabSelectedListener(mTabSelectedListener)
        if (mScreenShotMode) {
            binding?.tabs?.visibility = View.INVISIBLE
        } else {
            binding?.tabs?.visibility = View.VISIBLE
        }

        // Set the analytics properties
        setAnalyticsProperties()
    }

    /**
     * If the user is logged in, make sure that sync is set up
     */
    private fun setupSync() {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            DatabaseManager.getInstance(requireContext()).firebaseUser = auth.currentUser
        } else if (Prefs.isSyncEnabled(context)) {
            // If sync is/was enabled, and no account was found, let the user know
            Snackbar.make(binding!!.root, R.string.sync_account_error, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.menu_sync) { _: View? ->
                        NavHostFragment.findNavController(this)
                                .navigate(MainFragmentDirections.mainToSyncAction())
                    }.show()
        }
    }

    /**
     * Set the various analytics properties
     */
    private fun setAnalyticsProperties() {
        // Record the number of deployments
        if (mAdapter != null) {
            mFirebaseAnalytics!!.setUserProperty(Analytics.PROPERTY_DEPLOYMENT_COUNT, "" + mAdapter!!.count)
        }
        Analytics.recordPreferences(mFirebaseAnalytics, mScreenShotMode)
    }

    /**
     * Check if there is an app available to handle an intent
     */
    private fun isAvailable(intent: Intent): Boolean {
        val mgr = requireContext().packageManager
        val list = mgr.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY)
        return list.size > 0
    }

    /**
     * Class to listen for page changes.
     * The page number is used for editing and deleting data
     */
    private val mTabSelectedListener: OnTabSelectedListener = object : OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab) {
            mCurrentPosition = tab.position
            Log.v("Page changed to $mCurrentPosition")
        }

        override fun onTabUnselected(tab: TabLayout.Tab) {
            // Ignore
        }

        override fun onTabReselected(tab: TabLayout.Tab) {
            // Ignore
        }
    }
    private val mChangeListener: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == DatabaseManager.DATA_DELETED) {
                mAdapter?.deploymentDeleted(intent.getStringExtra(DeleteDialog.KEY_ID))
            }
            reload()
        }
    }

    companion object {
        private const val KEY_POSITION = "position"
        private const val KEY_SCREENSHOT = "screenshot"
    }
}