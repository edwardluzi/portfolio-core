package org.goldenroute.security.oauth2;

import java.util.List;

import org.apache.log4j.Logger;
import org.goldenroute.cache.AccessTokenCache;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;

public class OAuth2ProviderManager extends ProviderManager implements InitializingBean
{
    private static final Logger logger = Logger.getLogger(OAuth2ProviderManager.class);

    @Autowired
    private AccessTokenCache accessTokenCache;

    public OAuth2ProviderManager(List<AuthenticationProvider> providers)
    {
        super(providers);
    }

    public OAuth2ProviderManager(List<AuthenticationProvider> providers, AuthenticationManager parent)
    {
        super(providers, parent);
    }

    @Override
    public Authentication authenticate(Authentication authentication)
    {
        try
        {
            if (authentication.getDetails() instanceof OAuth2AuthenticationDetails)
            {
                OAuth2AuthenticationDetails detail = (OAuth2AuthenticationDetails) authentication.getDetails();
                String token = detail.getTokenValue();
                Authentication cachedAuth = accessTokenCache.findAuthenticationByToken(token);

                if (cachedAuth != null)
                {
                    return cachedAuth;
                }
                else
                {
                    Authentication auth = super.authenticate(authentication);

                    if (auth.isAuthenticated())
                    {
                        accessTokenCache.addAuthentication(token, auth);
                    }

                    return auth;
                }
            }
            else
            {
                return super.authenticate(authentication);
            }

        }
        catch (AuthenticationException e)
        {
            logger.error(e);
        }

        authentication.setAuthenticated(false);

        return authentication;
    }
}
