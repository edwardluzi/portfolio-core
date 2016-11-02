package org.goldenroute.datafeed.pulling.yahoo;

import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.goldenroute.PortfolioCoreApplication;
import org.goldenroute.model.Bar;
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
public class YahooTimeseriesSchedulerTest
{
    private static final Logger logger = Logger.getLogger(YahooTimeseriesSchedulerTest.class);

    @Autowired
    private TimeseriesRepository timeseriesRepository;

    @Autowired
    private Environment environment;

    @Test
    public void testRequest()
    {
        YahooTimeseriesScheduler scheduler = new YahooTimeseriesScheduler(timeseriesRepository, environment);

        Map<String, List<Bar>> map = scheduler.request(Intervals.Daily,
                Arrays.asList(new String[] { "000651", "600036" }));

        assertNotNull(map);
        assert (map.size() > 0);

        for (List<Bar> bars : map.values())
        {
            for (Bar bar : bars)
            {
                logger.debug(bar.toString());
            }
        }
    }
}
