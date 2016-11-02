package org.goldenroute.datafeed.pulling.sina;

import org.goldenroute.datafeed.pulling.PullingQuoteFeed;
import org.goldenroute.datafeed.pulling.RealtimeScheduler;
import org.goldenroute.repository.QuoteRepository;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

public class SinaQuoteFeed extends PullingQuoteFeed implements InitializingBean, DisposableBean
{
    @Autowired
    private QuoteRepository quoteRepository;

    private SinaQuoteScheduler scheduler;

    public SinaQuoteFeed()
    {
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        scheduler = new SinaQuoteScheduler(quoteRepository);
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
