package org.goldenroute.portfolio.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalDouble;
import java.util.Set;

import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.log4j.Logger;

/*
 * http://faculty.washington.edu/ezivot/econ424/portfolioTheoryMatrix.pdf.
 * Unconstrained Portfolios.
 */
public class MarkowitzModel
{
    private static final Logger logger = Logger.getLogger(MarkowitzModel.class);

    private RealMatrix covariance;
    private RealMatrix expectedReturns;
    private RealMatrix initialWeights;
    private double riskFree;
    private Double targetReturn;

    public double getRiskFree()
    {
        return riskFree;
    }

    public void setRiskFree(double riskFree)
    {
        this.riskFree = riskFree;
    }

    public Double getTargetReturn()
    {
        return targetReturn;
    }

    public void setTargetReturn(Double targetReturn)
    {
        this.targetReturn = targetReturn;
    }

    public MarkowitzModel(RealMatrix covariance, double[] expectedReturns, double riskFree)
    {
        this(covariance, expectedReturns, riskFree, null);
    }

    public MarkowitzModel(RealMatrix covariance, double[] expectedReturns, double riskFree, double[] initialWeights)
    {
        if (covariance == null || !covariance.isSquare())
        {
            throw new IllegalArgumentException("covariance should be a square matrix.");
        }

        if (expectedReturns == null || covariance.getColumnDimension() != expectedReturns.length)
        {
            throw new IllegalArgumentException("covariance and expectedReturns should be with same length.");
        }

        logger.debug("covariance:" + covariance.toString());
        logger.debug("expectedReturns:" + Arrays.toString(expectedReturns));
        logger.debug("initialWeights:" + (initialWeights != null ? Arrays.toString(expectedReturns) : "N/A"));

        this.covariance = covariance;
        this.expectedReturns = MatrixUtils.createColumnRealMatrix(expectedReturns);
        this.riskFree = riskFree;
        this.targetReturn = null;

        if (initialWeights != null)
        {
            this.initialWeights = MatrixUtils.createColumnRealMatrix(initialWeights);
        }
        else
        {
            this.initialWeights = null;
        }
    }

    public double calculateVariance()
    {
        if (initialWeights == null)
        {
            throw new IllegalArgumentException("No asset weights available.");
        }

        return initialWeights.transpose().multiply(covariance).multiply(initialWeights).getEntry(0, 0);
    }

    public double calculateExpectedReturn()
    {
        if (initialWeights == null)
        {
            throw new IllegalArgumentException("No asset weights available.");
        }

        return initialWeights.transpose().multiply(expectedReturns).getEntry(0, 0);
    }

    public MarkowitzPortfolio optimize()
    {
        return optimize(targetReturn);
    }

    @SuppressWarnings({ "checkstyle:LocalVariableName" })
    private MarkowitzPortfolio optimize(Double targetReturn)
    {
        if (targetReturn != null)
        {
            double[] one = new double[covariance.getRowDimension() + 2];
            Arrays.fill(one, 0, one.length - 2, 1);

            double[] zero = new double[covariance.getRowDimension() + 2];
            zero[zero.length - 2] = targetReturn;

            zero[zero.length - 1] = 1;

            double[] u = expectedReturns.getColumnVector(0).append(0).append(0).toArray();

            RealMatrix a = MatrixUtils.createRealMatrix(covariance.getRowDimension() + 2,
                    covariance.getColumnDimension() + 2);

            a.setSubMatrix(covariance.scalarMultiply(2).getData(), 0, 0);

            a.setColumn(a.getColumnDimension() - 2, u);
            a.setColumn(a.getColumnDimension() - 1, one);

            a.setRow(a.getRowDimension() - 2, u);
            a.setRow(a.getRowDimension() - 1, one);

            RealMatrix b = MatrixUtils.createColumnRealMatrix(zero);
            RealMatrix z = new LUDecomposition(a).getSolver().getInverse().multiply(b);

            RealMatrix weights = z.getSubMatrix(0, z.getRowDimension() - 3, 0, 0);

            double ret = weights.transpose().multiply(expectedReturns).getEntry(0, 0);
            double var = weights.transpose().multiply(covariance).multiply(weights).getEntry(0, 0);

            return new MarkowitzPortfolio(weights.getColumn(0), ret, var, riskFree);
        }
        else
        {
            /**
             * return global minimum variance portfolio.
             */

            double[] one = new double[covariance.getRowDimension() + 1];
            Arrays.fill(one, 0, one.length - 1, 1);

            double[] zero = new double[covariance.getRowDimension() + 1];
            zero[zero.length - 1] = 1;

            RealMatrix a = MatrixUtils.createRealMatrix(covariance.getRowDimension() + 1,
                    covariance.getColumnDimension() + 1);
            a.setSubMatrix(covariance.scalarMultiply(2).getData(), 0, 0);
            a.setColumn(a.getColumnDimension() - 1, one);
            a.setRow(a.getRowDimension() - 1, one);

            RealMatrix b = MatrixUtils.createColumnRealMatrix(zero);
            RealMatrix z = new LUDecomposition(a).getSolver().getInverse().multiply(b);

            RealMatrix weights = z.getSubMatrix(0, z.getRowDimension() - 2, 0, 0);

            double ret = weights.transpose().multiply(expectedReturns).getEntry(0, 0);
            double var = weights.transpose().multiply(covariance).multiply(weights).getEntry(0, 0);

            return new MarkowitzPortfolio(weights.getColumn(0), ret, var, riskFree);
        }
    }

