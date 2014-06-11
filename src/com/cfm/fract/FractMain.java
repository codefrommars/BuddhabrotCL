package com.cfm.fract;

import java.awt.Color;
import java.awt.geom.Rectangle2D;

import javax.swing.JFrame;

import org.lwjgl.LWJGLException;

import com.cfm.fract.opencl.BuddhabrotGeneratorCL;

public class FractMain {
	
	public static void main(String[] ar){
		JFrame frame = new JFrame("FractMain");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		Rectangle2D.Float region = new Rectangle2D.Float(-2.5f, -1.5f, 4, 3);
		
		float k = region.height / region.width;
		int w = 1200;
		int h = (int)( k * w );
		
		//Calc points
		long totalPix = h * w;
		float ppp = 50;
		
		int points = (int)(ppp * totalPix);
		
		FractCanvas canvas = new FractCanvas(w, h);
		
		//Generate the Buddhabroth;
		addCounter(w, h, canvas, region, points, 1000, new Color[]{ Color.black, Color.white});
		//addCounter(w, h, canvas, region, points, 1000, new Color[]{ Color.black, Color.green});
		//addCounter(w, h, canvas, region, points, 10000, new Color[]{ Color.black, Color.green});
		//addCounter(w, h, canvas, region, points, 30000, new Color[]{ Color.black, Color.red});
		//addCounter(w, h, canvas, region, points, 50000, new Color[]{ Color.black, Color.cyan});
		
		frame.add(canvas);
		
		
		frame.pack();
		frame.setVisible(true);
	}
	
	static BuddhabrotGeneratorCL gen = new BuddhabrotGeneratorCL();
	static{
		try {
			gen.initialize();
		} catch (LWJGLException e) {
			e.printStackTrace();
		}
	}
	
	public static void addCounter(int w, int h, FractCanvas canvas, Rectangle2D.Float region, int points, int bailout, Color[] palette){
		long time = System.currentTimeMillis();
		System.out.println("Generating layer with for bailout: " + bailout + " Points: " + points);
		
		FreqCounter c = new FreqCounter(w, h);
		gen.gen(region, points, c, bailout);
		canvas.addCounter(c,palette);
		
		float secs =  (System.currentTimeMillis() - time) / 1000f;
		
		System.out.println("Layer generated in " + secs + " seconds");
	}
}
