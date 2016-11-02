package org.goldenroute.portfolio.wechat;

import org.springframework.social.wechat.api.Message;

public interface MessageDispatcher
{
    String process(Message message);
}
