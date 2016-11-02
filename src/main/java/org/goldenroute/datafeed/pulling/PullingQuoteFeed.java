package org.goldenroute.datafeed.pulling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.goldenroute.DateTimeRange;
import org.goldenroute.Listener;
import org.goldenroute.datafeed.QuoteFeed;
import org.goldenroute.model.Quote;
import org.goldenroute.repository.QuoteRepository;

public abstract class PullingQuoteFeed implements QuoteFeed
{
    public PullingQuoteFeed()
    {
    }

    @Override
    public void setListener(Listener<List<Quote>> listener)
    {
        getScheduler().setListener(listener);
    }

    @Override
    public void setWorkingHours(List<DateTimeRange> workingHours)
    {
        getScheduler().setWorkingHours(workingHours);
    }

    @Override
    public void subscribe(Collection<String> symbols)
    {
        getScheduler().schedule(symbols);
    }

    @Override
    public void unsubscribe(Collection<String> symbols)
    {
        getScheduler().cancel(symbols);
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
    public Map<String, Quote> snapshot(Collection<String> symbols)
    {
        List<Quote> quotes = new ArrayList<>();

        for (String symbol : symbols)
        {
            Quote quote = getQuoteRepository().findFirstBySymbolOrderByTimestampDesc(symbol);

            if (quote != null)
            {
                quotes.add(quote);
            }
        }

        Map<String, Quote> map = quotes.stream().collect(Collectors.toMap(Quote::getSymbol, Function.identity()));

        Set<String> temp = new HashSet<String>(symbols);

        temp.removeAll(map.keySet());

        if (temp.size() > 0)
        {
            subscribe(temp);

            quotes = getScheduler().request(temp);

            for (Quote quote : quotes)
            {
                map.put(quote.getSymbol(), quote);
            }
        }

        return map;
    }

    protected abstract RealtimeScheduler getScheduler();

    protected abstract QuoteRepository getQuoteRepository();
}
