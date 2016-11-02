package org.goldenroute.portfolio.service;

import java.math.BigDecimal;
import java.util.stream.DoubleStream;

import org.apache.log4j.Logger;
import org.goldenroute.PortfolioCoreApplication;
import org.goldenroute.util.FloatUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ojalgo.finance.portfolio.MarkowitzModel;
import org.ojalgo.matrix.BasicMatrix.Factory;
import org.ojalgo.matrix.PrimitiveMatrix;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@Configuration
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(PortfolioCoreApplication.class)
public class OjAlgoMarkowitzModelTest
{
    private static final Logger logger = Logger.getLogger(OjAlgoMarkowitzModelTest.class);

    @Test
    public void test1()
    {
        Factory<PrimitiveMatrix> matrixFactory = PrimitiveMatrix.FACTORY;
        PrimitiveMatrix cov = matrixFactory.rows(new double[][] { { 0.01, 0.0018, 0.0011 }, { 0.0018, 0.0109, 0.0026 },
                { 0.0011, 0.0026, 0.0199 } });
        PrimitiveMatrix ret = matrixFactory.columns(new double[] { 0.0427, 0.0015, 0.0285 });

        MarkowitzModel markowitz = new MarkowitzModel(cov, ret);
        markowitz.setShortingAllowed(true);
        markowitz.setRiskAversion(BigDecimal.valueOf(1000000));

        logger.debug(markowitz.getWeights().toString());
        logger.debug(Double.toString(markowitz.getMeanReturn()));
        logger.debug(Double.toString(markowitz.getReturnVariance()));

        assert (FloatUtil.equal(markowitz.getWeights().get(0).doubleValue(), 0.4411, FloatUtil.Precision.P4));
        assert (FloatUtil.equal(markowitz.getWeights().get(1).doubleValue(), 0.3656, FloatUtil.Precision.P4));
        assert (FloatUtil.equal(markowitz.getWeights().get(2).doubleValue(), 0.1933, FloatUtil.Precision.P4));
        assert (FloatUtil.equal(markowitz.getMeanReturn(), 0.02489, FloatUtil.Precision.P5));
        assert (FloatUtil.equal(markowitz.getReturnVariance(), 0.005282, FloatUtil.Precision.P6));

        markowitz.setRiskAversion(null);
        markowitz.setTargetReturn(BigDecimal.valueOf(0.0427));

        logger.debug(markowitz.getWeights().toString());
        logger.debug(Double.toString(markowitz.getMeanReturn()));
        logger.debug(Double.toString(markowitz.getReturnVariance()));

        FloatUtil.equal(markowitz.getWeights().get(1).doubleValue(), -0.09075, FloatUtil.Precision.P4);

        assert (FloatUtil.equal(markowitz.getWeights().get(0).doubleValue(), 0.82745, FloatUtil.Precision.P4));
        assert (FloatUtil.equal(markowitz.getWeights().get(1).doubleValue(), -0.09075, FloatUtil.Precision.P4));
        assert (FloatUtil.equal(markowitz.getWeights().get(2).doubleValue(), 0.26329, FloatUtil.Precision.P4));
        assert (FloatUtil.equal(markowitz.getMeanReturn(), 0.0427, FloatUtil.Precision.P4));
        assert (FloatUtil.equal(markowitz.getReturnVariance(), 0.0084, FloatUtil.Precision.P4));

        markowitz.setTargetReturn(BigDecimal.valueOf(0.0285));

        logger.debug(markowitz.getWeights().toString());
        logger.debug(Double.toString(markowitz.getMeanReturn()));
        logger.debug(Double.toString(markowitz.getReturnVariance()));

        assert (FloatUtil.equal(markowitz.getWeights().get(0).doubleValue(), 0.5194, FloatUtil.Precision.P4));
        assert (FloatUtil.equal(markowitz.getWeights().get(1).doubleValue(), 0.2732, FloatUtil.Precision.P4));
        assert (FloatUtil.equal(markowitz.getWeights().get(2).doubleValue(), 0.2075, FloatUtil.Precision.P4));
        assert (FloatUtil.equal(markowitz.getMeanReturn(), 0.0285, FloatUtil.Precision.P4));
        assert (FloatUtil.equal(markowitz.getReturnVariance(), 0.0054, FloatUtil.Precision.P4));
    }

    @Test
    public void test2()
    {
        Factory<PrimitiveMatrix> matrixFactory = PrimitiveMatrix.FACTORY;
        PrimitiveMatrix cov = matrixFactory.rows(new double[][] { { 1, 0.40, 0.15 }, { 0.4, 1, 0.35 },
                { 0.15, 0.35, 1 } });
        PrimitiveMatrix ret = matrixFactory.columns(new double[] { 0.028, 0.063, 0.108 });

        MarkowitzModel markowitz = new MarkowitzModel(cov, ret);
        markowitz.setRiskAversion(BigDecimal.valueOf(0.01));

        for (int i = 0; i < 3; i++)
        {
            markowitz.setLowerLimit(i, BigDecimal.valueOf(0.2));
            markowitz.setUpperLimit(i, BigDecimal.valueOf(0.5));
        }

        logger.debug(markowitz.getWeights().toString());
        logger.debug(Double.toString(markowitz.getMeanReturn()));
        logger.debug(Double.toString(markowitz.getReturnVariance()));

        assert (FloatUtil.equal(markowitz.getWeights().get(0).doubleValue(), 0.2000, FloatUtil.Precision.P4));
        assert (FloatUtil.equal(markowitz.getWeights().get(1).doubleValue(), 0.3000, FloatUtil.Precision.P4));
        assert (FloatUtil.equal(markowitz.getWeights().get(2).doubleValue(), 0.5000, FloatUtil.Precision.P4));

        markowitz.setRiskAversion(BigDecimal.valueOf(1000000));

        logger.debug(markowitz.getWeights().toString());
        logger.debug(Double.toString(markowitz.getMeanReturn()));
        logger.debug(Double.toString(markowitz.getReturnVariance()));
    }

    @Test
    public void test3()
    {
        Factory<PrimitiveMatrix> matrixFactory = PrimitiveMatrix.FACTORY;

        double riskFree = 0.005;

        PrimitiveMatrix cov = matrixFactory.rows(new double[][] { { 0, 0, 0, 0 }, { 0, 0.01, 0.0018, 0.0011 },
                { 0, 0.0018, 0.0109, 0.0026 }, { 0, 0.0011, 0.0026, 0.0199 } });
        PrimitiveMatrix ret = matrixFactory.columns(new double[] { riskFree, 0.0427, 0.0015, 0.0285 });

        MarkowitzModel markowitz = new MarkowitzModel(cov, ret);
        markowitz.setShortingAllowed(true);

        logger.debug(markowitz.getWeights().toString());

        double[] riskAssets = new double[3];

        for (int i = 0; i < 3; i++)
        {
            riskAssets[i] = markowitz.getWeights().get(i + 1).doubleValue();
        }

        double sum = DoubleStream.of(riskAssets).sum();

        for (int i = 0; i < riskAssets.length; i++)
        {
            riskAssets[i] = riskAssets[i] / sum;
        }

        assert (FloatUtil.equal(riskAssets[0], 1.0268, FloatUtil.Precision.P4));
        assert (FloatUtil.equal(riskAssets[1], -0.3263, FloatUtil.Precision.P4));
        assert (FloatUtil.equal(riskAssets[2], 0.2994, FloatUtil.Precision.P4));
    }
}
