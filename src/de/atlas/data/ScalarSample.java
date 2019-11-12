package de.atlas.data;

public class ScalarSample implements Comparable {
	private double val;
	private double time;
	
	public ScalarSample(){
		
	}
	public ScalarSample(double val, double time){
		this.val=val;
		this.time=time;
	}
	public double getVal() {
		return val;
	}
	public void setVal(double val) {
		this.val = val;
	}
	public double getTime() {
		return time;
	}
	public void setTime(double time) {
		this.time = time;
	}


	@Override
	public int compareTo(Object o) {
		return Double.compare(this.time,((ScalarSample)o).getTime());
	}
}
