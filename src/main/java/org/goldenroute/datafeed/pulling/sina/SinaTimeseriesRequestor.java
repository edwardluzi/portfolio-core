package org.goldenroute.datafeed.pulling.sina;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.goldenroute.DateTimeRange;
import org.goldenroute.datafeed.pulling.TimeseriesScheduler;
import org.goldenroute.model.Bar;
import org.goldenroute.model.Intervals;
import org.goldenroute.repository.TimeseriesRepository;
import org.goldenroute.sinafinance.SinaFinance;
import org.goldenroute.sinafinance.impl.SinaFinanceTemplate;
import org.goldenroute.util.CalendarUtil;
import org.goldenroute.util.FloatUtil;
import org.springframework.data.domain.PageRequest;

public class SinaTimeseriesRequestor
{
    private static final Logger logger = Logger.getLogger(SinaTimeseriesRequestor.class);

    private TimeseriesScheduler timeseriesScheduler;
    private SinaFinance sinaFinance;
    private TimeseriesRepository timeseriesRepository;
    private Map<Intervals, Integer> scaleMap;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public SinaTimeseriesRequestor(TimeseriesScheduler timeseriesScheduler, TimeseriesRepository timeseriesRepository)
    {
        this.timeseriesScheduler = timeseriesScheduler;
        this.timeseriesRepository = timeseriesRepository;
        this.sinaFinance = new SinaFinanceTemplate();
        this.scaleMap = new HashMap<>();
        this.scaleMap.put(Intervals.Minute, 1);
        this.scaleMap.put(Intervals.Minute5, 5);
        this.scaleMap.put(Intervals.Minute15, 15);
        this.scaleMap.put(Intervals.Minute30, 30);
        this.scaleMap.put(Intervals.Hour, 60);
    }

    public Map<String, List<Bar>> request(Intervals intervals, Collection<String> symbols)
    {
        if (intervals.ordinal() > Intervals.Hour.ordinal())
        {
            throw new IllegalArgumentException("intervals");
        }

        Map<String, List<Bar>> map = new HashMap<>();

        if (intervals.ordinal() <= Intervals.Hour.ordinal())
        {
            for (String symbol : symbols)
            {
                List<Bar> lasts = findFirstTwoBars(symbol, intervals);
                int sampleCount = calculateSampleCount(intervals, lasts);

                if (sampleCount <= 0)
                {
                    continue;
                }

                List<org.goldenroute.sinafinance.Bar> sinaBars = sinaFinance.timeseriesOperations().getTimeseries(
                        SinaSymbolConverter.getInstance().toLocal(symbol), scaleMap.get(intervals), sampleCount);

                if (sinaBars != null)
                {
                    List<Bar> bars = new ArrayList<Bar>();
                    convert(intervals, symbol, sinaBars, bars, lasts);

                    if (timeseriesScheduler.getListener() != null)
                    {
                        timeseriesScheduler.getListener().update(bars);
                    }

                    persist(bars);

                    map.put(symbol, bars);
                }
            }
        }

        return map;
    }

