package org.goldenroute.portfolio.config;

import java.util.Arrays;

import org.goldenroute.security.PermissionManipulator;
import org.goldenroute.security.oauth2.OAuth2AccessTokenConverter;
import org.goldenroute.security.oauth2.OAuth2AuhtencationProvider;
import org.goldenroute.security.oauth2.OAuth2ProviderManager;
import org.goldenroute.security.oauth2.social.FacebookTokenServices;
import org.goldenroute.security.oauth2.social.GoogleTokenServices;
import org.goldenroute.security.oauth2.social.WeiboTokenServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;

@Configuration
public class OAuth2ProviderManagerConfigurer
{
    @Autowired
    private Environment environment;

    @Autowired
    private PermissionManipulator permissionManipulator;

    @Bean
    public OAuth2ProviderManager providerManager()
    {
        return new OAuth2ProviderManager(Arrays.asList(weiboAuhtencationProvider(), facebookAuhtencationProvider(),
                googleAuhtencationProvider()));
    }

    private OAuth2AuhtencationProvider googleAuhtencationProvider()
    {
        OAuth2AuhtencationProvider authenticationProvider = new OAuth2AuhtencationProvider();
        authenticationProvider.setResourceServerTokenServices(googleTokenService());
        return authenticationProvider;
    }

    private OAuth2AuhtencationProvider weiboAuhtencationProvider()
    {
        OAuth2AuhtencationProvider authenticationProvider = new OAuth2AuhtencationProvider();
        authenticationProvider.setResourceServerTokenServices(weiboTokenService());
        return authenticationProvider;
    }

    private OAuth2AuhtencationProvider facebookAuhtencationProvider()
    {
        OAuth2AuhtencationProvider authenticationProvider = new OAuth2AuhtencationProvider();
        authenticationProvider.setResourceServerTokenServices(facebookTokenService());
        return authenticationProvider;
    }

    private GoogleTokenServices googleTokenService()
    {
        GoogleTokenServices tokenServices = new GoogleTokenServices();
        tokenServices.setClientId(environment.getProperty("social.google.clientId"));
        tokenServices.setClientSecret(environment.getProperty("social.google.secret"));
        tokenServices.setCheckTokenEndpointUrl("https://www.googleapis.com/oauth2/v1/tokeninfo");
        tokenServices.setCheckTokenMethod(HttpMethod.POST);
        tokenServices.setAccessTokenConverter(accessTokenConverter());
        tokenServices.setPermissionManipulator(permissionManipulator);
        return tokenServices;
    }

    private WeiboTokenServices weiboTokenService()
    {
        WeiboTokenServices tokenServices = new WeiboTokenServices();
        tokenServices.setClientId(environment.getProperty("social.weibo.clientId"));
        tokenServices.setClientSecret(environment.getProperty("social.weibo.secret"));
        tokenServices.setCheckTokenEndpointUrl("https://api.weibo.com/oauth2/get_token_info");
        tokenServices.setCheckTokenMethod(HttpMethod.POST);
        tokenServices.setAccessTokenConverter(accessTokenConverter());
        tokenServices.setPermissionManipulator(permissionManipulator);
        return tokenServices;
    }

    private FacebookTokenServices facebookTokenService()
    {
        FacebookTokenServices tokenServices = new FacebookTokenServices();
        tokenServices.setClientId(environment.getProperty("social.facebook.clientId"));
        tokenServices.setClientSecret(environment.getProperty("social.facebook.secret"));
        tokenServices.setCheckTokenEndpointUrl("https://graph.facebook.com/me");
        tokenServices.setCheckTokenMethod(HttpMethod.GET);
        tokenServices.setAccessTokenConverter(accessTokenConverter());
        tokenServices.setPermissionManipulator(permissionManipulator);
        return tokenServices;
    }

    private AccessTokenConverter accessTokenConverter()
    {
        return new OAuth2AccessTokenConverter();
    }
}
