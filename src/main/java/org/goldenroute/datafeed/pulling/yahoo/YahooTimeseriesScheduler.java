package org.goldenroute.datafeed.pulling.yahoo;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.goldenroute.datafeed.pulling.FetchingJob;
import org.goldenroute.datafeed.pulling.TimeseriesScheduler;
import org.goldenroute.model.Bar;
import org.goldenroute.model.Intervals;
import org.goldenroute.repository.TimeseriesRepository;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.core.env.Environment;

public class YahooTimeseriesScheduler extends TimeseriesScheduler
{
    private static final Logger logger = Logger.getLogger(YahooTimeseriesScheduler.class);

    private YahooTimeseriesRequestor requestor;

    public YahooTimeseriesScheduler(TimeseriesRepository timeseriesRepository, Environment environment)
    {
        requestor = new YahooTimeseriesRequestor(this, timeseriesRepository, environment);
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
        if (intervals.ordinal() <= Intervals.Hour.ordinal())
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
        return requestor.request(intervals, symbols);
    }

    @Override
    protected int getDelaySeconds(String category)
    {
        return 3600 * 5;
    }

    @Override
    protected Class<? extends Job> getJobClass()
    {
        return YahooTimeseriesFetchJob.class;
    }

    @Override
    protected String getContextName()
    {
        return YahooTimeseriesScheduler.class.getSimpleName() + ".THIS";
    }

    public static class YahooTimeseriesFetchJob extends FetchingJob
    {
        @Override
        public String getContextName()
        {
            return YahooTimeseriesScheduler.class.getSimpleName() + ".THIS";
        }
    }
}
