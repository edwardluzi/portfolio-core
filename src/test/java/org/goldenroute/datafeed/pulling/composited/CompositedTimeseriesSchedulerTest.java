package org.goldenroute.datafeed.pulling.composited;

import java.util.Arrays;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.goldenroute.CommonUtils;
import org.goldenroute.PortfolioCoreApplication;
import org.goldenroute.model.Intervals;
import org.goldenroute.repository.TimeseriesRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(PortfolioCoreApplication.class)
public class CompositedTimeseriesSchedulerTest
{
    private static final Logger logger = Logger.getLogger(CompositedTimeseriesSchedulerTest.class);

    @Autowired
    private TimeseriesRepository timeseriesRepository;

    @Autowired
    private Environment environment;

    @Test
    public void testSchedule()
    {
        CompositedTimeseriesScheduler scheduler = new CompositedTimeseriesScheduler(timeseriesRepository, environment);

        scheduler.setWorkingHours(CommonUtils.createDefaultWorkingHours());
        scheduler.schedule(Intervals.Minute5, new HashSet<String>(Arrays.asList(new String[] { "600036", "399106" })));
        scheduler.start();

        try
        {
            Thread.sleep(1000 * 120);
        }
        catch (InterruptedException e)
        {
            logger.error(e);
        }

        scheduler.stop();
    }
}
