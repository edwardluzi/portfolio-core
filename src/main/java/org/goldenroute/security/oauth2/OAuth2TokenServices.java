package org.goldenroute.security.oauth2;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.goldenroute.security.PermissionManipulator;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.oauth2.provider.token.UserAuthenticationConverter;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public class OAuth2TokenServices implements ResourceServerTokenServices
{
    private static final Logger logger = Logger.getLogger(OAuth2TokenServices.class);

    private RestOperations restTemplate;
    private String checkTokenEndpointUrl;
    private HttpMethod checkTokenMethod;

    private String clientId;
    private String clientSecret;

    private AccessTokenConverter tokenConverter;
    private PermissionManipulator permissionManipulator;

    public OAuth2TokenServices()
    {
        restTemplate = createDefaultRestTemplate();
    }

    public void setRestTemplate(RestOperations restTemplate)
    {
        this.restTemplate = restTemplate;
    }

    public void setCheckTokenEndpointUrl(String checkTokenEndpointUrl)
    {
        this.checkTokenEndpointUrl = checkTokenEndpointUrl;
    }

    protected String getCheckTokenEndpointUrl()
    {
        return checkTokenEndpointUrl;
    }

    public void setCheckTokenMethod(HttpMethod checkTokenMethod)
    {
        this.checkTokenMethod = checkTokenMethod;
    }

    protected HttpMethod getCheckTokenMethod()
    {
        return checkTokenMethod;
    }

    public void setClientId(String clientId)
    {
        this.clientId = clientId;
    }

    protected String getClientId()
    {
        return clientId;
    }

    public void setClientSecret(String clientSecret)
    {
        this.clientSecret = clientSecret;
    }

    protected String getClientSecret()
    {
        return clientSecret;
    }

    public void setAccessTokenConverter(AccessTokenConverter accessTokenConverter)
    {
        this.tokenConverter = accessTokenConverter;
    }

    public void setPermissionManipulator(PermissionManipulator permissionManipulator)
    {
        this.permissionManipulator = permissionManipulator;
    }

    @Override
    public OAuth2Authentication loadAuthentication(String accessToken) throws AuthenticationException,
            InvalidTokenException
    {
        Map<String, Object> checkTokenResponse = checkToken(accessToken);

        if (checkTokenResponse == null)
        {
            return null;
        }

        if (checkTokenResponse.containsKey("error"))
        {
            logger.info("check_token returned error: " + checkTokenResponse.get("error"));
            return null;
        }

        transformNonStandardValuesToStandardValues(checkTokenResponse);

        Assert.state(checkTokenResponse.containsKey(AccessTokenConverter.CLIENT_ID),
                "Client id must be present in response from auth server");
        Assert.state(checkTokenResponse.containsKey(UserAuthenticationConverter.USERNAME),
                "Username must be present in response from auth server");

        populateAuthorities(checkTokenResponse);

        return tokenConverter.extractAuthentication(checkTokenResponse);
    }

    @Override
    public OAuth2AccessToken readAccessToken(String accessToken)
    {
        throw new UnsupportedOperationException("Not supported: read access token");
    }

    protected void transformNonStandardValuesToStandardValues(Map<String, Object> map)
    {
    }

    protected Map<String, Object> checkToken(String accessToken)
    {
        MultiValueMap<String, String> formData = null;
        String path = checkTokenEndpointUrl;

        if (checkTokenMethod == HttpMethod.GET)
        {
            path = UriComponentsBuilder.fromHttpUrl(path).queryParam("access_token", accessToken).build().toUriString();
        }
        else
        {
            formData = new LinkedMultiValueMap<>();
            formData.add("access_token", accessToken);
        }

        HttpHeaders headers = createHttpHeaders();

        return exchangeForMap(path, formData, headers);
    }

    protected HttpHeaders createHttpHeaders()
    {
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Arrays.asList(new MediaType[] { MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE) }));

        return headers;
    }

    private Map<String, Object> exchangeForMap(String path, MultiValueMap<String, String> formData, HttpHeaders headers)
    {
        if (headers != null && headers.getContentType() == null)
        {
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setAccept(Arrays.asList(new MediaType[] { MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE) }));
        }

        String responseText = null;

        try
        {
            responseText = restTemplate.exchange(path, checkTokenMethod, new HttpEntity<>(formData, headers),
                    String.class).getBody();
        }
        catch (RestClientException e)
        {
            logger.error(e);
        }

        if (!StringUtils.isEmpty(responseText))
        {
            try
            {
                return new ObjectMapper().readValue(responseText, new TypeReference<HashMap<String, String>>()
                {
                });
            }
            catch (IOException e)
            {
                logger.info(e.getMessage());
            }
        }

        return null;
    }

    private void populateAuthorities(Map<String, Object> map)
    {
        String username = map.get(UserAuthenticationConverter.USERNAME).toString();
        Collection<? extends GrantedAuthority> userAuthorities = permissionManipulator.getAuthorities(username);
        Set<String> userAuthoritiesSet = AuthorityUtils.authorityListToSet(userAuthorities);
        String userAuthoritiesString = StringUtils.collectionToCommaDelimitedString(userAuthoritiesSet);
        map.put(UserAuthenticationConverter.AUTHORITIES, userAuthoritiesString);
    }

    private RestOperations createDefaultRestTemplate()
    {
        RestTemplate template = new RestTemplate();

        template.setErrorHandler(new DefaultResponseErrorHandler()
        {
            @Override
            public void handleError(ClientHttpResponse response) throws IOException
            {
            }
        });

        return template;
    }
}
