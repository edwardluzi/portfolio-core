package org.goldenroute.security;

import java.util.Collection;
import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.acls.domain.AclImpl;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.GroupManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

public class DefaultPermissionManipulator implements PermissionManipulator
{
    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private MutableAclService mutableAclService;
    private TransactionTemplate transactionTemplate;
    private UserDetailsManager userDetailsManager;
    private GroupManager groupManager;
    private PasswordEncoder passwordEncoder = null;

    @Autowired
    public void setDataSource(final DataSource dataSource)
    {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Autowired
    public void setMutableAclService(final MutableAclService mutableAclService)
    {
        this.mutableAclService = mutableAclService;
    }

    @Autowired
    public void setPlatformTransactionManager(final PlatformTransactionManager platformTransactionManager)
    {
        this.transactionTemplate = new TransactionTemplate(platformTransactionManager);
    }

    @Autowired
    public void setUserDetailsManager(final UserDetailsManager userDetailsManager)
    {
        this.userDetailsManager = userDetailsManager;
    }

    @Autowired
    public void setGroupManager(final GroupManager groupManager)
    {
        this.groupManager = groupManager;
    }

    @Autowired
    public void setPasswordEncoder(final PasswordEncoder passwordEncoder)
    {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        Assert.notNull(dataSource, "dataSource required");
        Assert.notNull(jdbcTemplate, "dataSource required");
        Assert.notNull(mutableAclService, "mutableAclService required");
        Assert.notNull(transactionTemplate, "platformTransactionManager required");
        Assert.notNull(userDetailsManager, "userDetailsManager required");
        Assert.notNull(groupManager, "groupManager required");
        Assert.notNull(passwordEncoder, "passwordEncoder required");
    }

    @Override
    public void createObjectIdentityEntryFor(final Class<?> clazz, final Long entityId)
    {
        final ObjectIdentity objectIdentity = new ObjectIdentityImpl(clazz, entityId);

        transactionTemplate.execute(new TransactionCallback<MutableAcl>()
        {
            @Override
            public MutableAcl doInTransaction(final TransactionStatus transactionStatus)
            {
                MutableAcl mutableAcl = null;

                try
                {
                    mutableAcl = (MutableAcl) DefaultPermissionManipulator.this.mutableAclService
                            .readAclById(objectIdentity);
                }
                catch (NotFoundException nfe)
                {
                    mutableAcl = DefaultPermissionManipulator.this.mutableAclService.createAcl(objectIdentity);
                }

                return mutableAcl;
            }
        });
    }

    @Override
    public void grantPermissions(final Class<?> entityType, final Long entityId, final String recipientUsername,
            final Collection<Permission> permissions)
    {
        final AclImpl acl = (AclImpl) mutableAclService.readAclById(new ObjectIdentityImpl(entityType, entityId));

        for (Permission permission : permissions)
        {
            acl.insertAce(acl.getEntries().size(), permission, new PrincipalSid(recipientUsername), true);
        }
        updateAclInTransaction(acl);
    }

    protected void updateAclInTransaction(final MutableAcl acl)
    {
        transactionTemplate.execute(new TransactionCallback<MutableAcl>()
        {
            @Override
            public MutableAcl doInTransaction(final TransactionStatus transactionStatus)
            {
                return DefaultPermissionManipulator.this.mutableAclService.updateAcl(acl);
            }
        });
    }

    @Override
    public UserDetails createUser(String username, String password)
    {
        if (!userDetailsManager.userExists(username))
        {
            String encodedPassword = passwordEncoder.encode(password);
            UserDetails userDetails = new User(username, encodedPassword, true, true, true, true,
                    AuthorityUtils.NO_AUTHORITIES);
            userDetailsManager.createUser(userDetails);
            return userDetails;
        }
        else
        {
            return userDetailsManager.loadUserByUsername(username);
        }
    }

    @Override
    public void addUserToGroup(String username, String group)
    {
        groupManager.addUserToGroup(username, group);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(String username)
    {
        if (!userDetailsManager.userExists(username))
        {
            createUser(username, UUID.randomUUID().toString());
            addUserToGroup(username, GROUP_GUESTS);
        }

        return userDetailsManager.loadUserByUsername(username).getAuthorities();
    }
}
