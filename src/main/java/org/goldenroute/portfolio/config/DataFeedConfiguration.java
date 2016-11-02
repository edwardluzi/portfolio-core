package org.goldenroute.portfolio.config;

import org.goldenroute.datafeed.QuoteFeed;
import org.goldenroute.datafeed.TimeseriesFeed;
import org.goldenroute.datafeed.pulling.composited.CompositedTimeseriesFeed;
import org.goldenroute.datafeed.pulling.sina.SinaQuoteFeed;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataFeedConfiguration
{
    @Bean
    public TimeseriesFeed timeseriesFeed()
    {
        return new CompositedTimeseriesFeed();
    }

    @Bean
    public QuoteFeed quoteFeed()
    {
        return new SinaQuoteFeed();
    }
}
