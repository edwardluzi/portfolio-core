package org.goldenroute.portfolio.repository;

import java.util.List;

import org.goldenroute.portfolio.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TransactionRepository extends JpaRepository<Transaction, Long>
{
    public static final String FIND_DISTINCT_TICKERS = "SELECT DISTINCT ticker FROM Transaction";

    @Query(value = FIND_DISTINCT_TICKERS, nativeQuery = true)
    public List<String> findDistinctTickers();
}
