package org.goldenroute.datafeed.pulling.yahoo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.log4j.Logger;
import org.goldenroute.datafeed.pulling.FetchingJob;
import org.goldenroute.datafeed.pulling.RealtimeScheduler;
import org.goldenroute.model.Quote;
import org.goldenroute.repository.QuoteRepository;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.core.env.Environment;
import org.springframework.social.yahoo.api.DetailQuote;
import org.springframework.social.yahoo.api.Yahoo;
import org.springframework.social.yahoo.api.impl.YahooTemplate;

public class YahooQuoteScheduler extends RealtimeScheduler
{
    private static final Logger logger = Logger.getLogger(YahooQuoteScheduler.class);

    private Yahoo yahoo;
    private QuoteRepository quoteRepository;

    public YahooQuoteScheduler(QuoteRepository quoteRepository, Environment environment)
    {
        String consumerKey = environment.getProperty("social.yahoo.consumerKey");
        String consumerSecret = environment.getProperty("social.yahoo.consumerSecret");

        this.quoteRepository = quoteRepository;
        this.yahoo = new YahooTemplate(consumerKey, consumerSecret, "", "");
    }

    @Override
    protected void execJob(JobExecutionContext context)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                doRequest();
            }
        }).start();
    }

    protected void doRequest()
    {
        List<String> list = new ArrayList<String>(getSymbols());

        int[] indexes = IntStream.range(0, list.size()).filter(i -> i % 99 == 0).toArray();

        List<List<String>> subSets = IntStream.range(0, indexes.length)
                .mapToObj(i -> list.subList(indexes[i] + 0, i < indexes.length - 1 ? indexes[i + 1] - 1 : list.size()))
                .collect(Collectors.toList());

        for (List<String> symbols : subSets)
        {
            request(symbols);
        }

        if (afterMarketClose(null))
        {
            notifyEndOfStream();
        }
    }

    @Override
    public List<Quote> request(Collection<String> symbols)
    {
        List<Quote> standardQuotes = new ArrayList<Quote>();

        Collection<String> yahooSymbols = YahooSymbolConverter.getInstance().toLocal(symbols);

        List<DetailQuote.Quote> yahooQuotes = yahoo.detailQuoteOperations().getQuotes(yahooSymbols);

        if (yahooQuotes != null)
        {
            convert(yahooQuotes, standardQuotes);

            if (getListener() != null)
            {
                getListener().update(standardQuotes);
            }

            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    quoteRepository.save(standardQuotes);
                }
            }).start();
        }

        return standardQuotes;
    }

    private void convert(List<DetailQuote.Quote> yahooQuotes, List<Quote> standardQuotes)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("M/d/y");

        for (DetailQuote.Quote yahoo : yahooQuotes)
        {
            String tradeDate = yahoo.getLastTradeDate();

            if (tradeDate == null || tradeDate.length() == 0)
            {
                continue;
            }

            Long time = null;

            try
            {
                time = dateFormat.parse(tradeDate).getTime();
            }
            catch (ParseException e)
            {
                logger.error(e);
                continue;
            }

            standardQuotes.add(new Quote(YahooSymbolConverter.getInstance().fromLocal(yahoo.getSymbol()), time, yahoo
                    .getLastTradePriceOnly(), yahoo.getVolume()));
        }
    }

    @Override
    protected int getDelaySeconds(String category)
    {
        return 30;
    }

    @Override
    protected Class<? extends Job> getJobClass()
    {
        return YahooQuoteFetchJob.class;
    }

    @Override
    protected String getContextName()
    {
        return YahooQuoteScheduler.class.getSimpleName() + ".THIS";
    }

    public static class YahooQuoteFetchJob extends FetchingJob
    {
        @Override
        public String getContextName()
        {
            return YahooQuoteScheduler.class.getSimpleName() + ".THIS";
        }
    }
}
