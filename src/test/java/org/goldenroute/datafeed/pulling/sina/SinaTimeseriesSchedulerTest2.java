package org.goldenroute.datafeed.pulling.sina;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.goldenroute.CommonUtils;
import org.goldenroute.PortfolioCoreApplication;
import org.goldenroute.model.Bar;
import org.goldenroute.model.Intervals;
import org.goldenroute.repository.TimeseriesRepository;
import org.goldenroute.util.CalendarUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(PortfolioCoreApplication.class)
public class SinaTimeseriesSchedulerTest2
{
    private static final Logger logger = Logger.getLogger(SinaTimeseriesSchedulerTest.class);

    @Mock
    private TimeseriesRepository timeseriesRepository;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testRequest()
    {
        doAnswer(new Answer<List<Bar>>()
        {
            @Override
            public List<Bar> answer(InvocationOnMock invocation) throws Throwable
            {
                List<Bar> bars = new ArrayList<>();

                String symbol = invocation.getArgumentAt(0, String.class);
                Intervals intervals = invocation.getArgumentAt(1, Intervals.class);

                Calendar calendar = Calendar.getInstance();

                if (intervals == Intervals.Minute)
                {
                    calendar.set(Calendar.HOUR_OF_DAY, 1);
                    calendar.set(Calendar.MINUTE, 50);

                    CalendarUtil.normalize(calendar, Calendar.MINUTE);

                    bars.add(new Bar(symbol, intervals, calendar.getTimeInMillis(), 1, 2, 3, 4, 5));

                    calendar.add(Calendar.MINUTE, -1);

                    bars.add(new Bar(symbol, intervals, calendar.getTimeInMillis(), 2, 3, 4, 5, 6));
                }
                else if (intervals == Intervals.Hour)
                {
                    calendar.set(Calendar.HOUR_OF_DAY, 3);
                    calendar.set(Calendar.MINUTE, 30);

                    CalendarUtil.normalize(calendar, Calendar.MINUTE);

                    bars.add(new Bar(symbol, intervals, calendar.getTimeInMillis(), 1, 2, 3, 4, 5));

                    calendar.add(Calendar.HOUR, -1);

                    bars.add(new Bar(symbol, intervals, calendar.getTimeInMillis(), 2, 3, 4, 5, 6));
                }

                return bars;
            }
        }).when(timeseriesRepository).findBySymbolAndIntervalsOrderByTimestampDesc(any(String.class),
                any(Intervals.class), any(Pageable.class));

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
