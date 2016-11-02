package org.goldenroute.datafeed.pulling.composited;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.goldenroute.datafeed.pulling.FetchingJob;
import org.goldenroute.datafeed.pulling.TimeseriesScheduler;
import org.goldenroute.datafeed.pulling.sina.SinaTimeseriesRequestor;
import org.goldenroute.datafeed.pulling.yahoo.YahooTimeseriesRequestor;
import org.goldenroute.model.Bar;
import org.goldenroute.model.Intervals;
import org.goldenroute.repository.TimeseriesRepository;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.core.env.Environment;

public class CompositedTimeseriesScheduler extends TimeseriesScheduler
{
    private static final Logger logger = Logger.getLogger(CompositedTimeseriesScheduler.class);

    private SinaTimeseriesRequestor sinaRequestor;
    private YahooTimeseriesRequestor yahooRequestor;

    public CompositedTimeseriesScheduler(TimeseriesRepository timeseriesRepository, Environment environment)
    {
        sinaRequestor = new SinaTimeseriesRequestor(this, timeseriesRepository);
        yahooRequestor = new YahooTimeseriesRequestor(this, timeseriesRepository, environment);
    }

    @Override
    protected void execJob(JobExecutionContext context)
    {
        JobDataMap jobDataMap = context.getMergedJobDataMap();
        Intervals intervals = Intervals.values()[jobDataMap.getInt(INTERVALS)];

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                doRequest(intervals);
            }
        }).start();
    }

    protected void doRequest(Intervals intervals)
    {
        if (intervals.ordinal() <= Intervals.Daily.ordinal() && intervals != Intervals.Minute)
        {
            request(intervals, getSymbolTable().get(intervals));

            if (afterMarketClose(intervals.toString()))
            {
                notifyEndOfStream(intervals);
            }
        }
        else
        {
            logger.debug("Unsupported request: intervals=" + intervals.toString());
        }
    }

    @Override
    public Map<String, List<Bar>> request(Intervals intervals, Collection<String> symbols)
    {
        if (intervals.ordinal() <= Intervals.Hour.ordinal())
        {
            return sinaRequestor.request(intervals, symbols);
        }
        else if (intervals == Intervals.Daily)
        {
            return yahooRequestor.request(intervals, symbols);
        }
        else
        {
            throw new IllegalArgumentException("intervals");
        }
    }

    @Override
    protected int getDelaySeconds(String category)
    {
        if ("Daily".equals(category))
        {
            return 3600 * 5;
        }
        else
        {
            return 30;
        }
    }

    @Override
    protected Class<? extends Job> getJobClass()
    {
        return CompositedTimeseriesFetchJob.class;
    }

    @Override
    protected String getContextName()
    {
        return CompositedTimeseriesScheduler.class.getSimpleName() + ".THIS";
    }

    public static class CompositedTimeseriesFetchJob extends FetchingJob
    {
        @Override
        public String getContextName()
        {
            return CompositedTimeseriesScheduler.class.getSimpleName() + ".THIS";
        }
    }
}
