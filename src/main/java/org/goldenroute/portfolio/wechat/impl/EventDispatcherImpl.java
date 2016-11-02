package org.goldenroute.portfolio.wechat.impl;

import org.goldenroute.portfolio.model.Binding;
import org.goldenroute.portfolio.service.ProfileService;
import org.goldenroute.portfolio.wechat.EventDispatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.wechat.api.ClickEvent;
import org.springframework.social.wechat.api.EventMessage;
import org.springframework.social.wechat.api.SubscribeEvent;
import org.springframework.social.wechat.api.TextMessage;

public class EventDispatcherImpl implements EventDispatcher
{
    @Autowired
    private ProfileService profileService;

    @Override
    public String process(EventMessage message)
    {
        String responseContent = "";

        switch (message.getEventType())
        {
        case subscribe:
            responseContent = handleSubscribeEvent(message);
            break;

        case scan:
            responseContent = handleScanEvent(message);
            break;

        case click:
            responseContent = handleClickEvent(message);
            break;

        default:
            responseContent = "Sorry, we could not recognize this event.";

            break;
        }

        return createTextMessage(message, responseContent).toXmlString();
    }

    private TextMessage createTextMessage(EventMessage message, String responseContent)
    {
        TextMessage textMessage = new TextMessage();
        textMessage.setToUserName(message.getFromUserName());
        textMessage.setFromUserName(message.getToUserName());

        textMessage.setContent(responseContent);

        return textMessage;
    }

    private String handleSubscribeEvent(EventMessage message)
    {
        SubscribeEvent subscribeEvent = (SubscribeEvent) message;

        String eventKey = subscribeEvent.getEventKey();

        if (eventKey != null && eventKey.startsWith("qrscene_"))
        {
            String parameter = eventKey.substring("qrscene_".length(), eventKey.length());

            bindAccount(message.getFromUserName(), parameter);
        }

        return "Thanks for your subscribing, and your wechat account has been bound to your portfolio account.";
    }

    private String handleScanEvent(EventMessage message)
    {
        SubscribeEvent subscribeEvent = (SubscribeEvent) message;
        String eventKey = subscribeEvent.getEventKey();

        if (eventKey != null)
        {
            bindAccount(message.getFromUserName(), eventKey);
        }

        return "Your wechat account has been bound to your portfolio account.";
    }

    private String handleClickEvent(EventMessage message)
    {
        String responseContent = null;

        ClickEvent clickEvent = (ClickEvent) message;

        if (clickEvent.getEventKey() == "menu_help_bind_account")
        {
            Binding binding = profileService.createBinding(clickEvent.getFromUserName());

            responseContent = "Binding key: " + binding.getParameter().toString();
        }
        else
        {
            responseContent = "Sorry";
        }

        return createTextMessage(message, responseContent).toXmlString();
    }

    private void bindAccount(String openId, String parameter)
    {
        profileService.bindWechat(openId, Integer.parseInt(parameter));
    }
}
