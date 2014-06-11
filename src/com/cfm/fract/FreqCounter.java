package com.cfm.fract;


public class FreqCounter {
	private int w, h;
	private long counters[][];
	private long total;

	private long min, max;

	public FreqCounter(int w, int h) {
		super();
		this.w = w;
		this.h = h;

		counters = new long[w][h];
	}

	public void addFreq(int x, int y) {
		counters[x][y]++;
		total++;

		max = Math.max(counters[x][y], max);
		min = Math.min(counters[x][y], min);
	}

	public void setFreq(int x, int y, long counter) {
		total -= counters[x][y];
		counters[x][y] = counter;
		total += counter;

		max = Math.max(counter, max);
		min = Math.min(counter, min);
	}

	public long getFreq(int x, int y) {
		return counters[x][y];
	}

	public long getTotal() {
		return total;
	}

	public int getWidth() {
		return w;
	}

	public int getHeight() {
		return h;
	}

	public void reset() {
		for (int i = 0; i < w; i++)
			for (int j = 0; j < h; j++)
				counters[i][j] = 0;

		total = 0;
	}

	public long getMax() {
		return max;
	}

	public long getMin() {
		return min;
	}


}
