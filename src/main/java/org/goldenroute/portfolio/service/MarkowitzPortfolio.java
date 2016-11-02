package org.goldenroute.portfolio.service;

import java.util.Arrays;

public class MarkowitzPortfolio
{
    public static final double RISK_AVERSION_INFINITESIMAL = 0.1;
    public static final double RISK_AVERSION_INFINITE = 10000;
    public static final double NONE_RISK_FREE_ASSET = 0.000001;

    private double[] weights;
    private double expectedReturn;
    private double variance;
    private double riskFree;

    public double[] getWeights()
    {
        return weights;
    }

    public double getExpectedReturn()
    {
        return expectedReturn;
    }

    public double getVariance()
    {
        return variance;
    }

    public double getStandardDeviation()
    {
        return Math.sqrt(variance);
    }

    public double getRiskFree()
    {
        return riskFree;
    }

    public double getSharpe()
    {
        if (variance != 0)
        {
            return (expectedReturn - riskFree) / Math.sqrt(variance);
        }
        else
        {
            return 0;
        }
    }

    public MarkowitzPortfolio(double[] weights, double expectedReturn, double variance, double riskFree)
    {
        this.weights = weights;
        this.expectedReturn = expectedReturn;
        this.variance = variance;
        this.riskFree = riskFree;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(expectedReturn);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(variance);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + Arrays.hashCode(weights);
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MarkowitzPortfolio other = (MarkowitzPortfolio) obj;
        if (Double.doubleToLongBits(expectedReturn) != Double.doubleToLongBits(other.expectedReturn))
            return false;
        if (Double.doubleToLongBits(variance) != Double.doubleToLongBits(other.variance))
            return false;
        if (!Arrays.equals(weights, other.weights))
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "MarkowitzPortfolio [weights=" + Arrays.toString(weights) + ", expectedReturn=" + expectedReturn
                + ", standardDeviation=" + getStandardDeviation() + ", variance=" + variance + ", riskFree=" + riskFree
                + "]";
    }
}
