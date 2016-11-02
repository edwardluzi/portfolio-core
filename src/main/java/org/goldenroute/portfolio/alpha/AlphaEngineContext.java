package org.goldenroute.portfolio.alpha;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.goldenroute.DateTimeRange;
import org.goldenroute.model.Intervals;

public interface AlphaEngineContext
{
    void load();

    List<DateTimeRange> getWorkingHours();

    Set<Calendar> getHolidays();

    Set<String> getSymbols();

    Map<Intervals, Set<String>> getSymbolTable();
}
