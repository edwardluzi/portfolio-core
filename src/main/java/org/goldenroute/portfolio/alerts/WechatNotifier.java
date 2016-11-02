package org.goldenroute.portfolio.alerts;

import org.goldenroute.Constants;
import org.goldenroute.Wrapper;
import org.goldenroute.portfolio.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.wechat.api.Wechat;

import reactor.bus.Event;
import reactor.fn.Consumer;

public class WechatNotifier implements Consumer<Event<String>>
{
    @Autowired
    private Wrapper<Wechat> wechatWrapper;

    @Autowired
    private AccountService accountService;

    @Override
    public void accept(Event<String> evt)
    {
        String eventData = evt.getData();
        eventData = eventData.substring(1, eventData.length() - 1);
        String[] params = eventData.split("\\s*,\\s*");

        if (params.length == 2 && Constants.EVENT_ID_QUOTE_EOS.equals(params[0]))
        {
            new BalanceNotificationTask(wechatWrapper, accountService).run();
        }
    }
}
