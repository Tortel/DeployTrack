/*
 * Copyright (C) 2013 Scott Warner
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

import com.echo.holographlibrary.PieGraph;
import com.tortel.deploytrack.Log;

/**
 * Customized PieGraph that will be square, based on the provided width.
 */
public class CustomPieGraph extends PieGraph {

	public CustomPieGraph(Context context) {
		super(context);
	}
	
    public CustomPieGraph(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int size;
		if(MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY){
			size = widthMeasureSpec;
		} else {
			size = heightMeasureSpec;
		}
		
		super.onMeasure(size, size);
		
		// Make the graph mostly a filled circle
		int widthSize = MeasureSpec.getSize(size);
		int graphThickness = widthSize / 3;
		Log.v("Thickness: "+graphThickness);
		setThickness(graphThickness);
	}
}
