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
		super.onMeasure(widthMeasureSpec, widthMeasureSpec);
		
		// Make the graph mostly a filled circle
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int graphThickness = widthSize / 3;
		Log.v("Thickness: "+graphThickness);
		setThickness(graphThickness);
	}
}