    private void persist(List<Bar> bars)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                timeseriesRepository.save(bars);
            }
        }).start();
    }

    private boolean convert(Intervals intervals, String symbol, List<org.goldenroute.sinafinance.Bar> sinaBars,
            List<Bar> bars, List<Bar> lasts)
    {
        Long baseline = lasts != null && lasts.size() > 0 ? lasts.get(0).getTimestamp() : null;

        int index = 0;

        for (org.goldenroute.sinafinance.Bar sinaBar : sinaBars)
        {
            logger.debug(sinaBar.toString());

            Long date = null;

            if (intervals == Intervals.Minute)
            {
                date = calculateTimestamp(index++);
            }
            else
            {
                try
                {
                    date = dateFormat.parse(sinaBar.getDay()).getTime();
                    date -= 1000 * 3600 * 8;
                }
                catch (ParseException e)
                {
                    logger.error(e);
                }
            }

            if (date == null)
            {
                continue;
            }

            if (baseline != null)
            {
                int result = (int) (date - baseline);

                if (result < 0)
                {
                    continue;
                }
                else if (result == 0)
                {
                    if (sinaBar.getClose() != lasts.get(0).getClose())
                    {
                        return false;
                    }

                    continue;
                }
            }

            if (!FloatUtil.equal(sinaBar.getVolume(), 0, FloatUtil.Precision.P2))
            {
                bars.add(new Bar(symbol, intervals, date, sinaBar.getOpen(), sinaBar.getHigh(), sinaBar.getLow(),
                        sinaBar.getClose(), sinaBar.getVolume()));
            }
        }

        return true;
    }

    private int calculateSampleCount(Intervals intervals, List<Bar> lasts)
    {
        if (lasts != null && lasts.size() == 2 && lasts.get(0).getIntervals() != Intervals.Minute)
        {
            Calendar now = Calendar.getInstance();
            Calendar start = Calendar.getInstance();
            start.setTimeInMillis(lasts.get(0).getTimestamp());

            Duration total = toMarketClose(start);
            total = total.plus(fromMarketOpen(now));

            if (CalendarUtil.compare(start, now, Calendar.DAY_OF_MONTH) == 0)
            {
                total = total.minus(calculateMarketDuration());
            }
            else
            {
                total.plus(calculateMarketDuration().multipliedBy(CalendarUtil.weekDayCount(start, now) - 1));
            }

            return (int) total.toMinutes() / scaleMap.get(intervals);
        }
        else
        {
            return 1024;
        }
    }

    private Duration calculateMarketDuration()
    {
        Duration duration = Duration.ofMillis(0);

        for (DateTimeRange range : timeseriesScheduler.getWorkingHours())
        {
            duration = duration.plus(range.getClose().getTimeInMillis() - range.getOpen().getTimeInMillis(),
                    ChronoUnit.MILLIS);
        }

        return duration;
    }

    private Duration toMarketClose(Calendar calendar)
    {
        boolean found = false;
        Duration duration = Duration.ofMillis(0);

        for (DateTimeRange range : timeseriesScheduler.getWorkingHours())
        {
            if (!found)
            {
                if (CalendarUtil.isBetweenTimeOfDay(range.getOpen(), range.getClose(), calendar))
                {
                    duration.plus(range.getClose().getTimeInMillis() - calendar.getTimeInMillis(), ChronoUnit.MILLIS);
                    found = true;
                }
            }
            else
            {
                duration = duration.plus(range.getClose().getTimeInMillis() - range.getOpen().getTimeInMillis(),
                        ChronoUnit.MILLIS);
            }
        }
        return duration;
    }

    private Duration fromMarketOpen(Calendar calendar)
    {
        Duration duration = Duration.ofMillis(0);

        for (DateTimeRange range : timeseriesScheduler.getWorkingHours())
        {
            if (CalendarUtil.beforeTimeOfDay(calendar, range.getOpen()))
            {
                break;
            }

            if (CalendarUtil.isBetweenTimeOfDay(range.getOpen(), range.getClose(), calendar))
            {
                duration = duration.plus(calendar.getTimeInMillis() - range.getOpen().getTimeInMillis(),
                        ChronoUnit.MILLIS);
            }
            else
            {
                duration = duration.plus(range.getClose().getTimeInMillis() - range.getOpen().getTimeInMillis(),
                        ChronoUnit.MILLIS);
            }
        }

        return duration;
    }

    private long calculateTimestamp(int index)
    {
        Calendar calendar = Calendar.getInstance();

        for (DateTimeRange range : timeseriesScheduler.getWorkingHours())
        {
            int totalMinutes = (int) CalendarUtil.between(range.getOpen(), range.getClose()).toMinutes();

            if (index < totalMinutes)
            {
                CalendarUtil.add(CalendarUtil.normalize(calendar, Calendar.DAY_OF_MONTH),
                        CalendarUtil.getTimeOfDay(range.getOpen()));
                calendar.add(Calendar.MINUTE, index + 1);
                break;
            }
            else
            {
                index -= totalMinutes;
            }
        }

        return calendar.getTimeInMillis();
    }

    private List<Bar> findFirstTwoBars(String symbol, Intervals intervals)
    {
        List<Bar> result = null;

        try
        {
            result = timeseriesRepository.findBySymbolAndIntervalsOrderByTimestampDesc(symbol, intervals,
                    new PageRequest(0, 2));
        }
        catch (Exception e)
        {
            logger.debug(e);
        }

        return result;
    }
}
