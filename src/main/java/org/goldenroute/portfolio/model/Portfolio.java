package org.goldenroute.portfolio.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
public class Portfolio
{
    @Id
    @GeneratedValue
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner")
    private Account owner;

    private String name;

    private String benchmark;

    private Currency currency;

    private AssetClass primaryAssetClass;

    private String description;

    @Transient
    private BigDecimal value;

    @Transient
    private BigDecimal cost;

    @Transient
    private BigDecimal dailyChange;

    @Transient
    private BigDecimal dailyChangePercentage;

    @Transient
    private BigDecimal totalChange;

    @Transient
    private BigDecimal totalChangePercentage;

    @OneToMany(mappedBy = "owner", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Transaction> transactions;

    @Transient
    public List<Holding> holdings;

    @Transient
    private BigDecimal weight;

    public Long getId()
    {
        return id;
    }

    public Account getOwner()
    {
        return owner;
    }

    public void setOwner(Account owner)
    {
        this.owner = owner;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getBenchmark()
    {
        return benchmark;
    }

    public void setBenchmark(String benchmark)
    {
        this.benchmark = benchmark;
    }

    public Currency getCurrency()
    {
        return currency;
    }

    public void setCurrency(Currency currency)
    {
        this.currency = currency;
    }

    public AssetClass getPrimaryAssetClass()
    {
        return primaryAssetClass;
    }

    public void setPrimaryAssetClass(AssetClass primaryAssetClass)
    {
        this.primaryAssetClass = primaryAssetClass;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
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

    public List<Transaction> getTransactions()
    {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions)
    {
        this.transactions = transactions;
    }

    public BigDecimal getWeight()
    {
        return weight;
    }

    public void setWeight(BigDecimal weight)
    {
        this.weight = weight;
    }

    public void setHoldings(List<Holding> holdings)
    {
        this.holdings = holdings;
    }

    public List<Holding> getHoldings()
    {
        return holdings;
    }

    public void addOrUpdate(Transaction transaction)
    {
        if (transactions == null)
        {
            transactions = new ArrayList<Transaction>();
        }

        if (transaction.getId() != null)
        {
            int index = Collections.binarySearch(transactions, transaction, new Comparator<Transaction>()
            {
                @Override
                public int compare(Transaction lhs, Transaction rhs)
                {
                    return (int) (lhs.getId() - rhs.getId());
                }
            });

            if (index >= 0)
            {
                transactions.set(index, transaction);
            }
            else
            {
                transactions.add(transaction);
            }
        }
        else
        {
            transactions.add(transaction);
        }
    }

    public Transaction find(final Long id)
    {
        if (transactions == null)
        {
            return null;
        }

        for (Transaction transaction : transactions)
        {
            if (transaction.getId() == id)
            {
                return transaction;
            }
        }

        return null;
    }

    public Transaction remove(final Long id)
    {
        Transaction transaction = find(id);

        if (transaction == null)
        {
            return null;
        }

        transactions.remove(transaction);

        transaction.setOwner(null);

        return transaction;
    }

    @Transient
    @JsonIgnore
    public Set<String> getTickers()
    {
        if (transactions != null)
        {
            return transactions.stream().map(t -> t.getTicker()).distinct().collect(Collectors.toSet());
        }
        else
        {
            return new HashSet<String>();
        }
    }
}
