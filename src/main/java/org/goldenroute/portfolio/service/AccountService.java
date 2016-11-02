package org.goldenroute.portfolio.service;

import java.util.List;

import org.goldenroute.portfolio.model.Account;

public interface AccountService
{
    List<Account> findAll();

    Account findOne(Long id);

    Account findByUsername(String username);

    Account create(String username);

    void save(Account account);
}
