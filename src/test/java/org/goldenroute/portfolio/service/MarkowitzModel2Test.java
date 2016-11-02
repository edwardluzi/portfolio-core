package org.goldenroute.portfolio.service;

import java.util.stream.DoubleStream;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.log4j.Logger;
import org.goldenroute.PortfolioCoreApplication;
import org.goldenroute.util.FloatUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@Configuration
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(PortfolioCoreApplication.class)
public class MarkowitzModel2Test
{
    private static final Logger logger = Logger.getLogger(MarkowitzModel2Test.class);

    @Test
    public void test()
    {
        RealMatrix cov = MatrixUtils.createRealMatrix(new double[][] { { 0.01, 0.0018, 0.0011 },
                { 0.0018, 0.0109, 0.0026 }, { 0.0011, 0.0026, 0.0199 } });

        double[] ret = new double[] { 0.0427, 0.0015, 0.0285 };

        MarkowitzModel2 markowitz = new MarkowitzModel2(cov, ret, 0, MarkowitzPortfolio.RISK_AVERSION_INFINITE);
        MarkowitzPortfolio o1 = markowitz.optimize();

        logger.debug(o1.toString());

        assert (FloatUtil.equal(o1.getWeights()[0], 0.4411, FloatUtil.Precision.P4));
        assert (FloatUtil.equal(o1.getWeights()[1], 0.3656, FloatUtil.Precision.P4));
        assert (FloatUtil.equal(o1.getWeights()[2], 0.1933, FloatUtil.Precision.P4));
        assert (FloatUtil.equal(o1.getExpectedReturn(), 0.02489, FloatUtil.Precision.P5));
        assert (FloatUtil.equal(o1.getVariance(), 0.005282, FloatUtil.Precision.P6));

        markowitz.setRiskAversion(3.4);

        o1 = markowitz.optimize();
        logger.debug(o1.toString());

        markowitz.setRiskFree(0.005);
        markowitz.setRiskAversion(4);
        MarkowitzPortfolio tp = markowitz.optimize();

        logger.debug(tp.toString());

        double[] riskAssets = new double[3];
        System.arraycopy(tp.getWeights(), 1, riskAssets, 0, 3);

        double sum = DoubleStream.of(riskAssets).sum();

        for (int i = 0; i < riskAssets.length; i++)
        {
            riskAssets[i] = riskAssets[i] / sum;
        }

        assert (FloatUtil.equal(riskAssets[0], 1.0268, FloatUtil.Precision.P4));
        assert (FloatUtil.equal(riskAssets[1], -0.3263, FloatUtil.Precision.P4));
        assert (FloatUtil.equal(riskAssets[2], 0.2994, FloatUtil.Precision.P4));
    }

    @Test
    public void test2()
    {
        RealMatrix cov = MatrixUtils.createRealMatrix(new double[][] { { 0.00620, 0.00246 }, { 0.00246, 0.03738 } });

        double[] ret = new double[] { 0.0502, 0.1158 };

        MarkowitzModel2 markowitz = new MarkowitzModel2(cov, ret, 0, MarkowitzPortfolio.RISK_AVERSION_INFINITE);
        MarkowitzPortfolio o1 = markowitz.optimize();

        logger.debug(o1.toString());

        assert (FloatUtil.equal(o1.getWeights()[0], 0.9033, FloatUtil.Precision.P4));
        assert (FloatUtil.equal(o1.getWeights()[1], 0.0967, FloatUtil.Precision.P4));

        markowitz.setRiskAversion(3.4);

        o1 = markowitz.optimize();
        logger.debug(o1.toString());

        assert (FloatUtil.equal(o1.getWeights()[0], 0.4042, FloatUtil.Precision.P4));
        assert (FloatUtil.equal(o1.getWeights()[1], 0.5958, FloatUtil.Precision.P4));

        markowitz.setRiskAversion(1.88);

        o1 = markowitz.optimize();
        logger.debug(o1.toString());

        assert (FloatUtil.equal(o1.getWeights()[0], 0.0007, FloatUtil.Precision.P4));
        assert (FloatUtil.equal(o1.getWeights()[1], 0.9993, FloatUtil.Precision.P4));

        markowitz.setRiskAversion(MarkowitzPortfolio.RISK_AVERSION_INFINITESIMAL);

        o1 = markowitz.optimize();
        logger.debug(o1.toString());

        markowitz.setRiskFree(0.037);
        markowitz.setRiskAversion(MarkowitzPortfolio.RISK_AVERSION_INFINITE);

        MarkowitzPortfolio o2 = markowitz.optimize();
        logger.debug(o2.toString());

        markowitz.setRiskAversion(100);

        o2 = markowitz.optimize();
        logger.debug(o2.toString());

        markowitz.setRiskAversion(20.19);

        o2 = markowitz.optimize();
        logger.debug(o2.toString());

        assert (FloatUtil.equal(o2.getWeights()[0], 0.83, FloatUtil.Precision.P2));
        assert (FloatUtil.equal(o2.getWeights()[1], 0.07, FloatUtil.Precision.P2));
        assert (FloatUtil.equal(o2.getWeights()[2], 0.10, FloatUtil.Precision.P2));
    }
}
