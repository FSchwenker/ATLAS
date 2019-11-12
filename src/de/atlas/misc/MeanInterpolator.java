package de.atlas.misc;

import de.atlas.exceptions.FilterSizeException;
import de.atlas.data.SamplePoint;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

import java.util.ArrayList;

/**
 * Created by smeudt on 22.10.14.
 */
public class MeanInterpolator extends Interpolator{

    private ArrayList<SamplePoint> points;
    private double[] sampled;
    public MeanInterpolator(ArrayList<SamplePoint> points){
        this.points = points;
        if(points.size()>1) {
            org.apache.commons.math3.analysis.interpolation.LinearInterpolator interpolator = new org.apache.commons.math3.analysis.interpolation.LinearInterpolator();
            double[] x = new double[points.size()];
            double[] y = new double[points.size()];
            for (int i = 0; i < points.size(); i++) {
                x[i] = points.get(i).getX();
                y[i] = points.get(i).getY();
            }
            PolynomialSplineFunction function = interpolator.interpolate(x, y);
            sampled = new double[(int)(points.get(points.size()-1).getX()+1)];

            for (int i=0;i<sampled.length;i++){
                sampled[i]=function.value(i);
            }
            int fs = (sampled.length/points.size()/3)*2+1;
            try {
                sampled = HelperFunctions.signalFilter(sampled, fs, HelperFunctions.filterType.AVERAGE);
            } catch (FilterSizeException e) {
                e.printStackTrace();
            }
        }



    }

    @Override
    public double getInterpolationValueAt(double x){
        if(isValidPoint(x))return sampled[(int)x];
        return .5;
    }

    @Override
    public boolean isValidPoint(double x) {
        if(x>=points.get(0).getX()&&x<=points.get(points.size()-1).getX())return true;
        return false;    }
}
