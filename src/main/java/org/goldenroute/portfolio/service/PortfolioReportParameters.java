package org.goldenroute.portfolio.service;

public class PortfolioReportParameters
{
    private String intervals;
    private int sampleCount;
    private double riskFree;
    private double riskAversion;
    private boolean shortingAllowed;

    public PortfolioReportParameters()
    {
    }

    public PortfolioReportParameters(String intervals, int sampleCount, double riskFree, double riskAversion,
            boolean shortingAllowed)
    {
        this.intervals = intervals;
        this.sampleCount = sampleCount;
        this.riskFree = riskFree;
        this.riskAversion = riskAversion;
        this.shortingAllowed = shortingAllowed;

    }

    public String getIntervals()
    {
        return intervals;
    }

    public void setIntervals(String intervals)
    {
        this.intervals = intervals;
    }

    public int getSampleCount()
    {
        return sampleCount;
    }

    public void setSampleCount(int sampleCount)
    {
        this.sampleCount = sampleCount;
    }

    public double getRiskFree()
    {
        return riskFree;
    }

    public void setRiskFree(double riskFree)
    {
        this.riskFree = riskFree;
    }

    public double getRiskAversion()
    {
        return riskAversion;
    }

    public void setRiskAversion(double riskAversion)
    {
        this.riskAversion = riskAversion;
    }

    public boolean isShortingAllowed()
    {
        return shortingAllowed;
    }

    public void setShortingAllowed(boolean shortingAllowed)
    {
        this.shortingAllowed = shortingAllowed;
    }

    @Override
    public String toString()
    {
        return "PortfolioReportParameters [intervals=" + intervals + ", sampleCount=" + sampleCount + ", riskFree="
                + riskFree + ", riskAversion=" + riskAversion + ", shortingAllowed=" + shortingAllowed + "]";
    }
}
