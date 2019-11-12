package de.atlas.data;

import java.util.ArrayList;

public class VectorSample {
	private ArrayList<Double> val;
	private long time;
	
	public VectorSample(){
		
	}
	public VectorSample(ArrayList<Double> val, long time){
		this.val=val;
		this.time=time;
	}
	public ArrayList<Double> getVal() {
		return val;
	}
	public void setVal(ArrayList<Double> val) {
		this.val = val;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public int getDimension(){
		if(val!=null){
			return val.size();
		}else{
			return -1;
		}
	}
	

}
