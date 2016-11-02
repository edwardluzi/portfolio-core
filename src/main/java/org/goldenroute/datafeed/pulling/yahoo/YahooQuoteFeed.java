package org.goldenroute.datafeed.pulling.yahoo;

import org.goldenroute.datafeed.pulling.PullingQuoteFeed;
import org.goldenroute.datafeed.pulling.RealtimeScheduler;
import org.goldenroute.repository.QuoteRepository;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

public class YahooQuoteFeed extends PullingQuoteFeed implements InitializingBean, DisposableBean
{
    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private Environment environment;

    private YahooQuoteScheduler scheduler;

    public YahooQuoteFeed()
    {
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        scheduler = new YahooQuoteScheduler(quoteRepository, environment);
    }

    @Override
    public void destroy() throws Exception
    {
        stop();
    }

    @Override
    protected RealtimeScheduler getScheduler()
    {
        return scheduler;
    }

    @Override
    protected QuoteRepository getQuoteRepository()
    {
        return quoteRepository;
    }
}
