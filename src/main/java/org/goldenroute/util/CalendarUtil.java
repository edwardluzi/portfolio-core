package org.goldenroute.util;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class CalendarUtil
{
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");

    private static Set<Integer> validFields = new HashSet<Integer>(Arrays.asList(new Integer[] { Calendar.MILLISECOND,
            Calendar.SECOND, Calendar.MINUTE, Calendar.HOUR, Calendar.HOUR_OF_DAY, Calendar.DAY_OF_MONTH,
            Calendar.MONTH }));

    public static String formatDateTime(Long date)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        return dateFormat.format(calendar.getTime());
    }

    public static Calendar normalize(Calendar calendar, int precision)
    {
        if (!validFields.contains(precision))
        {
            throw new IllegalArgumentException("precision");
        }

        if (precision < Calendar.MILLISECOND)
        {
            calendar.set(Calendar.MILLISECOND, 0);
        }

        if (precision < Calendar.SECOND)
        {
            calendar.set(Calendar.SECOND, 0);
        }

        if (precision < Calendar.MINUTE)
        {
            calendar.set(Calendar.MINUTE, 0);
        }

        if (precision < Calendar.HOUR_OF_DAY)
        {
            calendar.set(Calendar.HOUR_OF_DAY, 0);
        }

        if (precision < Calendar.DAY_OF_MONTH)
        {
            calendar.set(Calendar.DAY_OF_MONTH, 0);
        }

        return calendar;
    }

    public static Calendar getDate(Calendar calendar)
    {
        return normalize((Calendar) calendar.clone(), Calendar.DAY_OF_MONTH);
    }

    public static Duration getTimeOfDay(Calendar calendar)
    {
        return Duration.of(calendar.getTimeInMillis() % (1000 * 3600 * 24), ChronoUnit.MILLIS);
    }

    public static void add(Calendar calendar, Duration duration)
    {
        calendar.add(Calendar.MILLISECOND, (int) duration.toMillis());
    }

    public static int compare(Calendar calendar1, Calendar calendar2, int precision)
    {
        if (!validFields.contains(precision))
        {
            throw new IllegalArgumentException("precision");
        }

        Calendar clone1 = normalize((Calendar) calendar1.clone(), precision);
        Calendar clone2 = normalize((Calendar) calendar2.clone(), precision);

        return clone1.compareTo(clone2);
    }

    public static int compare(Long timeInMillis1, Long timeInMillis2, int precision)
    {
        Calendar calendar1 = Calendar.getInstance();
        Calendar calendar2 = (Calendar) calendar1.clone();

        calendar1.setTimeInMillis(timeInMillis1);
        calendar2.setTimeInMillis(timeInMillis2);

        normalize(calendar1, precision);
        normalize(calendar2, precision);

        return calendar1.compareTo(calendar2);
    }

    public static boolean isBetween(Calendar start, Calendar stop, Calendar calendar)
    {
        return !(start.after(calendar) || stop.before(calendar));
    }

    public static boolean isBetweenDate(Calendar start, Calendar stop, Calendar calendar)
    {
        return isBetween(getDate(start), getDate(stop), getDate(calendar));
    }

    public static boolean isBetweenTimeOfDay(Calendar start, Calendar stop, Calendar calendar)
    {
        long timeInMillis = calendar.getTimeInMillis() % (1000 * 3600 * 24);

        return timeInMillis >= start.getTimeInMillis() % (1000 * 3600 * 24)
                && timeInMillis <= stop.getTimeInMillis() % (1000 * 3600 * 24);
    }

    public static Duration between(Calendar start, Calendar stop)
    {
        return Duration.of(stop.getTimeInMillis() - start.getTimeInMillis(), ChronoUnit.MILLIS);
    }

    public static int weekDayCount(Calendar start, Calendar stop)
    {
        Calendar startDate = getDate(start);
        Calendar stopDate = getDate(stop);

        int weekDays = 0;

        while (startDate.before(stopDate))
        {
            if (startDate.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY
                    && startDate.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY)
            {
                ++weekDays;
            }

            startDate.add(Calendar.DAY_OF_MONTH, 1);
        }

        return weekDays;
    }

    public static boolean beforeTimeOfDay(Calendar calendar1, Calendar calendar2)
    {
        return calendar1.getTimeInMillis() % (1000 * 3600 * 24) < calendar2.getTimeInMillis() % (1000 * 3600 * 24);
    }

    public static boolean afterTimeOfDay(Calendar calendar1, Calendar calendar2)
    {
        return calendar1.getTimeInMillis() % (1000 * 3600 * 24) > calendar2.getTimeInMillis() % (1000 * 3600 * 24);
    }
}
