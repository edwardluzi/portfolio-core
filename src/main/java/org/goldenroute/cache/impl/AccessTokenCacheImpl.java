package org.goldenroute.cache.impl;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.goldenroute.cache.AccessTokenCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;

public class AccessTokenCacheImpl implements AccessTokenCache
{
    private Cache cache;

    @Autowired
    public void setCacheManager(EhCacheManagerFactoryBean cacheManager)
    {
        cache = cacheManager.getObject().getCache("tokenCache");
    }

    @Override
    public String findTokenByName(String username)
    {
        String token = null;
        Element element = cache.get(username);

        if (element != null && !element.isExpired())
        {
            Object object = element.getObjectValue();

            if (object instanceof String)
            {
                token = object.toString();
            }
        }

        return token;
    }

    @Override
    public Authentication findAuthenticationByToken(String token)
    {
        Authentication auth = null;
        Element element = cache.get(token);

        if (element != null && !element.isExpired())
        {
            Object object = element.getObjectValue();

            if (object instanceof Authentication)
            {
                auth = (Authentication) object;
            }
        }

        return auth;
    }

    @Override
    public void addAuthentication(String token, Authentication authentication)
    {
        cache.put(new Element(authentication.getName(), token));
        cache.put(new Element(token, authentication));
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        Assert.notNull(cache, "cacheManager required");
    }
}
