package org.goldenroute.portfolio.social;

import org.goldenroute.cache.AccessTokenCache;
import org.goldenroute.portfolio.model.Profile;
import org.springframework.social.facebook.api.CoverPhoto;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.Reference;
import org.springframework.social.facebook.api.User;
import org.springframework.social.facebook.api.impl.FacebookTemplate;

public class FacebookProfileCreator implements SocialProfileCreator
{
    private AccessTokenCache accessTokenCache;

    public FacebookProfileCreator(AccessTokenCache accessTokenCache)
    {
        this.accessTokenCache = accessTokenCache;
    }

    @Override
    public Profile create(String principal)
    {
        String token = accessTokenCache.findTokenByName(principal);
        Facebook facebook = new FacebookTemplate(token);
        User user = facebook.userOperations().getUserProfile();
        Profile profile = new Profile();
        profile.setScreenName(user.getName());
        profile.setGender(user.getGender());
        profile.setEmail(user.getEmail());
        CoverPhoto photo = user.getCover();
        if (photo != null)
        {
            profile.setAvatarUrl(user.getCover().getSource());
        }
        Reference location = user.getLocation();
        if (location != null)
        {
            profile.setLocation(user.getLocation().toString());
        }
        return profile;
    }
}
