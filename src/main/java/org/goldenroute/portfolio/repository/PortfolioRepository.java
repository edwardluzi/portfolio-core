package org.goldenroute.portfolio.repository;

import org.goldenroute.portfolio.model.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long>
{
}
