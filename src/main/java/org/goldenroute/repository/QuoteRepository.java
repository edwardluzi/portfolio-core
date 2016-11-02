package org.goldenroute.repository;

import org.goldenroute.model.Quote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuoteRepository extends JpaRepository<Quote, Long>
{
    Quote findFirstBySymbolOrderByTimestampDesc(String symbol);
}
