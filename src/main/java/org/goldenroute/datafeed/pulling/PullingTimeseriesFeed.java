package org.goldenroute.datafeed.pulling;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.goldenroute.DateTimeRange;
import org.goldenroute.Listener;
import org.goldenroute.datafeed.TimeseriesFeed;
import org.goldenroute.model.Bar;
import org.goldenroute.model.Intervals;
import org.goldenroute.repository.TimeseriesRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public abstract class PullingTimeseriesFeed implements TimeseriesFeed
{
    public PullingTimeseriesFeed()
    {
    }

    @Override
    public void setListener(Listener<List<Bar>> listener)
    {
        getScheduler().setListener(listener);
    }

    @Override
    public void setWorkingHours(List<DateTimeRange> workingHours)
    {
        getScheduler().setWorkingHours(workingHours);
    }

    @Override
    public void subscribe(Intervals intervals, Collection<String> symbols)
    {
        getScheduler().schedule(intervals, symbols);
    }

    @Override
    public void unsubscribe(Intervals intervals, Collection<String> symbols)
    {
        getScheduler().cancel(intervals, symbols);
    }

    @Override
    public void start()
    {
        getScheduler().start();
    }

    @Override
    public void stop()
    {
        getScheduler().stop();
    }

    @Override
    public Map<String, List<Bar>> snapshot(Intervals intervals, Collection<String> symbols)
    {
        Map<String, List<Bar>> map = new HashMap<>();

        for (String symbol : symbols)
        {
            List<Bar> bars = getTimeseriesRepository().findBySymbolAndIntervalsOrderByTimestampDesc(symbol, intervals);

            if (bars != null && bars.size() > 0)
            {
                map.put(symbol, bars);
            }
        }

        Set<String> remainder = new HashSet<String>(symbols);

        remainder.removeAll(map.keySet());

        if (remainder.size() > 0)
        {
            subscribe(intervals, remainder);
            map.putAll(getScheduler().request(intervals, remainder));
        }

        return map;
    }

    @Override
    public Map<String, List<Bar>> snapshot(Intervals intervals, Collection<String> symbols, int count)
    {
        Map<String, List<Bar>> map = new HashMap<>();

        for (String symbol : symbols)
        {
            Pageable top = new PageRequest(0, count);

            List<Bar> bars = getTimeseriesRepository().findBySymbolAndIntervalsOrderByTimestampDesc(symbol, intervals,
                    top);

            if (bars != null && bars.size() > 0)
            {
                map.put(symbol, bars);
            }
        }

        Set<String> remainder = new HashSet<String>(symbols);

        remainder.removeAll(map.keySet());

        if (remainder.size() > 0)
        {
            subscribe(intervals, remainder);

            Map<String, List<Bar>> temp = getScheduler().request(intervals, remainder);

            for (List<Bar> bars : temp.values())
            {
                if (bars.size() > count)
                {
                    bars.subList(count, bars.size() - 1).clear();
                }
            }

            map.putAll(temp);
        }

        return map;
    }

    protected abstract TimeseriesScheduler getScheduler();

    protected abstract TimeseriesRepository getTimeseriesRepository();
}
