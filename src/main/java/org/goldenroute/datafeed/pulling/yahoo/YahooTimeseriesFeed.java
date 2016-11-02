package org.goldenroute.datafeed.pulling.yahoo;

import org.goldenroute.datafeed.pulling.PullingTimeseriesFeed;
import org.goldenroute.datafeed.pulling.TimeseriesScheduler;
import org.goldenroute.repository.TimeseriesRepository;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

public class YahooTimeseriesFeed extends PullingTimeseriesFeed implements InitializingBean, DisposableBean
{
    @Autowired
    private TimeseriesRepository timeseriesRepository;

    @Autowired
    private Environment environment;

    private YahooTimeseriesScheduler scheduler;

    public YahooTimeseriesFeed()
    {
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        scheduler = new YahooTimeseriesScheduler(timeseriesRepository, environment);
    }

    @Override
    public void destroy() throws Exception
    {
        stop();
    }

    @Override
    protected TimeseriesScheduler getScheduler()
    {
        return scheduler;
    }

    @Override
    protected TimeseriesRepository getTimeseriesRepository()
    {
        return timeseriesRepository;
    }
}
