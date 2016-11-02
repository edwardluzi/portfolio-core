package org.goldenroute.datafeed.pulling.sina;

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

public class SinaTimeseriesScheduler extends TimeseriesScheduler
{
    private static final Logger logger = Logger.getLogger(SinaTimeseriesScheduler.class);

    private SinaTimeseriesRequestor requestor;

    public SinaTimeseriesScheduler(TimeseriesRepository timeseriesRepository)
    {
        requestor = new SinaTimeseriesRequestor(this, timeseriesRepository);
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
        return 30;
    }

    @Override
    protected Class<? extends Job> getJobClass()
    {
        return SinaTimeseriesFetchJob.class;
    }

    @Override
    protected String getContextName()
    {
        return SinaTimeseriesScheduler.class.getSimpleName() + ".THIS";
    }

    public static class SinaTimeseriesFetchJob extends FetchingJob
    {
        @Override
        public String getContextName()
        {
            return SinaTimeseriesScheduler.class.getSimpleName() + ".THIS";
        }
    }
}
