package org.goldenroute.portfolio.wechat;

import org.goldenroute.Wrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.wechat.api.Wechat;
import org.springframework.social.wechat.connect.WechatServiceProvider;

public class WechatWrapper implements Wrapper<Wechat>
{
    @Value("${social.wechat.appid}")
    private String appid;

    @Value("${social.wechat.secret}")
    private String secret;

    private Wechat instance = null;

    @Override
    public synchronized Wechat get()
    {
        if (instance == null)
        {
            WechatServiceProvider provider = new WechatServiceProvider(appid, secret);
            AccessGrant accessGrant = provider.getOAuthOperations().authenticateClient(null);
            instance = provider.getApi(accessGrant.getAccessToken());
        }

        return instance;
    }
}
