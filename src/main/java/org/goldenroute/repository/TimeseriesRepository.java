package org.goldenroute.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.goldenroute.model.Bar;
import org.goldenroute.model.Intervals;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeseriesRepository extends JpaRepository<Bar, Long>
{
    List<Bar> findBySymbolAndIntervalsOrderByTimestampDesc(String symbol, Intervals intervals);

    List<Bar> findBySymbolAndIntervalsOrderByTimestampDesc(String symbol, Intervals intervals, Pageable pageable);

    @Transactional
    void deleteBySymbolAndIntervals(String symbol, Intervals intervals);
}