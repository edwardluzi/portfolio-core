package org.goldenroute.portfolio.config;

import org.goldenroute.portfolio.alerts.WechatNotifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import reactor.bus.Event;
import reactor.fn.Consumer;

@Configuration
public class NotifierConfigurer
{
    @Bean(name = { "wechatNotifier" })
    public Consumer<Event<String>> wechatNotifier()
    {
        return new WechatNotifier();
    }
}
