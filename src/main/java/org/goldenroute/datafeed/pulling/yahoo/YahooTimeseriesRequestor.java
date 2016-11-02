package org.goldenroute.datafeed.pulling.yahoo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.goldenroute.datafeed.pulling.TimeseriesScheduler;
import org.goldenroute.model.Bar;
import org.goldenroute.model.Intervals;
import org.goldenroute.repository.TimeseriesRepository;
import org.goldenroute.util.CalendarUtil;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.social.yahoo.api.Timeseries;
import org.springframework.social.yahoo.api.Yahoo;
import org.springframework.social.yahoo.api.impl.YahooTemplate;

public class YahooTimeseriesRequestor
{
    private static final Logger logger = Logger.getLogger(YahooTimeseriesRequestor.class);
    private static final Set<String> symbolsInProcess = ConcurrentHashMap.newKeySet();;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private int startYear = 2007;

    private TimeseriesRepository timeseriesRepository;
    private Yahoo yahoo;

    private TimeseriesScheduler timeseriesScheduler;

    public YahooTimeseriesRequestor(TimeseriesScheduler timeseriesScheduler, TimeseriesRepository timeseriesRepository,
            Environment environment)
    {

        String consumerKey = environment.getProperty("social.yahoo.consumerKey");
        String consumerSecret = environment.getProperty("social.yahoo.consumerSecret");

        this.timeseriesScheduler = timeseriesScheduler;
        this.timeseriesRepository = timeseriesRepository;
        this.yahoo = new YahooTemplate(consumerKey, consumerSecret, "", "");
        this.startYear = Integer.parseInt(environment.getProperty("alpha.timeseries.startYear"));
    }

    public Map<String, List<Bar>> request(Intervals intervals, Collection<String> symbols)
    {
        if (intervals != Intervals.Daily)
        {
            throw new IllegalArgumentException("intervals");
        }

        Map<String, List<Bar>> map = new HashMap<>();

        Calendar to = CalendarUtil.normalize(Calendar.getInstance(), Calendar.DAY_OF_MONTH);

        for (String symbol : symbols)
        {
            if (symbolsInProcess.contains(symbol))
            {
                continue;
            }

            symbolsInProcess.add(symbol);

            try
            {
                List<Bar> lasts = findLastTwoBars(symbol);
                Calendar from = calculateFromTime(lasts);

                List<Timeseries.Quote> quotes = yahoo.timeseriesOperations().getTimeseries(
                        YahooSymbolConverter.getInstance().toLocal(symbol), from, to);

                if (quotes != null)
                {
                    List<Bar> bars = new ArrayList<Bar>();

                    if (!convert(quotes, bars, lasts))
                    {
                        bars = renew(intervals, symbol, to, lasts);
                    }
                    else
                    {
                        persist(bars);
                    }

                    if (timeseriesScheduler.getListener() != null)
                    {
                        timeseriesScheduler.getListener().update(bars);
                    }

                    map.put(symbol, bars);
                }
            }
            catch (Exception e)
            {
                logger.debug(e);
            }

            symbolsInProcess.remove(symbol);
        }

        return map;
    }

    private List<Bar> renew(Intervals intervals, String symbol, Calendar to, List<Bar> lasts)
    {
        Calendar from = calculateFromTime(null);

        List<Timeseries.Quote> quotes = yahoo.timeseriesOperations().getTimeseries(
                YahooSymbolConverter.getInstance().toLocal(symbol), from, to);

        List<Bar> all = new ArrayList<Bar>();
        convert(quotes, all, null);

        new Thread(new Runnable()
        {
            private List<Bar> all;

            @Override
            public void run()
            {
                logger.debug("delete all " + symbol);

                timeseriesRepository.deleteBySymbolAndIntervals(symbol, intervals);

                logger.debug("persist " + Integer.toString(all.size()));

                if (Level.DEBUG.isGreaterOrEqual(logger.getEffectiveLevel()))
                {
                    for (Bar bar : all)
                    {
                        logger.debug("persist " + bar.toString());
                    }
                }

                timeseriesRepository.save(all);
            }

            public Runnable initialize(List<Bar> all)
            {
                this.all = new ArrayList<>(all);
                return (this);
            }
        }.initialize(all)).start();

        List<Bar> bars = new ArrayList<>();
        Long baseline = lasts != null && lasts.size() > 0 ? lasts.get(0).getTimestamp() : null;

        if (baseline != null)
        {
            for (Bar bar : all)
            {
                if (bar.getTimestamp() <= baseline)
                {
                    continue;
                }

                bars.add(bar);
            }
        }
        else
        {
            bars.addAll(all);
        }

        return bars;
    }

    private void persist(List<Bar> bars)
    {
        new Thread(new Runnable()
        {
            private List<Bar> bars;

            @Override
            public void run()
            {
                logger.debug("persist " + Integer.toString(bars.size()));

                if (Level.DEBUG.isGreaterOrEqual(logger.getEffectiveLevel()))
                {
                    for (Bar bar : bars)
                    {
                        logger.debug("persist " + bar.toString());
                    }
                }

                try
                {
                    timeseriesRepository.save(bars);
                }
                catch (Exception e)
                {
                    logger.error(e.getMessage(), e);
                }
            }

            public Runnable initialize(List<Bar> bars)
            {
                this.bars = new ArrayList<>(bars);
                return (this);
            }

        }.initialize(bars)).start();
    }

    private boolean convert(List<Timeseries.Quote> quotes, List<Bar> bars, List<Bar> lasts)
    {
        Long baseline = lasts != null && lasts.size() > 0 ? lasts.get(0).getTimestamp() : null;

        ListIterator<Timeseries.Quote> li = quotes.listIterator(quotes.size());

        while (li.hasPrevious())
        {
            Timeseries.Quote quote = li.previous();

            Long date = null;

            try
            {
                date = dateFormat.parse(quote.getDate()).getTime();
            }
            catch (ParseException e)
            {
                logger.error(e);
            }

            if (date == null)
            {
                continue;
            }

            if (baseline != null)
            {
                int result = (int) (date - baseline);

                if (result < 0)
                {
                    continue;
                }
                else if (result == 0)
                {
                    if (quote.getClose() != lasts.get(0).getClose())
                    {
                        return false;
                    }

                    continue;
                }
            }

            bars.add(new Bar(YahooSymbolConverter.getInstance().fromLocal(quote.getSymbol()), Intervals.Daily, date,
                    quote.getOpen(), quote.getHigh(), quote.getLow(), quote.getClose(), quote.getVolume()));
        }

        return true;
    }

    private Calendar calculateFromTime(List<Bar> lasts)
    {
        Calendar from = null;

        if (lasts != null && lasts.size() == 2)
        {
            from = Calendar.getInstance();
            from.setTimeInMillis(lasts.get(1).getTimestamp());
        }
        else
        {
            from = CalendarUtil.normalize(Calendar.getInstance(), Calendar.DAY_OF_MONTH);
            from.set(startYear, 0, 1);
        }

        return from;
    }

    private List<Bar> findLastTwoBars(String symbol)
    {
        List<Bar> result = null;

        try
        {
            result = timeseriesRepository.findBySymbolAndIntervalsOrderByTimestampDesc(symbol, Intervals.Daily,
                    new PageRequest(0, 2));
        }
        catch (Exception e)
        {
            logger.debug(e);
        }

        return result;
    }
}