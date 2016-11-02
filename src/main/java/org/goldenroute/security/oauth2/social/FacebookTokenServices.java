package org.goldenroute.security.oauth2.social;

import java.util.Map;

import org.goldenroute.security.oauth2.OAuth2TokenServices;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.UserAuthenticationConverter;

public class FacebookTokenServices extends OAuth2TokenServices
{
    @Override
    protected void transformNonStandardValuesToStandardValues(Map<String, Object> map)
    {
        map.put(AccessTokenConverter.CLIENT_ID, getClientId());
        map.put(UserAuthenticationConverter.USERNAME, "FB" + map.get("id"));
    }
}
