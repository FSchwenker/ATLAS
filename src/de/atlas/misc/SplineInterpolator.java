package de.atlas.misc;

import de.atlas.data.SamplePoint;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

import java.util.ArrayList;

/**
 * Created by smeudt on 22.10.14.
 */
public class SplineInterpolator extends Interpolator{
    PolynomialSplineFunction function;

    public SplineInterpolator(ArrayList<SamplePoint> points){
        if(points.size()>1) {
            org.apache.commons.math3.analysis.interpolation.SplineInterpolator interpolator = new org.apache.commons.math3.analysis.interpolation.SplineInterpolator();
            double[] x = new double[points.size()];
            double[] y = new double[points.size()];
            for (int i = 0; i < points.size(); i++) {
                x[i] = points.get(i).getX();
                y[i] = points.get(i).getY();
            }
            function = interpolator.interpolate(x, y);
        }
    }

    @Override
    public double getInterpolationValueAt(double x){
            if(function!=null&&function.isValidPoint(x))return function.value(x);
            return .5;
    }

    @Override
    public boolean isValidPoint(double x) {
        return function.isValidPoint(x);
    }
}
