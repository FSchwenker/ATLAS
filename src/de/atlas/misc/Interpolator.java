package de.atlas.misc;

/**
 * Created by smeudt on 22.10.14.
 */
public abstract class Interpolator {
    public static final int LINEAR  = 1;
    public static final int BSPLINE = 2;
    public static final int MEAN    = 3;
    public static final int STAIRS  = 4;

    public abstract double getInterpolationValueAt(double x);


    public abstract boolean isValidPoint(double x);
}
