package org.goldenroute.portfolio.alpha.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.goldenroute.Constants;
import org.goldenroute.Disposable;
import org.goldenroute.Listener;
import org.goldenroute.datafeed.QuoteFeed;
import org.goldenroute.datafeed.TimeseriesFeed;
import org.goldenroute.model.Bar;
import org.goldenroute.model.Quote;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;

import reactor.bus.EventBus;

public class AlphaExecutionPlanRuntime implements Disposable
{
    private static final String SCRIPT_FILE = "alpha.engine.ql";
    private static final String TIMESERIES_EVENT_STREAM = "TimeseriesEventStream";
    private static final String QUOTE_EVENT_STREAM = "QuoteEventStream";
    private static final String ALERT_EVENT_STREAM = "AlertEventStream";

    private static final Logger logger = Logger.getLogger(AlphaExecutionPlanRuntime.class);

    private EventBus eventBus;
    private TimeseriesFeed timeseriesFeed;
    private QuoteFeed quoteFeed;
    private SiddhiManager siddhiManager;
    private ExecutionPlanRuntime executionPlanRuntime;
    private InputHandler timeseriesInputHandler;
    private InputHandler quoteInputHandler;

    public AlphaExecutionPlanRuntime(SiddhiManager siddhiManager, EventBus eventBus, TimeseriesFeed timeseriesFeed,
            QuoteFeed quoteFeed)
    {
        this.siddhiManager = siddhiManager;
        this.eventBus = eventBus;
        this.timeseriesFeed = timeseriesFeed;
        this.quoteFeed = quoteFeed;
    }

    public void start()
    {
        String text = loadScript();

        if (text != null)
        {
            executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(text);

            setupStreamCallback();

            timeseriesInputHandler = executionPlanRuntime.getInputHandler(TIMESERIES_EVENT_STREAM);
            quoteInputHandler = executionPlanRuntime.getInputHandler(QUOTE_EVENT_STREAM);

            setupDataFeedListener();

            executionPlanRuntime.start();
        }
    }

    private void setupStreamCallback()
    {
        executionPlanRuntime.addCallback(ALERT_EVENT_STREAM, new StreamCallback()
        {
            @Override
            public void receive(Event[] events)
            {
                AlphaExecutionPlanRuntime.this.eventBus.notify(Constants.EVENT_GROUP_ALERT,
                        reactor.bus.Event.wrap(Arrays.toString(events[0].getData())));
            }
        });
    }

    private void setupDataFeedListener()
    {
        timeseriesFeed.setListener(new Listener<List<Bar>>()
        {
            @Override
            public void update(List<Bar> event)
            {
                try
                {
                    for (Bar bar : event)
                    {
                        timeseriesInputHandler.send(bar.toTuples());
                    }
                }
                catch (InterruptedException ex)
                {
                    logger.error(ex);
                }
            }

        });

        quoteFeed.setListener(new Listener<List<Quote>>()
        {
            @Override
            public void update(List<Quote> event)
            {
                try
                {
                    for (Quote quote : event)
                    {
                        quoteInputHandler.send(quote.toTuples());
                    }
                }
                catch (InterruptedException ex)
                {
                    logger.error(ex);
                }
            }

        });
    }

    private String loadScript()
    {
        try
        {
            ClassPathResource resource = new ClassPathResource(SCRIPT_FILE);
            return new String(FileCopyUtils.copyToByteArray(resource.getInputStream()));
        }
        catch (IOException e)
        {
            logger.error(e);
        }

        return null;
    }

    @Override
    public void dispose()
    {
        if (executionPlanRuntime != null)
        {
            executionPlanRuntime.shutdown();
        }
    }
}
