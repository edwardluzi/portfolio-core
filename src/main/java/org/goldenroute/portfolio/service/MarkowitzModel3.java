package org.goldenroute.portfolio.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalDouble;
import java.util.Set;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.log4j.Logger;
import org.ojalgo.finance.portfolio.MarkowitzModel;
import org.ojalgo.matrix.BasicMatrix.Builder;
import org.ojalgo.matrix.BasicMatrix.Factory;
import org.ojalgo.matrix.PrimitiveMatrix;

public class MarkowitzModel3
{
    private static final Logger logger = Logger.getLogger(MarkowitzModel3.class);

    private double[][] covariance;
    private double[] expectedReturns;
    private double[] initialWeights;

    private double riskFree;
    private double riskAversion;

    private Boolean shortingAllowed;

    public double getRiskFree()
    {
        return riskFree;
    }

    public void setRiskFree(double riskFree)
    {
        this.riskFree = riskFree;
    }

    public double getRiskAversion()
    {
        return riskAversion;
    }

    public void setRiskAversion(double riskAversion)
    {
        this.riskAversion = riskAversion;
    }

    public Boolean getShortingAllowed()
    {
        return shortingAllowed;
    }

    public void setShortingAllowed(Boolean shortingAllowed)
    {
        this.shortingAllowed = shortingAllowed;
    }

    public MarkowitzModel3(double[][] covariance, double[] expectedReturns, double riskFree, double riskAversion)
    {
        this(covariance, expectedReturns, riskFree, riskAversion, null);
    }

    public MarkowitzModel3(double[][] covariance, double[] expectedReturns, double riskFree, double riskAversion,
            double[] initialWeights)
    {
        if (covariance == null || covariance.length == 0 || covariance.length != covariance[0].length)
        {
            throw new IllegalArgumentException("covariance should be a square matrix.");
        }

        if (expectedReturns == null || expectedReturns.length != covariance.length)
        {
            throw new IllegalArgumentException("covariance and expectedReturns should be with same length.");
        }

        if (initialWeights != null && initialWeights.length != covariance.length)
        {
            throw new IllegalArgumentException("covariance and expectedReturns should be with same length.");
        }

        logger.debug("covariance:" + covariance.toString());
        logger.debug("expectedReturns:" + Arrays.toString(expectedReturns));
        logger.debug("initialWeights:" + (initialWeights != null ? Arrays.toString(expectedReturns) : "N/A"));

        this.covariance = covariance;
        this.expectedReturns = expectedReturns;
        this.riskFree = riskFree;
        this.riskAversion = riskAversion;

        if (this.riskAversion > MarkowitzPortfolio.RISK_AVERSION_INFINITE)
        {
            this.riskAversion = MarkowitzPortfolio.RISK_AVERSION_INFINITE;
        }
        else if (this.riskAversion < MarkowitzPortfolio.RISK_AVERSION_INFINITESIMAL)
        {
            this.riskAversion = MarkowitzPortfolio.RISK_AVERSION_INFINITESIMAL;
        }

        this.initialWeights = initialWeights;
        this.shortingAllowed = null;
    }

    public double calculateVariance()
    {
        if (initialWeights == null)
        {
            throw new IllegalArgumentException("No asset weights available.");
        }

        RealMatrix cov = MatrixUtils.createRealMatrix(covariance);
        RealMatrix weights = MatrixUtils.createColumnRealMatrix(initialWeights);

        return weights.transpose().multiply(cov).multiply(weights).getEntry(0, 0);
    }

    public double calculateExpectedReturn()
    {
        if (initialWeights == null)
        {
            throw new IllegalArgumentException("No asset weights available.");
        }

        RealMatrix rets = MatrixUtils.createColumnRealMatrix(expectedReturns);
        RealMatrix weights = MatrixUtils.createColumnRealMatrix(initialWeights);

        return weights.transpose().multiply(rets).getEntry(0, 0);
    }

