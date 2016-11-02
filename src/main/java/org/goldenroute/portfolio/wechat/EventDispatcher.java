package org.goldenroute.portfolio.wechat;

import org.springframework.social.wechat.api.EventMessage;

public interface EventDispatcher
{
    String process(EventMessage message);
}
