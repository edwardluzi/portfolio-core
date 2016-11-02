package org.goldenroute.datafeed.pulling.yahoo;

import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.goldenroute.PortfolioCoreApplication;
import org.goldenroute.model.Quote;
import org.goldenroute.repository.QuoteRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(PortfolioCoreApplication.class)
public class YahooQuoteSchedulerTest
{
    private static final Logger logger = Logger.getLogger(YahooQuoteSchedulerTest.class);

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private Environment environment;

    @Test
    public void testRequest()
    {
        YahooQuoteScheduler scheduler = new YahooQuoteScheduler(quoteRepository, environment);
        List<Quote> quotes = scheduler.request(Arrays.asList(new String[] { "000651", "399106" }));
        assertNotNull(quotes);

        for (Quote quote : quotes)
        {
            logger.debug(quote.toString());
        }
    }
}
