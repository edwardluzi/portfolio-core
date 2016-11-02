package org.goldenroute.portfolio.social;

import org.goldenroute.cache.AccessTokenCache;
import org.goldenroute.portfolio.model.Profile;
import org.springframework.social.weibo.api.Weibo;
import org.springframework.social.weibo.api.WeiboProfile;
import org.springframework.social.weibo.api.impl.WeiboTemplate;

public class WeiboProfileCreator implements SocialProfileCreator
{
    private AccessTokenCache accessTokenCache;

    public WeiboProfileCreator(AccessTokenCache accessTokenCache)
    {
        this.accessTokenCache = accessTokenCache;
    }

    @Override
    public Profile create(String principal)
    {
        String token = accessTokenCache.findTokenByName(principal);
        Weibo weibo = new WeiboTemplate(token);
        WeiboProfile user = weibo.profileOperations().getUserProfileById(Long.parseLong(principal.substring(2)));

        Profile profile = new Profile();

        profile.setScreenName(user.getScreenName());
        profile.setGender(user.getGender());
        profile.setEmail("");
        profile.setAvatarUrl(user.getAvatarHd());
        profile.setLocation(user.getLocation());

        return profile;
    }
}
