package org.goldenroute.security;

import java.util.Collection;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public interface PermissionManipulator extends InitializingBean
{
    public static final String GROUP_GUESTS = "Guests";

    void createObjectIdentityEntryFor(final Class<?> clazz, final Long entityId);

    void grantPermissions(final Class<?> entityType, final Long entityId, final String recipientUsername,
            final Collection<Permission> permissions);

    UserDetails createUser(String username, String password);

    void addUserToGroup(String username, String group);

    Collection<? extends GrantedAuthority> getAuthorities(String username);
}
