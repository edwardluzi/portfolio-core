package org.goldenroute.portfolio.wechat.impl;

import org.goldenroute.portfolio.wechat.MessageDispatcher;
import org.springframework.social.wechat.api.Message;
import org.springframework.social.wechat.api.TextMessage;

public class MessageDispatcherImpl implements MessageDispatcher
{
    @Override
    public String process(Message message)
    {
        switch (message.getType())
        {
        case text:
        {
            return processTextMessage(message);
        }
        default:
        {
            return "";
        }
        }
    }

    private String processTextMessage(Message message)
    {
        TextMessage receivedMessage = (TextMessage) message;
        TextMessage responseMessage = new TextMessage();

        responseMessage.setToUserName(receivedMessage.getFromUserName());
        responseMessage.setFromUserName(receivedMessage.getToUserName());
        responseMessage.setContent(String.format(
                "Hello, %s, welcome to golden route portfolio management, this is your personal assistant.",
                receivedMessage.getFromUserName()));

        return responseMessage.toXmlString();
    }
}
