/*
 * Created by Daniel Nadeau
 *         daniel.nadeau01@gmail.com
 *         danielnadeau.blogspot.com
 * 
 * Portions Copyright (C) 2013-2014 Scott Warner
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

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Path.Direction;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.echo.holographlibrary.PieSlice;

/**
 * This is the full PieGraph class from the HoloGraphLibrary, modified for 
 * animation and to be square based on the size of the fixed dimension.
 */
public class CustomPieGraph extends View {
    private static final float PADDING = 0.5f;
    
	private ArrayList<PieSlice> slices = new ArrayList<PieSlice>();
	private Paint paint = new Paint();
	private Path path = new Path();
	
	private int indexSelected = -1;
	private int thickness = 50;
	private OnSliceClickedListener listener;
	private float percent = 1;
	
	// Drawing Variables
	private Path p = new Path();
	private RectF rect = new RectF();
	private Region region = new Region();
	
    private float currentAngle = 270;
    private float currentSweep = 0;
    private float percentSweep = 0;
    private int totalValue = 0;
    
    private float midX, midY, radius, innerRadius;
	
	
	public CustomPieGraph(Context context) {
		super(context);
	}
	public CustomPieGraph(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void setPercent(float x){
		if(x >= 100){
			percent = 1;
		} else if(x <= 0){
			percent = 0;
		} else {
			percent = x / 100f;
		}
		invalidate();
	}
	
	public void onDraw(Canvas canvas) {
		canvas.drawColor(Color.TRANSPARENT);
		paint.reset();
		paint.setAntiAlias(true);
		path.reset();
		
		//Reset variables
	    currentAngle = 270;
	    currentSweep = 0;
	    percentSweep = 0;
	    totalValue = 0;
		
		midX = getWidth()/2;
		midY = getHeight()/2;
		if (midX < midY){
			radius = midX;
		} else {
			radius = midY;
		}
		radius -= PADDING;
		innerRadius = radius - thickness;
		
		for (PieSlice slice : slices){
			totalValue += slice.getValue();
		}
		
		int count = 0;
		for (PieSlice slice : slices){
			p.reset();
			paint.setColor(slice.getColor());
			currentSweep = (slice.getValue()/totalValue)*(360);
			percentSweep = currentSweep * percent;
			rect.set(midX-radius, midY-radius, midX+radius, midY+radius);
			p.arcTo(rect, currentAngle+PADDING, percentSweep - PADDING);
			
			rect.set(midX-innerRadius, midY-innerRadius, midX+innerRadius, midY+innerRadius);
			p.arcTo(rect, (currentAngle+PADDING) + (percentSweep - PADDING), -(percentSweep-PADDING));
			p.close();
			
			slice.setPath(p);
			
			region.set((int)(midX-radius), (int)(midY-radius), (int)(midX+radius), (int)(midY+radius));
			slice.setRegion(region);
			canvas.drawPath(p, paint);
			
			if (indexSelected == count && listener != null){
				path.reset();
				paint.setColor(slice.getColor());
				paint.setColor(Color.parseColor("#33B5E5"));
				paint.setAlpha(100);
				
				if (slices.size() > 1) {
				    rect.set(midX-radius-(PADDING*2), midY-radius-(PADDING*2), midX+radius+(PADDING*2), midY+radius+(PADDING*2));
					path.arcTo(rect, currentAngle, percentSweep+PADDING);
					
					rect.set(midX-innerRadius+(PADDING*2), midY-innerRadius+(PADDING*2), midX+innerRadius-(PADDING*2), midY+innerRadius-(PADDING*2));
					path.arcTo(rect, currentAngle + percentSweep + PADDING, -(currentSweep + PADDING));
					path.close();
				} else {
					path.addCircle(midX, midY, radius+PADDING, Direction.CW);
				}
				
				canvas.drawPath(path, paint);
				paint.setAlpha(255);
			}
			
			currentAngle = currentAngle+currentSweep;
			
			count++;
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
	    Point point = new Point();
	    point.x = (int) event.getX();
	    point.y = (int) event.getY();
	    
	    int count = 0;
	    for (PieSlice slice : slices){
	    	Region r = new Region();
	    	r.setPath(slice.getPath(), slice.getRegion());
	    	if (r.contains((int)point.x,(int) point.y) && event.getAction() == MotionEvent.ACTION_DOWN){
	    		indexSelected = count;
	    	} else if (event.getAction() == MotionEvent.ACTION_UP){
	    		if (r.contains((int)point.x,(int) point.y) && listener != null){
	    			if (indexSelected > -1){
		    			listener.onClick(indexSelected);
	    			}
	    			indexSelected = -1;
	    		}
	    		
	    	}
		    count++;
	    }
	    
	    if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_UP){
	    	postInvalidate();
	    }
	    
	    return true;
	}
	
	public ArrayList<PieSlice> getSlices() {
		return slices;
	}
	public void setSlices(ArrayList<PieSlice> slices) {
		this.slices = slices;
		postInvalidate();
	}
	public PieSlice getSlice(int index) {
		return slices.get(index);
	}
	public void addSlice(PieSlice slice) {
		this.slices.add(slice);
		postInvalidate();
	}
	public void setOnSliceClickedListener(OnSliceClickedListener listener) {
		this.listener = listener;
	}
	
	public int getThickness() {
		return thickness;
	}
	public void setThickness(int thickness) {
		this.thickness = thickness;
		postInvalidate();
	}
	
	public void removeSlices(){
		for (int i = slices.size()-1; i >= 0; i--){
			slices.remove(i);
		}
		postInvalidate();
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
		setThickness(graphThickness);
	}

	public static interface OnSliceClickedListener {
		public abstract void onClick(int index);
	}
}
