package de.atlas.data;

public class ClassifikationResult {

	private int resultClassID;
	private double[] probilityVector;
	
	public ClassifikationResult(int resultClassID, double[] probilityVector){
		this.probilityVector = probilityVector;
		this.resultClassID = resultClassID;
	}

	public int getResultClassID() {
		return resultClassID;
	}
	public double[] getProbilityVector() {
		return probilityVector;
	}
	public double getMaxProbability(){
		double max = -1.0*Double.MAX_VALUE;
		
		for(int i=0;i<probilityVector.length;i++){
			if(max<probilityVector[i])max=probilityVector[i];
		}
		return max;
		
	}
	
}
