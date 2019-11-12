package de.atlas.misc;

import de.atlas.data.SamplePoint;

import java.util.ArrayList;

/**
 * Created by smeudt on 22.10.14.
 */
public class StairInterpolator extends Interpolator{

    private ArrayList<SamplePoint> points;
    public StairInterpolator(ArrayList<SamplePoint> points){
        this.points = points;
    }

    @Override
    public double getInterpolationValueAt(double x){
        for(int i=0;i<points.size()-1;i++){
            if(x>=points.get(i).getX()&&x<points.get(i+1).getX()){
                return points.get(i).getY();
            }
        }
        return .5;
    }

    @Override
    public boolean isValidPoint(double x) {
        if(x>=points.get(0).getX()&&x<=points.get(points.size()-1).getX())return true;
        return false;
    }
}
