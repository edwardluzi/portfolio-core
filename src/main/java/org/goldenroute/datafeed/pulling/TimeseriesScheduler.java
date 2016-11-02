package org.goldenroute.datafeed.pulling;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.goldenroute.Constants;
import org.goldenroute.DateTimeRange;
import org.goldenroute.Listener;
import org.goldenroute.model.Bar;
import org.goldenroute.model.Intervals;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.DailyTimeIntervalTrigger;
import org.quartz.DateBuilder.IntervalUnit;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;

public abstract class TimeseriesScheduler extends BaseScheduler
{
    private static final Logger logger = Logger.getLogger(TimeseriesScheduler.class);

    public static final String INTERVALS = "intervals";

    private Map<Intervals, Set<String>> symbolTable;
    private Listener<List<Bar>> listener = null;

    public TimeseriesScheduler()
    {
        symbolTable = new HashMap<Intervals, Set<String>>();
        symbolTable.put(Intervals.Minute, ConcurrentHashMap.newKeySet());
        symbolTable.put(Intervals.Minute5, ConcurrentHashMap.newKeySet());
        symbolTable.put(Intervals.Minute15, ConcurrentHashMap.newKeySet());
        symbolTable.put(Intervals.Minute30, ConcurrentHashMap.newKeySet());
        symbolTable.put(Intervals.Hour, ConcurrentHashMap.newKeySet());
        symbolTable.put(Intervals.Daily, ConcurrentHashMap.newKeySet());
        symbolTable.put(Intervals.Weekly, ConcurrentHashMap.newKeySet());
        symbolTable.put(Intervals.Monthly, ConcurrentHashMap.newKeySet());
    }

    public Map<Intervals, Set<String>> getSymbolTable()
    {
        return symbolTable;
    }

    public void schedule(Intervals intervals, Collection<String> symbols)
    {
        symbolTable.get(intervals).addAll(symbols);
    }

    public void cancel(Intervals intervals, Collection<String> symbols)
    {
        symbolTable.get(intervals).removeAll(symbols);
    }

    public void setListener(Listener<List<Bar>> listener)
    {
        this.listener = listener;
    }

    public Listener<List<Bar>> getListener()
    {
        return listener;
    }

    @Override
    protected void scheduleJob()
    {
        scheduleJob(Intervals.Minute);
        scheduleJob(Intervals.Minute5);
        scheduleJob(Intervals.Minute15);
        scheduleJob(Intervals.Minute30);
        scheduleJob(Intervals.Hour);
        scheduleJob(Intervals.Daily);
        scheduleJob(Intervals.Weekly);
        scheduleJob(Intervals.Monthly);
    }

    protected void scheduleJob(Intervals intervals)
    {
        if (intervals.ordinal() < Intervals.Daily.ordinal())
        {
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

            for (DateTimeRange entry : getWorkingHours())
            {
                Calendar start = (Calendar) entry.getOpen().clone();
                Calendar stop = (Calendar) entry.getClose().clone();

                start.add(Calendar.SECOND, getDelaySeconds(intervals.toString()));
                stop.add(Calendar.SECOND, getDelaySeconds(intervals.toString()));

                int intervalsInMinutes = 60;

                switch (intervals)
                {
                case Minute:
                    intervalsInMinutes = 1;
                    break;
                case Minute5:
                    intervalsInMinutes = 5;
                    break;
                case Minute15:
                    intervalsInMinutes = 15;
                    break;
                case Minute30:
                    intervalsInMinutes = 30;
                    break;
                default:
                    intervalsInMinutes = 60;
                    break;
                }

                String identity = String.format("%s:%s:%s", getClass().getSimpleName(), intervals.toString(),
                        dateFormat.format(start.getTime()));

                TriggerBuilder<DailyTimeIntervalTrigger> triggerBuilder = dailyTimeIntervalTriggerBuilder("trigger:"
                        + identity, start, stop, intervalsInMinutes, IntervalUnit.MINUTE);

                JobDetail job = newJob(getJobClass()).withIdentity("job:" + identity).build();
                job.getJobDataMap().put(INTERVALS, intervals.ordinal());

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
        else
        {
            DateTimeRange lastEntry = getWorkingHours().get(getWorkingHours().size() - 1);

            Calendar closeTime = (Calendar) lastEntry.getClose().clone();
            closeTime.add(Calendar.SECOND, getDelaySeconds(intervals.toString()));

            String cron = null;

            switch (intervals)
            {
            case Daily:
                cron = String.format("0 %d %d ? * MON-FRI", closeTime.get(Calendar.MINUTE),
                        closeTime.get(Calendar.HOUR_OF_DAY));
                break;
            case Weekly:
                cron = String.format("0 %d %d ? * FRI", closeTime.get(Calendar.MINUTE),
                        closeTime.get(Calendar.HOUR_OF_DAY));
                break;
            default:
                cron = String.format("0 %d %d LW * ?", closeTime.get(Calendar.MINUTE),
                        closeTime.get(Calendar.HOUR_OF_DAY));
                break;
            }

            String identity = String.format("%s:%s", getClass().getSimpleName(), intervals.toString());

            TriggerBuilder<CronTrigger> triggerBuilder = newTrigger().withIdentity("trigger:" + identity).withSchedule(
                    CronScheduleBuilder.cronSchedule(cron));

            JobDetail job = newJob(getJobClass()).withIdentity("job:" + identity).build();
            job.getJobDataMap().put(INTERVALS, intervals.ordinal());

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

    protected void notifyEndOfStream(Intervals intervals)
    {
        if (getListener() != null)
        {
            if (getListener() != null)
            {
                getListener().update(
                        Arrays.asList(new Bar[] { new Bar(Constants.EVENT_ID_TIMERSERIES_EOS, intervals, Calendar
                                .getInstance().getTimeInMillis(), 0, 0, 0, 0, 0) }));
            }
        }
    }

    protected abstract Map<String, List<Bar>> request(Intervals intervals, Collection<String> symbols);
}
