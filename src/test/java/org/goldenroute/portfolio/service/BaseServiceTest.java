package org.goldenroute.portfolio.service;

import java.util.List;

import javax.sql.DataSource;

import org.goldenroute.CommonUtils;
import org.goldenroute.DateTimeRange;
import org.goldenroute.datafeed.QuoteFeed;
import org.goldenroute.datafeed.TimeseriesFeed;
import org.goldenroute.portfolio.RepositoryUtils;
import org.goldenroute.portfolio.repository.AccountRepository;
import org.goldenroute.portfolio.repository.PortfolioRepository;
import org.goldenroute.portfolio.repository.ProfileRepository;
import org.goldenroute.portfolio.repository.TransactionRepository;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

public class BaseServiceTest
{
    public static final String USERNAME = "Edward";

    @Autowired
    private DataSource dataSource;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PortfolioService portfolioService;

    @Autowired
    private QuoteFeed quoteFeed;

    @Autowired
    private TimeseriesFeed timeseriesFeed;

    private RepositoryUtils repositoryUtils;

    protected PortfolioService getPortfolioService()
    {
        return portfolioService;
    }

    protected QuoteFeed getQuoteFeed()
    {
        return quoteFeed;
    }

    protected TimeseriesFeed getTimeseriesFeed()
    {
        return timeseriesFeed;
    }

    protected RepositoryUtils getRepositoryUtils()
    {
        return repositoryUtils;
    }

    @Before
    public void setUp()
    {
        repositoryUtils = repositoryUtils();

        List<DateTimeRange> workingHours = CommonUtils.createDefaultWorkingHours();

        quoteFeed.setWorkingHours(workingHours);
        timeseriesFeed.setWorkingHours(workingHours);

        quoteFeed.start();
        timeseriesFeed.start();
    }

    @After
    public void tearDown()
    {
        quoteFeed.stop();
        timeseriesFeed.stop();
    }

    protected RepositoryUtils repositoryUtils()
    {
        return new RepositoryUtils(dataSource, accountRepository, profileRepository, portfolioRepository,
                transactionRepository);
    }
}
