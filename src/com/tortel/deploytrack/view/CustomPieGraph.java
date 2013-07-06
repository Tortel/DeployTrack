package com.tortel.deploytrack.view;

import android.content.Context;
import android.util.AttributeSet;

import com.echo.holographlibrary.PieGraph;

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
	}
}
