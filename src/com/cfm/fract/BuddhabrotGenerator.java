package com.cfm.fract;

import java.awt.geom.Rectangle2D;

public class BuddhabrotGenerator {
	
	static class XY{
		public float x, y;
	}
	
	public void gen(Rectangle2D.Float region, long points, FreqCounter counter, int bailout){
		counter.reset();
		
		float x = region.x;
		float y = region.y;
		
		XY seq[] = new XY[bailout];
		for(int i = 0; i < bailout; i++)
			seq[i] = new XY();
		
		for( long tt= 0; tt < points; tt++){
				
			// Random point in the image range...
			x = region.x + region.width * (float) Math.random();
			y = region.y + region.height * (float) Math.random();

			// Determine state of the point
			int seqLen = iteratePoint(x, y, seq, bailout);
			
			
			for (int i = 0; i < seqLen; i++) {
				int ix = (int) (((seq[i].x - region.x) * counter.getWidth() / region.width));
				int iy = (int) (((seq[i].y - region.y) * counter.getHeight() / region.height));
				
				if (ix >= 0 && iy >= 0 && ix < counter.getWidth() && iy < counter.getHeight()) 
					counter.addFreq(ix, iy);
			}
			
		}
	}
	
	private int iteratePoint(float x0, float y0,  XY seq[], int bailout ){
		
		float x = 0, y = 0, nx, ny;
		
		for(int i = 0; i < bailout; i++){
			nx = x*x  - y*y + x0;
			ny = 2 * x * y + y0;
			
			seq[i].x = nx;
			seq[i].y = ny;
		
			if( nx * nx + ny*ny > 10 )
				return i;
			
			x = nx;
			y = ny;
		}
		
		return 0;
	}
}
