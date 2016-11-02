package org.goldenroute.portfolio.repository;

import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.util.Calendar;

import org.goldenroute.PortfolioCoreApplication;
import org.goldenroute.portfolio.model.Account;
import org.goldenroute.portfolio.model.AssetClass;
import org.goldenroute.portfolio.model.Currency;
import org.goldenroute.portfolio.model.Portfolio;
import org.goldenroute.portfolio.model.Transaction;
import org.goldenroute.util.CalendarUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@Configuration
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(PortfolioCoreApplication.class)
public class PortfolioRepositoryTest extends BaseRepositoryTest
{
    private Account testAccount;

    @Before
    @Override
    public void setUp()
    {
        super.setUp();

        testAccount = this.getRepositoryUtils().createAccount("Edward");
    }

    @After
    public void tearDown()
    {
        if (testAccount != null)
        {
            this.getRepositoryUtils().deleteAccount(testAccount);
        }
    }

    @Test
    public void testAddTransactions()
    {
        assertNotNull(getPortfolioRepository());

        Portfolio portfolio = getRepositoryUtils().createPortfolio(testAccount, "P01", "000001", Currency.CNY,
                AssetClass.Equity, getClass().toString());

        Calendar calendar = CalendarUtil.normalize(Calendar.getInstance(), Calendar.DAY_OF_MONTH);

        getRepositoryUtils().createTransaction(portfolio, calendar.getTimeInMillis(), "000651",
                Transaction.Type.Buying, BigDecimal.valueOf(18.00), BigDecimal.valueOf(1000), BigDecimal.valueOf(12),
                BigDecimal.valueOf(34));

        Portfolio fetched = getPortfolioRepository().findOne(portfolio.getId());

        assert (fetched.getTransactions().size() > 0);
    }
}
