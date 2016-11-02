package org.goldenroute.portfolio.repository;

import javax.sql.DataSource;

import org.goldenroute.portfolio.RepositoryUtils;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

public class BaseRepositoryTest
{
    @Autowired
    private DataSource dataSource;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PortfolioRepository portfolioRepository;

    private RepositoryUtils repositoryUtils;

    protected DataSource getDataSource()
    {
        return dataSource;
    }

    protected AccountRepository getAccountRepository()
    {
        return accountRepository;
    }

    protected ProfileRepository getProfileRepository()
    {
        return profileRepository;
    }

    protected PortfolioRepository getPortfolioRepository()
    {
        return portfolioRepository;
    }

    protected TransactionRepository getTransactionRepository()
    {
        return transactionRepository;
    }

    protected RepositoryUtils getRepositoryUtils()
    {
        return repositoryUtils;
    }

    @Before
    public void setUp()
    {
        this.repositoryUtils = repositoryUtils();
    }

    protected RepositoryUtils repositoryUtils()
    {
        return new RepositoryUtils(dataSource, accountRepository, profileRepository, portfolioRepository,
                transactionRepository);
    }
}
