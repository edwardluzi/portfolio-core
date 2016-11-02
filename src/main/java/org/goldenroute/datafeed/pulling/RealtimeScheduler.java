package org.goldenroute.datafeed.pulling;

import static org.quartz.JobBuilder.newJob;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.goldenroute.Constants;
import org.goldenroute.DateTimeRange;
import org.goldenroute.Listener;
import org.goldenroute.model.Quote;
import org.quartz.DailyTimeIntervalTrigger;
import org.quartz.DateBuilder.IntervalUnit;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;

public abstract class RealtimeScheduler extends BaseScheduler
{
    private static final int pullingIntervalMinutes = 2;
    private static final Logger logger = Logger.getLogger(RealtimeScheduler.class);

    private Set<String> symbols;
    private Listener<List<Quote>> listener = null;

    public RealtimeScheduler()
    {
        symbols = ConcurrentHashMap.newKeySet();
    }

    public Set<String> getSymbols()
    {
        return symbols;
    }

    public void schedule(Collection<String> symbols)
    {
        this.symbols.addAll(symbols);
    }

    public void cancel(Collection<String> symbols)
    {
        this.symbols.removeAll(symbols);
    }

    public void setListener(Listener<List<Quote>> listener)
    {
        this.listener = listener;
    }

    public Listener<List<Quote>> getListener()
    {
        return listener;
    }

    @Override
    protected void scheduleJob()
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

        for (DateTimeRange entry : getWorkingHours())
        {
            Calendar start = (Calendar) entry.getOpen().clone();
            Calendar stop = (Calendar) entry.getClose().clone();

            start.add(Calendar.SECOND, getDelaySeconds(null));
            stop.add(Calendar.SECOND, getDelaySeconds(null));

            String identity = String.format("%s:%s", getClass().getSimpleName(), dateFormat.format(start.getTime()));

            TriggerBuilder<DailyTimeIntervalTrigger> triggerBuilder = dailyTimeIntervalTriggerBuilder("trigger:"
                    + identity, start, stop, pullingIntervalMinutes * 60, IntervalUnit.SECOND);

            JobDetail job = newJob(getJobClass()).withIdentity("job:" + identity).build();

            try
            {
                getScheduler().scheduleJob(job, triggerBuilder.build());
            }
            catch (SchedulerException e)
            {
                logger.error(e);
            }
        }
    }

    protected void notifyEndOfStream()
    {
        if (getListener() != null)
        {
            getListener().update(
                    Arrays.asList(new Quote[] { new Quote(Constants.EVENT_ID_QUOTE_EOS, Calendar.getInstance()
                            .getTimeInMillis(), 0, 0) }));
        }
    }

    protected abstract List<Quote> request(Collection<String> symbols);
}