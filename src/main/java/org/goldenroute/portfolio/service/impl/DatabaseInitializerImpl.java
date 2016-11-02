package org.goldenroute.portfolio.service.impl;

import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.goldenroute.portfolio.model.Groups;
import org.goldenroute.portfolio.service.DatabaseInitializer;
import org.goldenroute.security.PermissionManipulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.provisioning.GroupManager;
import org.springframework.util.Assert;

public class DatabaseInitializerImpl implements DatabaseInitializer
{
    private DataSource dataSource;

    private GroupManager groupManager;

    @Autowired
    public void setDataSource(final DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    @Autowired
    public void setGroupManager(final GroupManager groupManager)
    {
        this.groupManager = groupManager;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        Assert.notNull(groupManager, "groupManager required");
        Assert.notNull(dataSource, "dataSource required");
    }

    @Override
    public void config()
    {
        populateDatabase();
        createDefaultGroup();
    }

    private void populateDatabase()
    {
        new ResourceDatabasePopulator(new ClassPathResource("acl-schema.sql")).execute(dataSource);

        new ResourceDatabasePopulator(new ClassPathResource("portfolio-schema.sql")).execute(dataSource);
    }

    private void createDefaultGroup()
    {
        List<String> allGroups = groupManager.findAllGroups();

        if (!allGroups.contains(Groups.GROUP_ADMINISTRATORS))
        {
            groupManager.createGroup(Groups.GROUP_ADMINISTRATORS,
                    Arrays.asList(new GrantedAuthority[] { new SimpleGrantedAuthority("ROLE_ADMIN") }));
        }
        if (!allGroups.contains(Groups.GROUP_USERS))
        {
            groupManager.createGroup(Groups.GROUP_USERS,
                    Arrays.asList(new GrantedAuthority[] { new SimpleGrantedAuthority("ROLE_USER") }));
        }

        if (!allGroups.contains(PermissionManipulator.GROUP_GUESTS))
        {
            groupManager.createGroup(PermissionManipulator.GROUP_GUESTS,
                    Arrays.asList(new GrantedAuthority[] { new SimpleGrantedAuthority("ROLE_GUEST") }));
        }
    }
}
