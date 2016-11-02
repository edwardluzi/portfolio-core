package org.goldenroute.model;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.goldenroute.util.CalendarUtil;

@Entity
@Table(name = "timeseries")
public class Bar
{
    @Id
    @GeneratedValue
    private Long id;
    private String symbol;

    @Enumerated(EnumType.ORDINAL)
    private Intervals intervals;
    private Long timestamp;
    private double open;
    private double high;
    private double low;
    private double close;
    private double volume;

    public Long getId()
    {
        return id;
    }

    public String getSymbol()
    {
        return symbol;
    }

    public Intervals getIntervals()
    {
        return intervals;
    }

    public Long getTimestamp()
    {
        return timestamp;
    }

    public double getOpen()
    {
        return open;
    }

    public double getHigh()
    {
        return high;
    }

    public double getLow()
    {
        return low;
    }

    public double getClose()
    {
        return close;
    }

    public double getVolume()
    {
        return volume;
    }

    public Bar()
    {
    }

    public Bar(String symbol, Intervals intervals, Long timestamp, double open, double high, double low, double close,
            double volume)
    {
        this.symbol = symbol;
        this.intervals = intervals;
        this.timestamp = timestamp;
        this.open = open;
        this.low = low;
        this.high = high;
        this.close = close;
        this.volume = volume;
    }

    public Object[] toTuples()
    {
        return new Object[] { symbol, intervals.ordinal(), timestamp, open, high, low, close, volume };
    }

    @Override
    public String toString()
    {
        return "Bar [id=" + (id != null ? id : Long.valueOf(0)) + ", symbol=" + symbol + ", intervals=" + intervals
                + ", timestamp=" + CalendarUtil.formatDateTime(timestamp) + ", open=" + open + ", high=" + high
                + ", low=" + low + ", close=" + close + ", volume=" + volume + "]";
    }
}