    public MarkowitzPortfolio optimize()
    {
        Factory<PrimitiveMatrix> matrixFactory = PrimitiveMatrix.FACTORY;

        PrimitiveMatrix cov = matrixFactory.rows(covariance);
        PrimitiveMatrix ret = matrixFactory.columns(expectedReturns);

        MarkowitzModel markowitz = new MarkowitzModel(cov, ret);
        markowitz.setRiskAversion(riskAversion);

        if (shortingAllowed != null)
        {
            markowitz.setShortingAllowed(shortingAllowed);
        }

        return new MarkowitzPortfolio(markowitz.getWeights().stream().mapToDouble(bd -> bd.doubleValue()).toArray(),
                markowitz.getMeanReturn(), markowitz.getReturnVariance(), riskFree);
    }

    public EfficientFrontier calculateEfficientFrontier()
    {
        double savedRiskAversion = riskAversion;

        logger.debug("Start calculate Efficient Frontier******");

        OptionalDouble highest = Arrays.stream(expectedReturns).max();
        logger.debug("highest ret:" + Double.toString(highest.getAsDouble()));

        setRiskAversion(MarkowitzPortfolio.RISK_AVERSION_INFINITE);
        MarkowitzPortfolio globalMinimumVariance = optimize();
        logger.debug("globalMinimumVariance:" + globalMinimumVariance.toString());

        Set<MarkowitzPortfolio> optimisations = new HashSet<>();

        double ra = 1.0;
        setRiskAversion(ra);

        MarkowitzPortfolio portfolio = optimize();

        if (portfolio.getExpectedReturn() < globalMinimumVariance.getExpectedReturn())
        {
            logger.debug("ret < GMT");
        }
        else
        {
            optimisations.add(portfolio);
        }

        ra = 1.3;

        for (int index = 0; (index < 10 || optimisations.size() < 10) && ra < MarkowitzPortfolio.RISK_AVERSION_INFINITE; index++)
        {
            setRiskAversion(ra);
            portfolio = optimize();

            if (portfolio.getExpectedReturn() < globalMinimumVariance.getExpectedReturn())
            {
                continue;
            }

            optimisations.add(portfolio);
            ra *= 1.3;
        }

        ra = 0.8;

        for (int index = 0; index < 15; index++)
        {
            setRiskAversion(ra);

            portfolio = optimize();

            if (portfolio.getExpectedReturn() < globalMinimumVariance.getExpectedReturn())
            {
                logger.debug("ret < GMT");
                continue;
            }

            optimisations.add(portfolio);
            ra *= 0.8;
        }

        setRiskAversion(savedRiskAversion);

        List<MarkowitzPortfolio> list = new ArrayList<>(optimisations);

        list.sort(new Comparator<MarkowitzPortfolio>()
        {
            @Override
            public int compare(MarkowitzPortfolio o1, MarkowitzPortfolio o2)
            {
                return (int) ((o1.getExpectedReturn() - o2.getExpectedReturn()) * 1000000);
            }
        });

        for (MarkowitzPortfolio port : list)
        {
            logger.debug("portfolio:" + port.toString());
        }

        return new EfficientFrontier(list, globalMinimumVariance);
    }

    public MarkowitzPortfolio calculateTangencyPortfolio()
    {
        Factory<PrimitiveMatrix> matrixFactory = PrimitiveMatrix.FACTORY;
        Builder<PrimitiveMatrix> builder = matrixFactory.makeZero(covariance.length + 1, covariance.length + 1)
                .copyToBuilder();

        for (int row = 0; row < covariance.length; row++)
        {
            for (int col = 0; col < covariance.length; col++)
            {
                builder.set(row + 1, col + 1, covariance[row][col]);
            }
        }

        PrimitiveMatrix cov = builder.build();

        double[] returns = new double[expectedReturns.length + 1];
        returns[0] = riskFree;
        System.arraycopy(expectedReturns, 0, returns, 1, expectedReturns.length);

        PrimitiveMatrix ret = matrixFactory.columns(returns);

        MarkowitzModel markowitz = new MarkowitzModel(cov, ret);

        if (shortingAllowed != null)
        {
            markowitz.setShortingAllowed(shortingAllowed);
        }

        MarkowitzPortfolio portfolio = new MarkowitzPortfolio(markowitz.getWeights().stream()
                .mapToDouble(bd -> bd.doubleValue()).toArray(), markowitz.getMeanReturn(),
                markowitz.getReturnVariance(), riskFree);

        logger.debug("tangency:" + portfolio.toString());

        return portfolio;
    }
}
