package org.goldenroute.datafeed.pulling.sina;

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
import org.goldenroute.sinafinance.SinaFinance;
import org.goldenroute.sinafinance.impl.SinaFinanceTemplate;
import org.goldenroute.util.FloatUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

public class SinaQuoteScheduler extends RealtimeScheduler
{
    private static final Logger logger = Logger.getLogger(SinaQuoteScheduler.class);

    private SinaFinance sinaFinance;
    private QuoteRepository quoteRepository;

    public SinaQuoteScheduler(QuoteRepository quoteRepository)
    {
        this.quoteRepository = quoteRepository;
        this.sinaFinance = new SinaFinanceTemplate();
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

        Collection<String> sinaSymbols = SinaSymbolConverter.getInstance().toLocal(symbols);

        List<org.goldenroute.sinafinance.Quote> sinaQuotes = sinaFinance.quoteOperations().getQuotes(sinaSymbols);

        if (sinaQuotes != null)
        {
            convert(sinaQuotes, standardQuotes);

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

    private void convert(List<org.goldenroute.sinafinance.Quote> sinaQuotes, List<Quote> standardQuotes)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (org.goldenroute.sinafinance.Quote sina : sinaQuotes)
        {
            String tradeDate = sina.getTimestamp();

            if (tradeDate == null || tradeDate.length() == 0)
            {
                continue;
            }

            logger.debug(sina.toString());

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

            String symbol = SinaSymbolConverter.getInstance().fromLocal(sina.getSymbol());

            if (!FloatUtil.equal(sina.getPrice(), 0, FloatUtil.Precision.P4))
            {
                standardQuotes.add(new Quote(symbol, time, sina.getPrice(), sina.getVolume()));
            }
            else if (!FloatUtil.equal(sina.getHistoryClose(), 0, FloatUtil.Precision.P4))
            {
                Quote quote = null;

                try
                {
                    quote = quoteRepository.findFirstBySymbolOrderByTimestampDesc(symbol);
                }
                catch (Exception e)
                {
                    logger.error(e);
                }

                if (quote == null)
                {
                    standardQuotes.add(new Quote(symbol, time, sina.getHistoryClose(), sina.getVolume()));
                }
            }
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
        return SinaQuoteFetchJob.class;
    }

    @Override
    protected String getContextName()
    {
        return SinaQuoteScheduler.class.getSimpleName() + ".THIS";
    }

    public static class SinaQuoteFetchJob extends FetchingJob
    {
        @Override
        public String getContextName()
        {
            return SinaQuoteScheduler.class.getSimpleName() + ".THIS";
        }
    }
}
