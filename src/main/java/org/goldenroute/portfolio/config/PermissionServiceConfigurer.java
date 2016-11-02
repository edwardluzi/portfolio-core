package org.goldenroute.portfolio.config;

import javax.sql.DataSource;

import org.goldenroute.security.DefaultPermissionManipulator;
import org.goldenroute.security.PermissionManipulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.acls.AclPermissionEvaluator;
import org.springframework.security.acls.domain.AclAuthorizationStrategy;
import org.springframework.security.acls.domain.AclAuthorizationStrategyImpl;
import org.springframework.security.acls.domain.ConsoleAuditLogger;
import org.springframework.security.acls.domain.DefaultPermissionGrantingStrategy;
import org.springframework.security.acls.domain.EhCacheBasedAclCache;
import org.springframework.security.acls.jdbc.BasicLookupStrategy;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.AclCache;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.PermissionGrantingStrategy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;

@Configuration
public class PermissionServiceConfigurer
{
    @Autowired
    private DataSource dataSource;

    @Bean
    public PermissionManipulator aclObjectManipulator()
    {
        return new DefaultPermissionManipulator();
    }

    @Bean
    public MutableAclService aclService()
    {
        JdbcMutableAclService aclService = new JdbcMutableAclService(dataSource, lookupStrategy(), aclCache());

        aclService.setClassIdentityQuery("SELECT @@IDENTITY");
        aclService.setSidIdentityQuery("SELECT @@IDENTITY");

        return aclService;
    }

    @Bean
    public AclCache aclCache()
    {
        return new EhCacheBasedAclCache(ehCacheFactoryBean().getObject(), permissionGrantingStrategy(),
                aclAuthorizationStrategy("ROLE_ACL_ADMIN"));
    }

    @Bean
    public LookupStrategy lookupStrategy()
    {
        return new BasicLookupStrategy(dataSource, aclCache(), aclAuthorizationStrategy("ROLE_ADMIN"),
                new ConsoleAuditLogger());
    }

    @Bean
    protected EhCacheFactoryBean ehCacheFactoryBean()
    {
        EhCacheFactoryBean factoryBean = new EhCacheFactoryBean();
        factoryBean.setName("aclCache");
        factoryBean.setCacheManager(ehCacheManagerFactoryBean().getObject());
        return factoryBean;
    }

    @Bean
    protected EhCacheManagerFactoryBean ehCacheManagerFactoryBean()
    {
        EhCacheManagerFactoryBean cacheManager = new EhCacheManagerFactoryBean();
        cacheManager.setConfigLocation(new ClassPathResource("ehcache.xml"));
        cacheManager.setShared(true);
        return cacheManager;
    }

    private PermissionGrantingStrategy permissionGrantingStrategy()
    {
        return new DefaultPermissionGrantingStrategy(new ConsoleAuditLogger());
    }

    private AclAuthorizationStrategy aclAuthorizationStrategy(String role)
    {
        return new AclAuthorizationStrategyImpl(new SimpleGrantedAuthority(role));
    }

    @Bean
    public JdbcUserDetailsManager userDetailsAndGroupManager()
    {
        JdbcUserDetailsManager userDetailsManager = new JdbcUserDetailsManager();
        userDetailsManager.setDataSource(dataSource);
        userDetailsManager.setEnableAuthorities(false);
        userDetailsManager.setEnableGroups(true);
        return userDetailsManager;
    }

    @Bean
    public PasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public PermissionEvaluator permissionEvaluator()
    {
        return new AclPermissionEvaluator(aclService());
    }
}
