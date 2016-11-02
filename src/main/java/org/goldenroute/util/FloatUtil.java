package org.goldenroute.util;

public class FloatUtil
{
    public enum Precision
    {
        P0, P1, P2, P3, P4, P5, P6, P7, P8
    }

    private static final double[] precisions = new double[] { 1.0, 0.1, 0.01, 0.001, 0.0001, 0.00001, 0.000001,
            0.0000001, 0.00000001 };

    @SuppressWarnings({ "checkstyle:ParameterName" })
    public static boolean equal(double x, double y, Precision precision)
    {
        return Math.abs(x - y) <= precisions[precision.ordinal()];
    }

    @SuppressWarnings({ "checkstyle:ParameterName" })
    public static boolean equal(float x, float y, Precision precision)
    {
        return Math.abs(x - y) <= precisions[precision.ordinal()];
    }
}