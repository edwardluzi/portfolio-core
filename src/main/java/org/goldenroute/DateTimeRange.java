package org.goldenroute;

import java.util.Calendar;

public class DateTimeRange
{
    private Calendar open;
    private Calendar close;

    public Calendar getOpen()
    {
        return open;
    }

    public void setOpen(Calendar open)
    {
        this.open = (Calendar) open.clone();
    }

    public Calendar getClose()
    {
        return close;
    }

    public void setClose(Calendar close)
    {
        this.close = (Calendar) close.clone();
    }

    public DateTimeRange(Calendar open, Calendar close)
    {
        this.open = (Calendar) open.clone();
        this.close = (Calendar) close.clone();
    }
}
