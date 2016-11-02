package org.goldenroute.portfolio.service;

import java.util.Arrays;
import java.util.stream.DoubleStream;

import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.log4j.Logger;

/*
 * http://www.norstad.org/finance/portopt1.pdf.
 * Unconstrained Portfolios.
 */
public class MarkowitzModel2
{
    private static final Logger logger = Logger.getLogger(MarkowitzModel2.class);

    private RealMatrix covariance;
    private RealMatrix expectedReturns;
    private RealMatrix initialWeights;
    private double riskFree;
    private double riskAversion;

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

    public MarkowitzModel2(RealMatrix covariance, double[] expectedReturns, double riskFree, double riskAversion)
    {
        this(covariance, expectedReturns, riskFree, riskAversion, null);
    }

    public MarkowitzModel2(RealMatrix covariance, double[] expectedReturns, double riskFree, double riskAversion,
            double[] initialWeights)
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
        this.riskAversion = riskAversion;

        if (this.riskAversion > MarkowitzPortfolio.RISK_AVERSION_INFINITE)
        {
            this.riskAversion = MarkowitzPortfolio.RISK_AVERSION_INFINITE;
        }
        else if (this.riskAversion < MarkowitzPortfolio.RISK_AVERSION_INFINITESIMAL)
        {
            this.riskAversion = MarkowitzPortfolio.RISK_AVERSION_INFINITESIMAL;
        }

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

    /**
     * return global minimum variance portfolio with given risk aversion.
     */
    @SuppressWarnings({ "checkstyle:LocalVariableName" })
    public MarkowitzPortfolio optimize()
    {
        if (riskFree <= MarkowitzPortfolio.NONE_RISK_FREE_ASSET)
        {
            double[] one = new double[covariance.getRowDimension() + 1];
            Arrays.fill(one, 0, one.length - 1, 1);

            double[] zero = new double[covariance.getRowDimension() + 1];
            zero[zero.length - 1] = 1;

            RealMatrix v = MatrixUtils.createRealMatrix(covariance.getRowDimension() + 1,
                    covariance.getColumnDimension() + 1);
            v.setSubMatrix(covariance.getData(), 0, 0);
            v.setColumn(v.getColumnDimension() - 1, one);
            v.setRow(v.getRowDimension() - 1, one);

            RealMatrix x = MatrixUtils.createColumnRealMatrix(Arrays.copyOf(expectedReturns.getColumn(0),
                    covariance.getRowDimension() + 1));
            RealMatrix y = MatrixUtils.createColumnRealMatrix(zero);

            RealMatrix vi = new LUDecomposition(v).getSolver().getInverse();

            RealMatrix c = vi.multiply(x);
            RealMatrix d = vi.multiply(y);

            RealMatrix w = (riskAversion >= MarkowitzPortfolio.RISK_AVERSION_INFINITE) ? d : c.scalarMultiply(
                    1 / riskAversion).add(d);

            RealMatrix weights = w.getSubMatrix(0, w.getRowDimension() - 2, 0, 0);

            double ret = weights.transpose().multiply(expectedReturns).getEntry(0, 0);
            double var = weights.transpose().multiply(covariance).multiply(weights).getEntry(0, 0);

            return new MarkowitzPortfolio(weights.getColumn(0), ret, var, riskFree);
        }
        else
        {
            double[] zero = new double[covariance.getRowDimension() + 2];

            double[] one = new double[covariance.getRowDimension() + 2];
            Arrays.fill(one, 0, one.length - 1, 1);

            RealMatrix v = MatrixUtils.createRealMatrix(covariance.getRowDimension() + 2,
                    covariance.getColumnDimension() + 2);
            v.setSubMatrix(covariance.getData(), 1, 1);
            v.setColumn(0, zero);
            v.setRow(0, zero);
            v.setColumn(v.getColumnDimension() - 1, one);
            v.setRow(v.getRowDimension() - 1, one);

            double[] rets = new double[expectedReturns.getRowDimension() + 2];
            System.arraycopy(expectedReturns.getColumn(0), 0, rets, 1, rets.length - 2);
            rets[0] = riskFree;

            RealMatrix x = MatrixUtils.createColumnRealMatrix(rets);
            RealMatrix vi = new LUDecomposition(v).getSolver().getInverse();
            RealMatrix c = vi.multiply(x);

            RealMatrix w;

            if (riskAversion >= MarkowitzPortfolio.RISK_AVERSION_INFINITE)
            {
                w = MatrixUtils.createColumnRealMatrix(Arrays.copyOf(new double[] { 1 }, c.getRowDimension()));
            }
            else
            {
                w = c.scalarMultiply(1 / riskAversion);
                w.addToEntry(0, 0, 1);
            }

            double[] ws = Arrays.copyOf(w.getColumn(0), w.getRowDimension() - 1);

            double sum = DoubleStream.of(ws).sum();

            for (int i = 0; i < ws.length; i++)
            {
                ws[i] = ws[i] / sum;
            }

            RealMatrix weights = MatrixUtils.createColumnRealMatrix(ws);
            double ret = weights.transpose().multiply(x.getSubMatrix(0, x.getRowDimension() - 2, 0, 0)).getEntry(0, 0);
            RealMatrix vv = v.getSubMatrix(0, v.getRowDimension() - 2, 0, v.getColumnDimension() - 2);
            double var = weights.transpose().multiply(vv).multiply(weights).getEntry(0, 0);

            return new MarkowitzPortfolio(ws, ret, var, riskFree);
        }
    }
}
