package org.goldenroute.portfolio.config;

import org.goldenroute.cache.AccessTokenCache;
import org.goldenroute.cache.impl.AccessTokenCacheImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AccessTokenCacheConfigurer
{
    @Bean
    public AccessTokenCache accessTokenCache()
    {
        return new AccessTokenCacheImpl();
    }
}
