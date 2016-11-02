package org.goldenroute.datafeed.pulling;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;

public abstract class FetchingJob implements Job
{
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        SchedulerContext schedCtxt = null;

        try
        {
            schedCtxt = context.getScheduler().getContext();
        }
        catch (SchedulerException e)
        {
            throw new JobExecutionException("Error obtaining scheduler context.", e, false);
        }

        BaseScheduler parent = (BaseScheduler) schedCtxt.get(getContextName());

        if (parent == null)
        {
            throw new JobExecutionException("BaseScheduler instance is not found in SchedulerContext");
        }

        parent.execJob(context);
    }

    public abstract String getContextName();
}
