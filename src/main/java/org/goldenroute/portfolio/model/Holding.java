package org.goldenroute.portfolio.model;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Holding
{
    private static AtomicInteger next = new AtomicInteger(0);

    private Long id = Long.valueOf(next.addAndGet(1));
    private String ticker;
    private BigDecimal weight;
    private BigDecimal amount;
    private BigDecimal value;
    private BigDecimal cost;
    private BigDecimal dailyChange;
    private BigDecimal dailyChangePercentage;
    private BigDecimal totalChange;
    private BigDecimal totalChangePercentage;

    public Long getId()
    {
        return id;
    }

    public String getTicker()
    {
        return ticker;
    }

    public void setTicker(String ticker)
    {
        this.ticker = ticker;
    }

    public BigDecimal getWeight()
    {
        return weight;
    }

    public void setWeight(BigDecimal weight)
    {
        this.weight = weight;
    }

    public BigDecimal getAmount()
    {
        return amount;
    }

    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    public BigDecimal getValue()
    {
        return value;
    }

    public void setValue(BigDecimal value)
    {
        this.value = value;
    }

    public BigDecimal getCost()
    {
        return cost;
    }

    public void setCost(BigDecimal cost)
    {
        this.cost = cost;
    }

    public BigDecimal getDailyChange()
    {
        return dailyChange;
    }

    public void setDailyChange(BigDecimal dailyChange)
    {
        this.dailyChange = dailyChange;
    }

    public BigDecimal getDailyChangePercentage()
    {
        return dailyChangePercentage;
    }

    public void setDailyChangePercentage(BigDecimal dailyChangePercentage)
    {
        this.dailyChangePercentage = dailyChangePercentage;
    }

    public BigDecimal getTotalChange()
    {
        return totalChange;
    }

    public void setTotalChange(BigDecimal totalChange)
    {
        this.totalChange = totalChange;
    }

    public BigDecimal getTotalChangePercentage()
    {
        return totalChangePercentage;
    }

    public void setTotalChangePercentage(BigDecimal totalChangePercentage)
    {
        this.totalChangePercentage = totalChangePercentage;
    }
}
