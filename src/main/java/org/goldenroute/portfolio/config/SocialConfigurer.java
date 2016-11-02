package org.goldenroute.portfolio.config;

import org.goldenroute.Wrapper;
import org.goldenroute.portfolio.wechat.EventDispatcher;
import org.goldenroute.portfolio.wechat.MessageDispatcher;
import org.goldenroute.portfolio.wechat.WechatWrapper;
import org.goldenroute.portfolio.wechat.impl.EventDispatcherImpl;
import org.goldenroute.portfolio.wechat.impl.MessageDispatcherImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.social.wechat.api.Wechat;

@Configuration
public class SocialConfigurer
{
    @Bean
    public Wrapper<Wechat> wechat()
    {
        return new WechatWrapper();
    }

    @Bean
    public EventDispatcher wechatEventDispatcher()
    {
        return new EventDispatcherImpl();
    }

    @Bean
    public MessageDispatcher wechatMessageDispatcher()
    {
        return new MessageDispatcherImpl();
    }
}
