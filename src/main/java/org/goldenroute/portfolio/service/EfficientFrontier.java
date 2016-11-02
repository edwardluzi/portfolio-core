package org.goldenroute.portfolio.service;

import java.util.List;

public class EfficientFrontier
{
    private List<MarkowitzPortfolio> frontiers;
    private MarkowitzPortfolio globalMinimumVariance;

    public EfficientFrontier(List<MarkowitzPortfolio> frontiers, MarkowitzPortfolio globalMinimumVariance)
    {
        this.frontiers = frontiers;
        this.globalMinimumVariance = globalMinimumVariance;
    }

    public List<MarkowitzPortfolio> getfrontiers()
    {
        return frontiers;
    }

    public MarkowitzPortfolio getGlobalMinimumVariance()
    {
        return globalMinimumVariance;
    }
}
