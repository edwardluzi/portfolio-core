package org.goldenroute.portfolio.service;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@Configuration
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(PortfolioCoreApplication.class)
public class PortfolioServiceTest extends BaseServiceTest
{
    private Account testAccount;
    private Portfolio testPortfolio;

    @Autowired
    private PortfolioService portfolioService;

    @Before
    @Override
    public void setUp()
    {
        super.setUp();

        testAccount = getRepositoryUtils().createAccount(USERNAME);
        testPortfolio = getRepositoryUtils().createPortfolio(testAccount, "P01", "000001", Currency.CNY,
                AssetClass.Equity, getClass().toString());
    }

    @After
    @Override
    public void tearDown()
    {
        super.tearDown();

        Account temp = getRepositoryUtils().findAccount(USERNAME);

        if (temp != null)
        {
            getRepositoryUtils().deleteAccount(temp);
        }
    }

    @Test
    public void testFindOne()
    {
        Calendar calendar = CalendarUtil.normalize(Calendar.getInstance(), Calendar.DAY_OF_MONTH);

        Transaction transaction1 = getRepositoryUtils().createTransaction(testPortfolio, calendar.getTimeInMillis(),
                "000651", Transaction.Type.Buying, BigDecimal.valueOf(18.00), BigDecimal.valueOf(1000),
                BigDecimal.valueOf(12), BigDecimal.valueOf(34));

        Portfolio portfolio = getPortfolioService().findOne(testPortfolio.getId(), true);

        assertNotNull(portfolio);
        assertNotNull(portfolio.getHoldings());

        getRepositoryUtils().deleteTransaction(testPortfolio, transaction1);
    }

    @Test
    public void testReport()
    {
        Portfolio portfolio = portfolioService.findOne(33L, true);
        assertNotNull(portfolio);

        PortfolioReportParameters parameters = new PortfolioReportParameters("D", 100, getRiskfree(),
                MarkowitzPortfolio.RISK_AVERSION_INFINITE, false);

        PortfolioReport report = portfolioService.generateReport(portfolio, parameters);
        assertNotNull(report);
    }

    private double getRiskfree()
    {
        return Math.pow(1.0305, 1.0 / 360) - 1;
    }
}
