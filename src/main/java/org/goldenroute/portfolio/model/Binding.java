package org.goldenroute.portfolio.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Binding
{
    @Id
    @GeneratedValue
    private Long id;

    private Long createdAt;
    private Long profileId;
    private Integer parameter;
    private Integer expireSeconds;
    private String openId;

    public Long getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt)
    {
        this.createdAt = createdAt;
    }

    public Long getProfileId()
    {
        return profileId;
    }

    public void setProfileId(Long profileId)
    {
        this.profileId = profileId;
    }

    public Integer getParameter()
    {
        return parameter;
    }

    public void setParameter(Integer parameter)
    {
        this.parameter = parameter;
    }

    public Long getId()
    {
        return id;
    }

    public Integer getExpireSeconds()
    {
        return expireSeconds;
    }

    public void setExpireSeconds(Integer expireSeconds)
    {
        this.expireSeconds = expireSeconds;
    }

    public String getOpenId()
    {
        return openId;
    }

    public void setOpenId(String openId)
    {
        this.openId = openId;
    }

}
