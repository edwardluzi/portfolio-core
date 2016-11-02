package org.goldenroute.portfolio.repository;

import org.goldenroute.portfolio.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long>
{
    Account findByUsername(String username);
}