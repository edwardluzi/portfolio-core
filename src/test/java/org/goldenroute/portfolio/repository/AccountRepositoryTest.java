package org.goldenroute.portfolio.repository;

import static org.junit.Assert.assertNotNull;

import org.goldenroute.PortfolioCoreApplication;
import org.goldenroute.portfolio.model.Account;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@Configuration
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(PortfolioCoreApplication.class)
public class AccountRepositoryTest extends BaseRepositoryTest
{
    @Test
    public void createUserAndProfile()
    {
        assertNotNull(getDataSource());
        assertNotNull(getAccountRepository());
        assertNotNull(getProfileRepository());

        Account account = getRepositoryUtils().createAccount("Edward");

        assertNotNull(account.getId());
    }

    @After
    public void tearDown()
    {
        getRepositoryUtils().deleteAccount("Edward");
    }
}
