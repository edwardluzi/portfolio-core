package org.goldenroute.cache;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.core.Authentication;

public interface AccessTokenCache extends InitializingBean
{
    String findTokenByName(String username);

    Authentication findAuthenticationByToken(String token);

    void addAuthentication(String token, Authentication authentication);
}
