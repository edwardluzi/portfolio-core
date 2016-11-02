package org.goldenroute.datafeed;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.goldenroute.DateTimeRange;
import org.goldenroute.Listener;
import org.goldenroute.model.Bar;
import org.goldenroute.model.Intervals;

public interface TimeseriesFeed
{
    void setListener(Listener<List<Bar>> listener);

    void setWorkingHours(List<DateTimeRange> workingHours);

    void subscribe(Intervals intervals, Collection<String> symbols);

    void unsubscribe(Intervals intervals, Collection<String> symbols);

    void start();

    void stop();

    Map<String, List<Bar>> snapshot(Intervals intervals, Collection<String> symbols);

    Map<String, List<Bar>> snapshot(Intervals intervals, Collection<String> symbols, int count);
}
