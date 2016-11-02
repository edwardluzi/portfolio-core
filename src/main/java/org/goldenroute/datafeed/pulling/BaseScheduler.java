package org.goldenroute.datafeed.pulling;

import static org.quartz.TriggerBuilder.newTrigger;

import java.util.Calendar;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.goldenroute.DateTimeRange;
import org.goldenroute.util.CalendarUtil;
import org.quartz.DailyTimeIntervalScheduleBuilder;
import org.quartz.DailyTimeIntervalTrigger;
import org.quartz.DateBuilder.IntervalUnit;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TimeOfDay;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.calendar.HolidayCalendar;

public abstract class BaseScheduler
{
    private static final Logger logger = Logger.getLogger(BaseScheduler.class);

    private List<DateTimeRange> workingHours;
    private HolidayCalendar holidayCalendar;
    private Scheduler scheduler;

    public List<DateTimeRange> getWorkingHours()
    {
        return workingHours;
    }

    public void setWorkingHours(List<DateTimeRange> workingHours)
    {
        this.workingHours = workingHours;
    }

    protected HolidayCalendar getHolidayCalendar()
    {
        return holidayCalendar;
    }

    public void setHoliday(Set<Calendar> holidays)
    {
        holidayCalendar = new HolidayCalendar();

        for (Calendar calendar : holidays)
        {
            holidayCalendar.addExcludedDate(CalendarUtil.normalize(calendar, Calendar.DAY_OF_MONTH).getTime());
        }
    }

    protected Scheduler getScheduler()
    {
        return scheduler;
    }

    protected void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

    public void start()
    {
        try
        {
            setScheduler(StdSchedulerFactory.getDefaultScheduler());

            if (getHolidayCalendar() != null)
            {
                getScheduler().addCalendar(getHolidayName(), getHolidayCalendar(), false, false);
            }

            scheduleJob();

            getScheduler().getContext().put(getContextName(), this);

            if (!getScheduler().isStarted())
            {
                getScheduler().start();
            }
        }
        catch (SchedulerException e)
        {
            logger.error(e);
        }
    }

    public void stop()
    {
        try
        {
            if (getScheduler() != null)
            {
                getScheduler().shutdown();
            }
        }
        catch (SchedulerException e)
        {
            logger.error(e);
        }
    }

    protected TriggerBuilder<DailyTimeIntervalTrigger> dailyTimeIntervalTriggerBuilder(String identity, Calendar start,
            Calendar stop, int timeInterval, IntervalUnit unit)
    {
        DailyTimeIntervalScheduleBuilder scheduleBuilder = DailyTimeIntervalScheduleBuilder.dailyTimeIntervalSchedule()
                .onMondayThroughFriday().startingDailyAt(TimeOfDay.hourAndMinuteAndSecondFromDate(start.getTime()))
                .endingDailyAt(TimeOfDay.hourAndMinuteAndSecondFromDate(stop.getTime()))
                .withInterval(timeInterval, unit);

        TriggerBuilder<DailyTimeIntervalTrigger> triggerBuilder = newTrigger().withIdentity(identity).withSchedule(
                scheduleBuilder);

        if (getHolidayCalendar() != null)
        {
            triggerBuilder.modifiedByCalendar(getHolidayName());
        }

        return triggerBuilder;
    }

    public boolean afterMarketClose(String category)
    {
        List<DateTimeRange> workingHours = getWorkingHours();
        return CalendarUtil
                .afterTimeOfDay(Calendar.getInstance(), workingHours.get(workingHours.size() - 1).getClose());
    }

    protected String getHolidayName()
    {
        return this.getClass().getSimpleName() + ".HOLIDAY";
    }

    protected abstract void scheduleJob();

    protected abstract void execJob(JobExecutionContext context);

    protected abstract Class<? extends Job> getJobClass();

    protected abstract String getContextName();

    protected abstract int getDelaySeconds(String category);
}
