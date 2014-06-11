package com.cfm.fract;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

public class FractCanvas extends JComponent{
	
	private List<FreqCounter> counters = new ArrayList<>();
	private List<Color[]> pals = new ArrayList<>();
	
	private BufferedImage img;
	private FreqRenderer renderer = new FreqRenderer();
	
	public FractCanvas(int w, int h){
		setPreferredSize(new Dimension(w, h));
		img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
	}
	
	public void addCounter(FreqCounter c, Color[] palette){
		counters.add(c);
		pals.add(palette);
	}
	
	public void paint(Graphics g) {
		super.paint(g);
		
		//Reset img
		for (int i = 0; i < img.getWidth(); i++) 
			for (int j = 0; j < img.getHeight(); j++) 
				img.setRGB(i, j, 0xFF000000);
		
		for(int i = 0; i < counters.size(); i++)
			renderer.render(img, counters.get(i), pals.get(i));
		
		g.drawImage(img, 0, 0, this);
	}
	
}
