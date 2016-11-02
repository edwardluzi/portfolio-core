package org.goldenroute.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.goldenroute.util.CalendarUtil;

@Entity
@Table(name = "quote")
public class Quote
{
    @Id
    @GeneratedValue
    private Long id;

    private String symbol;

    private Long timestamp;

    private double price;

    private double volume;

    public Long getId()
    {
        return id;
    }

    public String getSymbol()
    {
        return symbol;
    }

    public Long getTimestamp()
    {
        return timestamp;
    }

    public double getPrice()
    {
        return price;
    }

    public double getVolume()
    {
        return volume;
    }

    public Quote()
    {
    }

    public Quote(String symbol, Long timestamp, double price, double volume)
    {
        super();
        this.symbol = symbol;
        this.timestamp = timestamp;
        this.price = price;
        this.volume = volume;
    }

    public Object[] toTuples()
    {
        return new Object[] { symbol, timestamp, price, volume };
    }

    @Override
    public String toString()
    {
        return "Quote [id=" + id + ", symbol=" + symbol + ", timestamp=" + CalendarUtil.formatDateTime(timestamp)
                + ", price=" + price + ", volume=" + volume + "]";
    }
}
