package com.cfm.fract;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class FreqRenderer {
	
	public void render(BufferedImage img, FreqCounter counter, Color[] palette) {

		int[] rs = new int[palette.length];
		int[] gs = new int[palette.length];
		int[] bs = new int[palette.length];

		for (int i = 0; i < palette.length; i++) {
			rs[i] = palette[i].getRed();
			gs[i] = palette[i].getGreen();
			bs[i] = palette[i].getBlue();
		}

		for (int i = 0; i < counter.getWidth(); i++) {
			for (int j = 0; j < counter.getHeight(); j++) {
				float f = (float) (counter.getFreq(i, j) - counter.getMin()) / (counter.getMax() - counter.getMin());

				int r = interpolate(f, rs);
				int g = interpolate(f, gs);
				int b = interpolate(f, bs);

				//Additive.
				int argb = img.getRGB(i, j);
				r += (int) ((argb >> 16) & 0xFF );
				g += (int) ((argb >>  8) & 0xFF );
				b += (int) ((argb >>  0) & 0xFF );
				
				r = Math.min(r, 255);
				g = Math.min(g, 255);
				b = Math.min(b, 255);
				
				
				r = (r & 0xFF) << 16;
				g = (g & 0xFF) << 8;
				b = (b & 0xFF) << 0;
				
				img.setRGB(i, j, 0xFF000000 + r + g + b);
			}
		}
	}

	public int interpolate(float f, int[] values) {
		if( f > 0.999f)
			f = 0.999f;
		
		int n = values.length - 1;
		
		int c = (int) (n * f);

		
		return interpolate(n * f - c, values[c], values[c + 1]);
	}

	public int interpolate(float f, int c1, int c2) {
		return (int) (c1 + (c2 - c1) * f);
	}
}
