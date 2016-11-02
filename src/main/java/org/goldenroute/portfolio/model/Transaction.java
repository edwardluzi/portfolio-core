package org.goldenroute.portfolio.model;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
public class Transaction
{
    public enum Type
    {
        Buying, Selling, ShortSelling, shortCovering
    }

    @Id
    @GeneratedValue
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner")
    private Portfolio owner;

    private Long timestamp;
    private String ticker;
    private Type type;

    @Column(precision = 19, scale = 3)
    private BigDecimal price;
    @Column(precision = 19, scale = 3)
    private BigDecimal amount;
    @Column(precision = 19, scale = 3)
    private BigDecimal commission;
    @Column(precision = 19, scale = 3)
    private BigDecimal otherCharges;

    public Portfolio getOwner()
    {
        return owner;
    }

    public void setOwner(Portfolio owner)
    {
        this.owner = owner;
    }

    public Long getTimestamp()
    {
        return timestamp;
    }

    public void setTimestamp(Long timestamp)
    {
        this.timestamp = timestamp;
    }

    public String getTicker()
    {
        return ticker;
    }

    public void setTicker(String ticker)
    {
        this.ticker = ticker;
    }

    public Type getType()
    {
        return type;
    }

    public void setType(Type type)
    {
        this.type = type;
    }

    public BigDecimal getPrice()
    {
        return price;
    }

    public void setPrice(BigDecimal price)
    {
        this.price = price;
    }

    public BigDecimal getAmount()
    {
        return amount;
    }

    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    public BigDecimal getCommission()
    {
        return commission;
    }

    public void setCommission(BigDecimal commission)
    {
        this.commission = commission;
    }

    public BigDecimal getOtherCharges()
    {
        return otherCharges;
    }

    public void setOtherCharges(BigDecimal otherCharges)
    {
        this.otherCharges = otherCharges;
    }

    public Long getId()
    {
        return id;
    }
}
