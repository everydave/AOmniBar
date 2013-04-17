package com.omni;

public class Lapse {
	
	private long lapse;
	private long init;
	
	public void spring() {
		init = System.currentTimeMillis();
	}
	
	public boolean fire(long time) {
		return (time - init) >= lapse;
	}
	
	public Lapse(long lapse) {
		this.lapse = lapse;
	}

}
