package org.goldenroute.portfolio.service.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.goldenroute.cache.AccessTokenCache;
import org.goldenroute.portfolio.model.Account;
import org.goldenroute.portfolio.model.Groups;
import org.goldenroute.portfolio.model.Portfolio;
import org.goldenroute.portfolio.model.Profile;
import org.goldenroute.portfolio.repository.AccountRepository;
import org.goldenroute.portfolio.service.AccountService;
import org.goldenroute.portfolio.service.PortfolioService;
import org.goldenroute.portfolio.social.FacebookProfileCreator;
import org.goldenroute.portfolio.social.SocialProfileCreator;
import org.goldenroute.portfolio.social.WeiboProfileCreator;
import org.goldenroute.security.PermissionManipulator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.model.Permission;
import org.springframework.util.Assert;

public class AccountServiceImpl implements AccountService, InitializingBean
{
    @Autowired
    private AccessTokenCache accessTokenCache;

    @Autowired
    private PermissionManipulator permissionManipulator;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PortfolioService portfolioService;

    private HashMap<String, SocialProfileCreator> profileCreators = new HashMap<String, SocialProfileCreator>();

    @Override
    public void afterPropertiesSet() throws Exception
    {
        Assert.notNull(accessTokenCache, "accessTokenCache required");
        Assert.notNull(permissionManipulator, "permissionManipulator required");
        Assert.notNull(accountRepository, "accountRepository required");
        Assert.notNull(portfolioService, "portfolioService required");

        profileCreators.put("FB", new FacebookProfileCreator(accessTokenCache));
        profileCreators.put("WB", new WeiboProfileCreator(accessTokenCache));
    }

    @Override
    public List<Account> findAll()
    {
        List<Account> accounts = accountRepository.findAll();

        for (Account account : accounts)
        {
            decorate(account);
        }

        return accounts;
    }

    @Override
    public Account findOne(Long id)
    {
        return decorate(accountRepository.findOne(id));
    }

    @Override
    public Account findByUsername(String username)
    {
        return decorate(accountRepository.findByUsername(username));
    }

    @Override
    public Account create(String principal)
    {
        Account account = new Account(principal);
        Profile profile = null;

        String provider = principal.substring(0, 2);
        SocialProfileCreator profileCreator = profileCreators.containsKey(provider) ? profileCreators.get(provider)
                : null;

        if (profileCreator != null)
        {
            profile = profileCreator.create(principal);
        }
        else
        {
            profile = new Profile();
        }

        account.setProfile(profile);
        profile.setOwner(account);

        accountRepository.save(account);

        permissionManipulator.createObjectIdentityEntryFor(Account.class, account.getId());
        permissionManipulator.grantPermissions(
                Account.class,
                account.getId(),
                principal,
                Arrays.asList(new Permission[] { BasePermission.ADMINISTRATION, BasePermission.READ,
                        BasePermission.WRITE, BasePermission.DELETE }));

        permissionManipulator.createObjectIdentityEntryFor(Profile.class, profile.getId());
        permissionManipulator.grantPermissions(
                Profile.class,
                profile.getId(),
                principal,
                Arrays.asList(new Permission[] { BasePermission.ADMINISTRATION, BasePermission.READ,
                        BasePermission.WRITE, BasePermission.DELETE }));

        permissionManipulator.addUserToGroup(account.getUsername(), Groups.GROUP_USERS);

        return account;
    }

    @Override
    public void save(Account account)
    {
        accountRepository.save(account);
    }

    public Account decorate(Account account)
    {
        if (account == null)
        {
            return null;
        }

        List<Portfolio> portfolios = account.getPortfolios();

        for (Portfolio portfolio : portfolios)
        {
            portfolioService.addValue(portfolio);
        }

        return account;
    }
}
