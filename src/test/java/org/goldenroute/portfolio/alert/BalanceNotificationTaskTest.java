package org.goldenroute.portfolio.alert;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.goldenroute.PortfolioCoreApplication;
import org.goldenroute.Wrapper;
import org.goldenroute.portfolio.alerts.BalanceNotificationTask;
import org.goldenroute.portfolio.model.Portfolio;
import org.goldenroute.portfolio.service.AccountService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.social.wechat.api.Wechat;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(PortfolioCoreApplication.class)
public class BalanceNotificationTaskTest
{
    @Value("${social.wechat.test.touser}")
    private String touser;

    @Autowired
    private Wrapper<Wechat> wechatWrapper;

    @Autowired
    private AccountService accountService;

    @Test
    public void testSendAlert()
    {
        boolean testOnDemand = true;

        if (!testOnDemand)
        {
            Portfolio portfolio = new Portfolio();

            portfolio.setName("P01");
            portfolio.setValue(new BigDecimal(10000));
            portfolio.setDailyChange(new BigDecimal(-100));
            portfolio.setDailyChangePercentage(new BigDecimal(-0.001));
            portfolio.setTotalChange(new BigDecimal(300));
            portfolio.setTotalChangePercentage(new BigDecimal(0.003));

            Calendar today = Calendar.getInstance();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            BalanceNotificationTask task = new BalanceNotificationTask(wechatWrapper, accountService);

            task.sendAlert(today, format, touser, "Edward", portfolio);
        }
    }
}
