package org.goldenroute.portfolio.config;

import org.goldenroute.portfolio.alpha.AlphaEngine;
import org.goldenroute.portfolio.alpha.AlphaEngineContext;
import org.goldenroute.portfolio.alpha.impl.AlphaEngineContextImpl;
import org.goldenroute.portfolio.alpha.impl.AlphaEngineImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AlphaEngineConfiguration
{
    @Bean
    public AlphaEngine alphaEngine()
    {
        return new AlphaEngineImpl();
    }

    @Bean
    public AlphaEngineContext alphaEngineContext()
    {
        return new AlphaEngineContextImpl();
    }
}
