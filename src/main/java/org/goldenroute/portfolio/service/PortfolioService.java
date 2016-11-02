package org.goldenroute.portfolio.service;

import java.util.List;

import org.goldenroute.portfolio.model.Portfolio;

public interface PortfolioService
{
    Portfolio findOne(Long id, boolean addingValue);

    void save(Portfolio portfolio);

    List<String> findDistinctSymbols();

    Portfolio addValue(Portfolio portfolio);

    PortfolioReport generateReport(Portfolio portfolio, PortfolioReportParameters parameters);
}
