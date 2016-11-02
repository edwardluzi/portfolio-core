package org.goldenroute.security.oauth2;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.util.Assert;

public class OAuth2AuhtencationProvider implements AuthenticationProvider, InitializingBean
{
    private ResourceServerTokenServices tokenServices;

    public void setResourceServerTokenServices(ResourceServerTokenServices tokenServices)
    {
        this.tokenServices = tokenServices;
    }

    @Override
    public void afterPropertiesSet()
    {
        Assert.state(tokenServices != null, "TokenServices are required");
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException
    {
        if (authentication == null)
        {
            throw new InvalidTokenException("Invalid token (token not found)");
        }

        if (!supports(authentication.getClass()))
        {
            return null;
        }

        OAuth2Authentication oauth2Authentication = tokenServices.loadAuthentication(authentication.getPrincipal()
                .toString());

        if (oauth2Authentication == null)
        {
            return null;
        }

        if (authentication.getDetails() instanceof OAuth2AuthenticationDetails)
        {
            OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) authentication.getDetails();

            // Guard against a cached copy of the same details
            if (!details.equals(oauth2Authentication.getDetails()))
            {
                // Preserve the authentication details from the one loaded by
                // token services
                details.setDecodedDetails(oauth2Authentication.getDetails());
            }
        }

        oauth2Authentication.setDetails(authentication.getDetails());
        oauth2Authentication.setAuthenticated(true);

        return oauth2Authentication;
    }

    @Override
    public boolean supports(Class<?> authentication)
    {
        return PreAuthenticatedAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
