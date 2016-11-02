package org.goldenroute.portfolio.service;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.log4j.Logger;
import org.goldenroute.PortfolioCoreApplication;
import org.goldenroute.datafeed.TimeseriesFeed;
import org.goldenroute.util.FloatUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@Configuration
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(PortfolioCoreApplication.class)
public class MarkowitzModelTest
{
    private static final Logger logger = Logger.getLogger(MarkowitzModelTest.class);

    @Autowired
    private TimeseriesFeed timeseriesFeed;

    @SuppressWarnings({ "checkstyle:LocalVariableName" })
    @Test
    public void test()
    {
        RealMatrix cov = MatrixUtils.createRealMatrix(new double[][] { { 0.01, 0.0018, 0.0011 },
                { 0.0018, 0.0109, 0.0026 }, { 0.0011, 0.0026, 0.0199 } });

        double[] ret = new double[] { 0.0427, 0.0015, 0.0285 };

        MarkowitzModel markowitz = new MarkowitzModel(cov, ret, 0.005);
        MarkowitzPortfolio o1 = markowitz.optimize();

        logger.debug(o1.toString());

        assert (FloatUtil.equal(o1.getWeights()[0], 0.4411, FloatUtil.Precision.P4));
        assert (FloatUtil.equal(o1.getWeights()[1], 0.3656, FloatUtil.Precision.P4));
        assert (FloatUtil.equal(o1.getWeights()[2], 0.1933, FloatUtil.Precision.P4));
        assert (FloatUtil.equal(o1.getExpectedReturn(), 0.02489, FloatUtil.Precision.P5));
        assert (FloatUtil.equal(o1.getVariance(), 0.005282, FloatUtil.Precision.P6));

        markowitz.setTargetReturn(0.0427);
        MarkowitzPortfolio o2 = markowitz.optimize();

        assert (FloatUtil.equal(o2.getWeights()[0], 0.82745, FloatUtil.Precision.P5));
        assert (FloatUtil.equal(o2.getWeights()[1], -0.09075, FloatUtil.Precision.P5));
        assert (FloatUtil.equal(o2.getWeights()[2], 0.26329, FloatUtil.Precision.P5));
        assert (FloatUtil.equal(o2.getExpectedReturn(), 0.0427, FloatUtil.Precision.P4));
        assert (FloatUtil.equal(o2.getVariance(), 0.0084, FloatUtil.Precision.P4));

        logger.debug(o2.toString());

        markowitz.setTargetReturn(0.0285);
        MarkowitzPortfolio o3 = markowitz.optimize();

        assert (FloatUtil.equal(o3.getWeights()[0], 0.5194, FloatUtil.Precision.P4));
        assert (FloatUtil.equal(o3.getWeights()[1], 0.2732, FloatUtil.Precision.P4));
        assert (FloatUtil.equal(o3.getWeights()[2], 0.2075, FloatUtil.Precision.P4));
        assert (FloatUtil.equal(o3.getExpectedReturn(), 0.0285, FloatUtil.Precision.P4));
        assert (FloatUtil.equal(o3.getVariance(), 0.0054, FloatUtil.Precision.P4));

        logger.debug(o3.toString());

        RealMatrix x = MatrixUtils.createColumnRealMatrix(o2.getWeights());
        RealMatrix y = MatrixUtils.createColumnRealMatrix(o3.getWeights());

        double a = 0.5;

        RealMatrix rets = MatrixUtils.createColumnRealMatrix(ret);

        RealMatrix z = x.scalarMultiply(a).add(y.scalarMultiply(1 - a));

        double reta = z.transpose().multiply(rets).getEntry(0, 0);
        double vara = z.transpose().multiply(cov).multiply(z).getEntry(0, 0);

        assert (FloatUtil.equal(reta, 0.0356, FloatUtil.Precision.P4));
        assert (FloatUtil.equal(vara, 0.00641, FloatUtil.Precision.P5));

        EfficientFrontier ef = markowitz.calculateEfficientFrontier();

        assert (ef.getfrontiers() != null);
        assert (ef.getGlobalMinimumVariance() != null);

        for (MarkowitzPortfolio p : ef.getfrontiers())
        {
            logger.debug(p.toString());
        }

        MarkowitzPortfolio tp = markowitz.calculateTangencyPortfolio();

        assert (FloatUtil.equal(tp.getWeights()[0], 1.0268, FloatUtil.Precision.P4));
        assert (FloatUtil.equal(tp.getWeights()[1], -0.3263, FloatUtil.Precision.P4));
        assert (FloatUtil.equal(tp.getWeights()[2], 0.2994, FloatUtil.Precision.P4));

        assert (FloatUtil.equal(tp.getExpectedReturn(), 0.05189, FloatUtil.Precision.P5));
        assert (FloatUtil.equal(tp.getVariance(), 0.01245, FloatUtil.Precision.P5));
    }
}
