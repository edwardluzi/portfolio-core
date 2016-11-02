package org.goldenroute.portfolio.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Account
{
    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @OneToOne(mappedBy = "owner", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Profile profile;

    @OneToMany(mappedBy = "owner", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Portfolio> portfolios;

    public Long getId()
    {
        return id;
    }

    public String getUsername()
    {
        return username;
    }

    @JsonIgnore
    public String getSocial()
    {
        return username.substring(0, 2);
    }

    public Profile getProfile()
    {
        return profile;
    }

    public void setProfile(Profile profile)
    {
        this.profile = profile;
    }

    public List<Portfolio> getPortfolios()
    {
        return portfolios;
    }

    public void setPortfolios(List<Portfolio> portfolios)
    {
        this.portfolios = portfolios;
    }

    public Account(String username)
    {
        this.username = username;
    }

    Account()
    {
    }

    public void addOrUpdate(Portfolio portfolio)
    {
        if (portfolios == null)
        {
            portfolios = new ArrayList<Portfolio>();
        }

        if (portfolio.getId() != null)
        {
            int index = Collections.binarySearch(portfolios, portfolio, new Comparator<Portfolio>()
            {
                @Override
                public int compare(Portfolio lhs, Portfolio rhs)
                {
                    return (int) (lhs.getId() - rhs.getId());
                }
            });

            if (index >= 0)
            {
                portfolios.set(index, portfolio);
            }
            else
            {
                portfolios.add(portfolio);
            }
        }
        else
        {
            portfolios.add(portfolio);
        }
    }

    public Portfolio find(final Long id)
    {
        if (portfolios == null)
        {
            return null;
        }

        for (Portfolio portfolio : portfolios)
        {
            if (portfolio.getId() == id)
            {
                return portfolio;
            }
        }

        return null;
    }

    public Portfolio remove(final Long id)
    {
        Portfolio portfolio = find(id);

        if (portfolio == null)
        {
            return null;
        }

        portfolios.remove(portfolio);

        portfolio.setOwner(null);

        return portfolio;
    }
}