    @SuppressWarnings({ "checkstyle:LocalVariableName" })
    public EfficientFrontier calculateEfficientFrontier()
    {
        logger.debug("Start calculate Efficient Frontier******");

        OptionalDouble highest = Arrays.stream(expectedReturns.getColumn(0)).max();
        logger.debug("highest ret:" + Double.toString(highest.getAsDouble()));

        MarkowitzPortfolio globalMinimumVariance = optimize(null);
        MarkowitzPortfolio highestReturn = optimize(highest.getAsDouble());

        logger.debug("globalMinimumVariance:" + globalMinimumVariance.toString());
        logger.debug("highestReturn:" + highestReturn.toString());

        RealMatrix x = MatrixUtils.createColumnRealMatrix(globalMinimumVariance.getWeights());
        RealMatrix y = MatrixUtils.createColumnRealMatrix(highestReturn.getWeights());

        Set<MarkowitzPortfolio> optimisations = new HashSet<>();

        for (double a = 0; a <= 1; a += 0.05)
        {
            RealMatrix z = x.scalarMultiply(a).add(y.scalarMultiply(1.0 - a));

            logger.debug("a:" + Double.toString(a));
            logger.debug("z weights:" + z.toString());

            double ret = z.transpose().multiply(expectedReturns).getEntry(0, 0);
            double var = z.transpose().multiply(covariance).multiply(z).getEntry(0, 0);

            logger.debug("ret:" + Double.toString(ret));
            logger.debug("var:" + Double.toString(var));

            if (ret < globalMinimumVariance.getExpectedReturn())
            {
                logger.debug("ret < GMT");
                continue;
            }

            optimisations.add(new MarkowitzPortfolio(z.getColumn(0), ret, var, riskFree));
        }

        List<MarkowitzPortfolio> list = new ArrayList<>(optimisations);

        list.sort(new Comparator<MarkowitzPortfolio>()
        {
            @Override
            public int compare(MarkowitzPortfolio o1, MarkowitzPortfolio o2)
            {
                return (int) ((o1.getExpectedReturn() - o2.getExpectedReturn()) * 1000000);
            }
        });

        return new EfficientFrontier(list, globalMinimumVariance);
    }

    public MarkowitzPortfolio calculateTangencyPortfolio()
    {
        double[] oneArray = new double[covariance.getRowDimension()];
        Arrays.fill(oneArray, 0, oneArray.length, 1);
        RealMatrix covt = new LUDecomposition(covariance).getSolver().getInverse();

        RealMatrix one = MatrixUtils.createColumnRealMatrix(oneArray);
        RealMatrix minusrf = expectedReturns.subtract(one.scalarMultiply(riskFree));

        RealMatrix top = covt.multiply(minusrf);
        double bottom = one.transpose().multiply(top).getEntry(0, 0);

        RealMatrix weights = top.scalarMultiply(1.0 / bottom);

        logger.debug("weights:" + weights.toString());

        double ret = weights.transpose().multiply(expectedReturns).getEntry(0, 0);
        double var = weights.transpose().multiply(covariance).multiply(weights).getEntry(0, 0);

        logger.debug("ret:" + Double.toString(ret));
        logger.debug("var:" + Double.toString(var));

        return new MarkowitzPortfolio(weights.getColumn(0), ret, var, riskFree);
    }
}
