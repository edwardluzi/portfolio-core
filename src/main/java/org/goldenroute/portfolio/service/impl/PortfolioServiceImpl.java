package org.goldenroute.portfolio.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.goldenroute.datafeed.QuoteFeed;
import org.goldenroute.datafeed.TimeseriesFeed;
import org.goldenroute.model.Bar;
import org.goldenroute.model.Intervals;
import org.goldenroute.model.Quote;
import org.goldenroute.portfolio.model.Holding;
import org.goldenroute.portfolio.model.Portfolio;
import org.goldenroute.portfolio.model.Transaction;
import org.goldenroute.portfolio.repository.PortfolioRepository;
import org.goldenroute.portfolio.repository.TransactionRepository;
import org.goldenroute.portfolio.service.EfficientFrontier;
import org.goldenroute.portfolio.service.MarkowitzModel3;
import org.goldenroute.portfolio.service.MarkowitzPortfolio;
import org.goldenroute.portfolio.service.PortfolioReport;
import org.goldenroute.portfolio.service.PortfolioReportParameters;
import org.goldenroute.portfolio.service.PortfolioService;
import org.goldenroute.util.CalendarUtil;
import org.goldenroute.util.FloatUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class PortfolioServiceImpl implements PortfolioService
{
    private static final Logger logger = Logger.getLogger(PortfolioServiceImpl.class);
    private static final int defaultSampleCount = 100;

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private QuoteFeed quoteFeed;

    @Autowired
    private TimeseriesFeed timeseriesFeed;

    @Override
    public Portfolio findOne(Long id, boolean addingValue)
    {
        Portfolio portfolio = portfolioRepository.findOne(id);

        if (addingValue)
        {
            portfolio = addValue(portfolio);
        }

        return portfolio;
    }

    @Override
    public void save(Portfolio portfolio)
    {
        portfolioRepository.save(portfolio);
    }

    @Override
    public List<String> findDistinctSymbols()
    {
        return transactionRepository.findDistinctTickers();
    }

    @Override
    public Portfolio addValue(Portfolio portfolio)
    {
        if (portfolio == null)
        {
            return null;
        }

        Set<String> tickers = portfolio.getTickers();

        if (tickers.size() == 0)
        {
            return portfolio;
        }

        calculateHoldings(portfolio, tickers);

        calculateHoldingChanges(portfolio, tickers);

        calculatePortfolioChanges(portfolio);

        calculateHoldingWeights(portfolio);

        return portfolio;
    }

    private void calculateHoldingWeights(Portfolio portfolio)
    {
        BigDecimal total = portfolio.getValue();

        for (Holding holding : portfolio.getHoldings())
        {
            holding.setWeight(holding.getValue().divide(total, 5, RoundingMode.HALF_UP));
        }
    }

    private void calculatePortfolioChanges(Portfolio portfolio)
    {
        BigDecimal portfolioCost = new BigDecimal(0);
        BigDecimal portfolioValue = new BigDecimal(0);
        BigDecimal portfolioDailyChange = new BigDecimal(0);

        for (Holding holding : portfolio.getHoldings())
        {
            portfolioCost = portfolioCost.add(holding.getCost());
            portfolioValue = portfolioValue.add(holding.getValue());
            portfolioDailyChange = portfolioDailyChange.add(holding.getDailyChange());
        }

        portfolio.setCost(portfolioCost);
        portfolio.setValue(portfolioValue);

        portfolio.setDailyChange(portfolioDailyChange);
        portfolio.setDailyChangePercentage(portfolioDailyChange.divide(portfolioValue.subtract(portfolioDailyChange),
                5, RoundingMode.HALF_UP));

        portfolio.setTotalChange(portfolioValue.subtract(portfolioCost));
        portfolio.setTotalChangePercentage(portfolio.getTotalChange().divide(portfolioCost, 5, RoundingMode.HALF_UP));
    }

    @Override
    public PortfolioReport generateReport(Portfolio portfolio, PortfolioReportParameters parameters)
    {
        List<String> symbols = new ArrayList<>(portfolio.getTickers());

        double[][] returns = loadHistoryReturns(symbols, parameters.getIntervals(), parameters.getSampleCount());

        if (Level.DEBUG.isGreaterOrEqual(getLevel(logger)))
        {
            for (int index = 0; index < symbols.size() && index < returns.length; index++)
            {
                logger.debug(symbols.get(index));
                logger.debug(Arrays.toString(returns[index]));
            }
        }

        double[] means = new double[symbols.size()];

        for (int index = 0; index < symbols.size(); index++)
        {
            means[index] = new DescriptiveStatistics(returns[index]).getMean();
        }

        if (Level.DEBUG.isGreaterOrEqual(getLevel(logger)))
        {
            logger.debug("mean");
            logger.debug(Arrays.toString(means));
        }

        if (symbols.size() > 1)
        {
            RealMatrix covariance = new Covariance(MatrixUtils.createRealMatrix(returns).transpose())
                    .getCovarianceMatrix();

            final Map<String, BigDecimal> weightMap = portfolio.getHoldings().stream()
                    .collect(Collectors.toMap(Holding::getTicker, Holding::getWeight));

            double[] weights = symbols.stream().mapToDouble(s -> weightMap.get(s).doubleValue()).toArray();

            if (Level.DEBUG.isGreaterOrEqual(getLevel(logger)))
            {
                logger.debug("weights");
                logger.debug(Arrays.toString(weights));
            }

            MarkowitzModel3 markowitzModel = new MarkowitzModel3(covariance.getData(), means, parameters.getRiskFree(),
                    parameters.getRiskAversion(), weights);

            markowitzModel.setShortingAllowed(parameters.isShortingAllowed());

            MarkowitzPortfolio overall = new MarkowitzPortfolio(weights, markowitzModel.calculateExpectedReturn(),
                    markowitzModel.calculateVariance(), parameters.getRiskFree());

            Map<String, MarkowitzPortfolio> individuals = new HashMap<>();

            for (int index = 0; index < symbols.size(); index++)
            {
                individuals.put(
                        symbols.get(index),
                        new MarkowitzPortfolio(null, means[index], covariance.getEntry(index, index), parameters
                                .getRiskFree()));
            }

            EfficientFrontier efficientFrontier = markowitzModel.calculateEfficientFrontier();

            MarkowitzPortfolio tangencyPortfolio = markowitzModel.calculateTangencyPortfolio();

            return new PortfolioReport(portfolio.getId(), portfolio.getName(), symbols, overall, individuals,
                    efficientFrontier, parameters.getRiskFree(), tangencyPortfolio);
        }
        else
        {
            return null;
        }
    }

    public Level getLevel(Logger logger)
    {
        return logger.getEffectiveLevel();
    }

    private void calculateHoldings(Portfolio portfolio, Set<String> tickers)
    {
        List<Holding> holdings = new ArrayList<Holding>();

        List<Transaction.Type> transactionTypes1 = Arrays.asList(new Transaction.Type[] { Transaction.Type.Buying,
                Transaction.Type.Selling });

        for (String ticker : tickers)
        {
            List<Transaction> founds = portfolio.getTransactions().stream()
                    .filter(t -> t.getTicker().equals(ticker) && transactionTypes1.contains(t.getType()))
                    .collect(Collectors.toList());

            BigDecimal cost = new BigDecimal(0.0);
            BigDecimal amount = new BigDecimal(0.0);
            BigDecimal change = null;

            for (Transaction t : founds)
            {
                amount = t.getType() == Transaction.Type.Buying ? amount.add(t.getAmount()) : amount.subtract(t
                        .getAmount());
                change = t.getAmount().multiply(t.getPrice());
                cost = t.getType() == Transaction.Type.Buying ? cost.add(change) : cost.subtract(change);

                if (t.getCommission() != null)
                {
                    cost = cost.add(t.getCommission());
                }

                if (t.getOtherCharges() != null)
                {
                    cost = cost.add(t.getOtherCharges());
                }
            }

            Holding holding = new Holding();

            holding.setTicker(ticker);
            holding.setAmount(amount);
            holding.setCost(rounding(cost));

            holdings.add(holding);
        }

        portfolio.setHoldings(holdings);
    }

    private BigDecimal rounding(BigDecimal value)
    {
        return value.setScale(3, RoundingMode.HALF_UP);
    }

    private void calculateHoldingChanges(Portfolio portfolio, Set<String> tickers)
    {
        Map<String, Quote> quotes = quoteFeed.snapshot(tickers);
        Map<String, List<Bar>> timeseries = timeseriesFeed.snapshot(Intervals.Daily, tickers, 2);

        if (Level.DEBUG.isGreaterOrEqual(getLevel(logger)))
        {
            if (quotes != null)
            {
                for (Quote quote : quotes.values())
                {
                    logger.debug(quote.toString());
                }
            }
            else
            {
                logger.debug("quotes == null");
            }

            if (timeseries != null)
            {
                for (List<Bar> bars : timeseries.values())
                {
                    if (bars != null)
                    {
                        for (Bar bar : bars)
                        {
                            logger.debug(bar.toString());
                        }
                    }
                }
            }
            else
            {
                logger.debug("timeseries == null");
            }
        }

        for (Holding holding : portfolio.getHoldings())
        {
            String ticker = holding.getTicker();

            BigDecimal price = getPrice(ticker, quotes, timeseries);
            BigDecimal historyClose = getHistoryClose(ticker, quotes, timeseries);

            logger.debug(String.format("%s: price=%f, history=%f", ticker, price != null ? price.doubleValue() : 0,
                    historyClose != null ? historyClose.doubleValue() : 0));

            if (price == null)
            {
                holding.setValue(holding.getCost());
                holding.setDailyChange(new BigDecimal(0));
                holding.setDailyChangePercentage(new BigDecimal(0));
                holding.setTotalChange(new BigDecimal(0));
                holding.setTotalChangePercentage(new BigDecimal(0));
            }
            else
            {
                holding.setValue(rounding(price.multiply(holding.getAmount())));

                holding.setTotalChange(holding.getValue().subtract(holding.getCost()));
                holding.setTotalChangePercentage(holding.getTotalChange().divide(holding.getCost(), 5,
                        RoundingMode.HALF_UP));

                if (historyClose == null)
                {
                    holding.setDailyChange(new BigDecimal(0));
                    holding.setDailyChangePercentage(new BigDecimal(0));
                }
                else
                {
                    BigDecimal historyValue = historyClose.multiply(holding.getAmount());

                    holding.setDailyChange(holding.getValue().subtract(historyValue));
                    holding.setDailyChangePercentage(holding.getDailyChange().divide(historyValue, 5,
                            RoundingMode.HALF_UP));
                }
            }
        }
    }

    private BigDecimal getPrice(String ticker, Map<String, Quote> quotes, Map<String, List<Bar>> timeseries)
    {
        List<Bar> bars = timeseries.containsKey(ticker) ? timeseries.get(ticker) : null;
        Quote quote = quotes.containsKey(ticker) ? quotes.get(ticker) : null;

        Bar latest = bars != null && bars.size() > 0 ? bars.get(0) : null;

        if (latest != null && quote != null && !FloatUtil.equal(quote.getPrice(), 0, FloatUtil.Precision.P6))
        {
            if (CalendarUtil.compare(latest.getTimestamp(), quote.getTimestamp(), Calendar.DAY_OF_MONTH) == 0)
            {
                return new BigDecimal(latest.getClose());
            }
            else
            {
                return new BigDecimal(CalendarUtil.compare(latest.getTimestamp(), quote.getTimestamp(),
                        Calendar.DAY_OF_MONTH) >= 0 ? latest.getClose() : quote.getPrice());
            }
        }
        else if (latest != null)
        {
            return new BigDecimal(latest.getClose());
        }
        else if (quote != null && !FloatUtil.equal(quote.getPrice(), 0, FloatUtil.Precision.P6))
        {
            return new BigDecimal(quote.getPrice());
        }

        return null;
    }

    private BigDecimal getHistoryClose(String ticker, Map<String, Quote> quotes, Map<String, List<Bar>> timeseries)
    {
        List<Bar> bars = timeseries.containsKey(ticker) ? timeseries.get(ticker) : null;
        Quote quote = quotes.containsKey(ticker) ? quotes.get(ticker) : null;

        Bar latest = bars != null && bars.size() > 0 ? bars.get(0) : null;
        Bar history = bars != null && bars.size() > 1 ? bars.get(1) : null;

        if (latest != null && quote != null && !FloatUtil.equal(quote.getPrice(), 0, FloatUtil.Precision.P6))
        {
            if (CalendarUtil.compare(latest.getTimestamp(), quote.getTimestamp(), Calendar.DAY_OF_MONTH) == 0)
            {
                return history != null ? new BigDecimal(history.getClose()) : null;
            }
            else
            {
                Bar candidate = CalendarUtil
                        .compare(latest.getTimestamp(), quote.getTimestamp(), Calendar.DAY_OF_MONTH) >= 0 ? history
                        : latest;
                return candidate != null ? new BigDecimal(candidate.getClose()) : null;
            }
        }
        else if (latest != null)
        {
            return history != null ? new BigDecimal(history.getClose()) : null;
        }

        return null;
    }

    private double[][] loadHistoryReturns(List<String> symbols, String intervals, int sampleCount)
    {
        if (sampleCount == 0)
        {
            sampleCount = defaultSampleCount;
        }

        int samples = 1;

        if (intervals.equals("W"))
        {
            samples = 5;
        }
        else if (intervals.equals("M"))
        {
            samples = 22;
        }
        else if (intervals.equals("Y"))
        {
            samples = 250;
        }

        int total = sampleCount * samples;

        Map<String, List<Bar>> barsMap = timeseriesFeed.snapshot(Intervals.Daily, symbols, total);

        for (int row = 0; row < symbols.size(); row++)
        {
            List<Bar> bars = barsMap.get(symbols.get(row));

            if (bars.size() < total)
            {
                total = bars.size();
            }
        }

        total = total / samples * samples;

        double[][] returns = new double[symbols.size()][total / samples];

        for (int row = 0; row < symbols.size(); row++)
        {
            List<Bar> bars = barsMap.get(symbols.get(row));

            for (int col = 0; col < bars.size() - 1 && col < total - samples; col += samples)
            {
                Bar cur = bars.get(col);
                Bar last = bars.get(col + samples);

                returns[row][col / samples] = (cur.getClose() - last.getClose()) / last.getClose();
            }
        }

        return returns;
    }
}
