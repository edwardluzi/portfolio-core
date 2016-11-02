package org.goldenroute.portfolio.alpha;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.goldenroute.Constants;
import org.goldenroute.Listener;
import org.goldenroute.PortfolioCoreApplication;
import org.goldenroute.datafeed.QuoteFeed;
import org.goldenroute.datafeed.TimeseriesFeed;
import org.goldenroute.model.Bar;
import org.goldenroute.model.Intervals;
import org.goldenroute.model.Quote;
import org.goldenroute.util.CalendarUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import reactor.bus.EventBus;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(PortfolioCoreApplication.class)
public class AlphaEngineTest
{
    private static final String SYMBOL1 = "SYMBOL1";
    private static final String SYMBOL2 = "SYMBOL2";

    @Autowired
    private EventBus eventBus;

    @Mock
    private TimeseriesFeed timeseriesFeed;

    @Mock
    private QuoteFeed quoteFeed;

    @InjectMocks
    @Autowired
    private AlphaEngine engine;

    private Listener<List<Bar>> timeseriesRuntime;
    private Listener<List<Quote>> quoteRuntime;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testTimeseries()
    {
        doAnswer(new Answer<Void>()
        {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable
            {
                timeseriesRuntime = invocation.getArgumentAt(0, Listener.class);
                return null;
            }
        }).when(timeseriesFeed).setListener(any(Listener.class));

        doAnswer(new Answer<Void>()
        {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable
            {
                List<Bar> bars = new ArrayList<>();

                Calendar today = CalendarUtil.normalize(Calendar.getInstance(), Calendar.DAY_OF_MONTH);

                today.set(2007, 3, 21);

                bars.add(new Bar(SYMBOL1, Intervals.Daily, today.getTimeInMillis(), 1.2, 1.3, 1.4, 1.5, 100));
                bars.add(new Bar(SYMBOL2, Intervals.Daily, today.getTimeInMillis(), 2.2, 2.3, 2.4, 2.5, 200));

                timeseriesRuntime.update(bars);

                timeseriesRuntime.update(Arrays.asList(new Bar[] { new Bar(Constants.EVENT_ID_TIMERSERIES_EOS,
                        Intervals.Daily, today.getTimeInMillis(), 0, 0, 0, 0, 0) }));

                return null;
            }
        }).when(timeseriesFeed).start();

        engine.start();

        try
        {
            Thread.sleep(10000);
        }
        catch (InterruptedException e)
        {
            engine.stop();
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testQuote()
    {
        doAnswer(new Answer<Void>()
        {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable
            {
                quoteRuntime = invocation.getArgumentAt(0, Listener.class);
                return null;
            }
        }).when(quoteFeed).setListener(any(Listener.class));

        doAnswer(new Answer<Void>()
        {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable
            {
                List<Quote> bars = new ArrayList<>();

                Calendar today = CalendarUtil.normalize(Calendar.getInstance(), Calendar.DAY_OF_MONTH);

                today.set(2007, 3, 21);

                bars.add(new Quote(SYMBOL1, today.getTimeInMillis(), 1.2, 100));
                bars.add(new Quote(SYMBOL2, today.getTimeInMillis(), 2.2, 200));

                quoteRuntime.update(bars);

                quoteRuntime.update(Arrays.asList(new Quote[] { new Quote(Constants.EVENT_ID_QUOTE_EOS + "T", today
                        .getTimeInMillis(), 0, 0) }));

                return null;
            }
        }).when(quoteFeed).start();

        engine.start();

        try
        {
            Thread.sleep(10000);
        }
        catch (InterruptedException e)
        {
            engine.stop();
        }
    }
}
