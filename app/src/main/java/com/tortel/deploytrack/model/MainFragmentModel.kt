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
package com.tortel.deploytrack.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tortel.deploytrack.data.DatabaseManager
import com.tortel.deploytrack.data.Deployment
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class MainFragmentModel : ViewModel() {
    @Inject lateinit var mDatabaseManager: DatabaseManager;

    private var mCurrentPosition = 0

    val currentPosition: Int
        get() = mCurrentPosition

    private var mScreenShotMode = false

    val screenShotMode: Boolean
        get() = mScreenShotMode

    private var mDeploymentList : MutableLiveData<ArrayList<Deployment>> = MutableLiveData();
    val deploymentList: LiveData<ArrayList<Deployment>>
        get() = mDeploymentList

    suspend fun loadData() {
        var deployments: List<Deployment>
        withContext(Dispatchers.IO) {
            deployments = mDatabaseManager.allDeployments
        }
        withContext(Dispatchers.Main) {
            mDeploymentList.postValue(ArrayList<Deployment>(deployments))
        }
    }

}
