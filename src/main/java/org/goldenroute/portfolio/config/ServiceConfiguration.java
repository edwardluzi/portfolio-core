package org.goldenroute.portfolio.config;

import org.goldenroute.portfolio.service.AccountService;
import org.goldenroute.portfolio.service.DatabaseInitializer;
import org.goldenroute.portfolio.service.PortfolioService;
import org.goldenroute.portfolio.service.ProfileService;
import org.goldenroute.portfolio.service.TransactionService;
import org.goldenroute.portfolio.service.impl.AccountServiceImpl;
import org.goldenroute.portfolio.service.impl.DatabaseInitializerImpl;
import org.goldenroute.portfolio.service.impl.PortfolioServiceImpl;
import org.goldenroute.portfolio.service.impl.ProfileServiceImpl;
import org.goldenroute.portfolio.service.impl.TransactionServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfiguration
{
    @Bean
    public AccountService accountService()
    {
        return new AccountServiceImpl();
    }

    @Bean
    public PortfolioService portfolioService()
    {
        return new PortfolioServiceImpl();
    }

    @Bean
    public TransactionService transactionService()
    {
        return new TransactionServiceImpl();
    }

    @Bean
    public ProfileService profileService()
    {
        return new ProfileServiceImpl();
    }

    @Bean
    public DatabaseInitializer databaseInitializer()
    {
        return new DatabaseInitializerImpl();
    }
}
