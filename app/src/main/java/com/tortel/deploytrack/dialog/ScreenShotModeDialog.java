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
package com.tortel.deploytrack.dialog;

import androidx.annotation.RawRes;
import androidx.annotation.StringRes;

import com.tortel.deploytrack.R;

/**
 * Fragment that shows the welcome text
 */
public class ScreenShotModeDialog extends AboutDialog {

    protected @StringRes int getTitleString(){
        return R.string.about_screenshot;
    }

    protected @RawRes int getContent(){
        return R.raw.screenshot_mode;
    }

}
