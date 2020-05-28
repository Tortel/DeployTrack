/*
 * Copyright (C) 2016-2020 Scott Warner
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
package com.tortel.deploytrack.view;

import android.content.Context;
import android.util.AttributeSet;

import com.hookedonplay.decoviewlib.DecoView;

/**
 * A {@link DecoView} that will make its self square.
 */
public class SquareDecoView extends DecoView {
    public SquareDecoView(Context context) {
        super(context);
    }

    public SquareDecoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareDecoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size;
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) {
            size = widthMeasureSpec;
        } else {
            size = heightMeasureSpec;
        }

        super.onMeasure(size, size);
    }
}
