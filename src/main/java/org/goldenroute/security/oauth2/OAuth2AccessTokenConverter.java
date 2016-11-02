package org.goldenroute.security.oauth2;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter;
import org.springframework.security.oauth2.provider.token.UserAuthenticationConverter;

public class OAuth2AccessTokenConverter extends DefaultAccessTokenConverter
{
    private UserAuthenticationConverter userTokenConverter;

    public OAuth2AccessTokenConverter()
    {
        setUserTokenConverter(new DefaultUserAuthenticationConverter());
    }

    @Override
    public void setUserTokenConverter(UserAuthenticationConverter userTokenConverter)
    {
        this.userTokenConverter = userTokenConverter;
        super.setUserTokenConverter(userTokenConverter);
    }

    @Override
    public OAuth2Authentication extractAuthentication(Map<String, ?> map)
    {
        Authentication user = userTokenConverter.extractAuthentication(map);
        Set<String> scope = getScopes(map);
        Set<String> resourceIds = getAudiences(map);
        List<GrantedAuthority> authorities = user == null ? getAuthorities(map) : null;

        Map<String, String> parameters = new HashMap<>();
        String clientId = (String) map.get(CLIENT_ID);
        parameters.put(CLIENT_ID, clientId);

        OAuth2Request request = new OAuth2Request(parameters, clientId, authorities, true, scope, resourceIds, null,
                null, null);

        return new OAuth2Authentication(request, user);
    }

    protected Set<String> getScopes(Map<String, ?> map)
    {
        return getCollection(map, SCOPE, " ");
    }

    protected Set<String> getAudiences(Map<String, ?> map)
    {
        return getCollection(map, AUD, " ");
    }

    protected List<GrantedAuthority> getAuthorities(Map<String, ?> map)
    {
        if (map.containsKey(AUTHORITIES))
        {
            @SuppressWarnings("unchecked")
            String[] roles = ((Collection<String>) map.get(AUTHORITIES)).toArray(new String[0]);
            return AuthorityUtils.createAuthorityList(roles);
        }
        else
        {
            return null;
        }
    }

    protected Set<String> getCollection(Map<String, ?> map, String key, String separator)
    {
        Object setAsObject = map.containsKey(key) ? map.get(key) : "";
        Set<String> set = new LinkedHashSet<String>();

        if (String.class.isAssignableFrom(setAsObject.getClass()))
        {
            String setAsString = (String) setAsObject;
            Collections.addAll(set, setAsString.split(separator));
        }
        else if (Collection.class.isAssignableFrom(setAsObject.getClass()))
        {
            @SuppressWarnings("unchecked")
            Collection<String> temps = (Collection<String>) setAsObject;
            set.addAll(temps);
        }

        return set;
    }
}
