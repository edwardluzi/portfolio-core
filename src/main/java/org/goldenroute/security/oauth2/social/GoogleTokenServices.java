package org.goldenroute.security.oauth2.social;

import java.util.Map;

import org.goldenroute.security.oauth2.OAuth2TokenServices;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.UserAuthenticationConverter;

public class GoogleTokenServices extends OAuth2TokenServices
{
    @Override
    protected void transformNonStandardValuesToStandardValues(Map<String, Object> map)
    {
        map.put(AccessTokenConverter.CLIENT_ID, map.get("issued_to"));
        map.put(UserAuthenticationConverter.USERNAME, "G+" + map.get("user_id"));
    }
}