package org.goldenroute.portfolio.alerts;

import org.springframework.social.wechat.api.ValueColorPair;

public class BalanceNotification
{
    private ValueColorPair first;
    private ValueColorPair greeting;
    private ValueColorPair date;
    private ValueColorPair balance;
    private ValueColorPair daily;
    private ValueColorPair total;
    private ValueColorPair remark;

    public ValueColorPair getFirst()
    {
        return first;
    }

    public void setFirst(ValueColorPair first)
    {
        this.first = first;
    }

    public ValueColorPair getGreeting()
    {
        return greeting;
    }

    public void setGreeting(ValueColorPair greeting)
    {
        this.greeting = greeting;
    }

    public ValueColorPair getRemark()
    {
        return remark;
    }

    public void setRemark(ValueColorPair remark)
    {
        this.remark = remark;
    }

    public ValueColorPair getDate()
    {
        return date;
    }

    public void setDate(ValueColorPair date)
    {
        this.date = date;
    }

    public ValueColorPair getBalance()
    {
        return balance;
    }

    public void setBalance(ValueColorPair balance)
    {
        this.balance = balance;
    }

    public ValueColorPair getDaily()
    {
        return daily;
    }

    public void setDaily(ValueColorPair daily)
    {
        this.daily = daily;
    }

    public ValueColorPair getTotal()
    {
        return total;
    }

    public void setTotal(ValueColorPair total)
    {
        this.total = total;
    }
}
