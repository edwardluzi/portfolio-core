package org.goldenroute.datafeed;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.goldenroute.DateTimeRange;
import org.goldenroute.Listener;
import org.goldenroute.model.Quote;

public interface QuoteFeed
{
    void setListener(Listener<List<Quote>> listener);

    void setWorkingHours(List<DateTimeRange> workingHours);

    void subscribe(Collection<String> symbols);

    void unsubscribe(Collection<String> symbols);

    void start();

    void stop();

    Map<String, Quote> snapshot(Collection<String> symbols);
}
