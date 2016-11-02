package org.goldenroute.datafeed.pulling.composited;

import org.goldenroute.datafeed.pulling.PullingTimeseriesFeed;
import org.goldenroute.datafeed.pulling.TimeseriesScheduler;
import org.goldenroute.repository.TimeseriesRepository;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

public class CompositedTimeseriesFeed extends PullingTimeseriesFeed implements InitializingBean, DisposableBean
{
    @Autowired
    private TimeseriesRepository timeseriesRepository;

    @Autowired
    private Environment environment;

    private CompositedTimeseriesScheduler scheduler;

    public CompositedTimeseriesFeed()
    {
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        scheduler = new CompositedTimeseriesScheduler(timeseriesRepository, environment);
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
