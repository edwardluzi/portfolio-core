package org.goldenroute.datafeed.pulling.sina;

import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.goldenroute.CommonUtils;
import org.goldenroute.PortfolioCoreApplication;
import org.goldenroute.model.Bar;
import org.goldenroute.model.Intervals;
import org.goldenroute.repository.TimeseriesRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(PortfolioCoreApplication.class)
public class SinaTimeseriesSchedulerTest
{
    private static final Logger logger = Logger.getLogger(SinaTimeseriesSchedulerTest.class);

    @Autowired
    private TimeseriesRepository timeseriesRepository;

    @Test
    public void testRequest()
    {
        SinaTimeseriesScheduler scheduler = new SinaTimeseriesScheduler(timeseriesRepository);

        scheduler.setWorkingHours(CommonUtils.createDefaultWorkingHours());

        Map<String, List<Bar>> map = scheduler.request(Intervals.Minute,
                Arrays.asList(new String[] { "000538", "600036" }));

        assertNotNull(map);
        assert (map.size() > 0);

        for (List<Bar> bars : map.values())
        {
            for (Bar bar : bars)
            {
                logger.debug(bar.toString());
            }
        }

        map = scheduler.request(Intervals.Hour, Arrays.asList(new String[] { "000538", "600036" }));

        assertNotNull(map);

        for (List<Bar> bars : map.values())
        {
            for (Bar bar : bars)
            {
                logger.debug(bar.toString());
            }
        }
    }
}
