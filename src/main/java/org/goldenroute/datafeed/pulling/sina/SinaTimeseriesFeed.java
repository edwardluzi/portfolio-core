package org.goldenroute.datafeed.pulling.sina;

import org.goldenroute.datafeed.pulling.PullingTimeseriesFeed;
import org.goldenroute.datafeed.pulling.TimeseriesScheduler;
import org.goldenroute.repository.TimeseriesRepository;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

public class SinaTimeseriesFeed extends PullingTimeseriesFeed implements InitializingBean, DisposableBean
{
    @Autowired
    private TimeseriesRepository timeseriesRepository;

    private SinaTimeseriesScheduler scheduler;

    public SinaTimeseriesFeed()
    {
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        scheduler = new SinaTimeseriesScheduler(timeseriesRepository);
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
