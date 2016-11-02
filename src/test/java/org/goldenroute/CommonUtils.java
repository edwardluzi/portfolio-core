package org.goldenroute;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.goldenroute.util.CalendarUtil;

public class CommonUtils
{
    public static List<DateTimeRange> createDefaultWorkingHours()
    {
        Calendar open = Calendar.getInstance();
        Calendar close = Calendar.getInstance();

        open.set(Calendar.HOUR_OF_DAY, 1);
        open.set(Calendar.MINUTE, 30);

        close.set(Calendar.HOUR_OF_DAY, 4);
        close.set(Calendar.MINUTE, 30);

        CalendarUtil.normalize(open, Calendar.MINUTE);
        CalendarUtil.normalize(close, Calendar.MINUTE);

        List<DateTimeRange> workingHours = new ArrayList<>();
        workingHours.add(new DateTimeRange(open, close));

        open.set(Calendar.HOUR_OF_DAY, 5);
        open.set(Calendar.MINUTE, 0);

        close.set(Calendar.HOUR_OF_DAY, 11);
        close.set(Calendar.MINUTE, 20);

        CalendarUtil.normalize(open, Calendar.MINUTE);
        CalendarUtil.normalize(close, Calendar.MINUTE);

        workingHours.add(new DateTimeRange(open, close));

        return workingHours;
    }
}
