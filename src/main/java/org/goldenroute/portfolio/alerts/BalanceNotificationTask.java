package org.goldenroute.portfolio.alerts;

import java.awt.Color;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.goldenroute.Wrapper;
import org.goldenroute.portfolio.model.Account;
import org.goldenroute.portfolio.model.Portfolio;
import org.goldenroute.portfolio.service.AccountService;
import org.springframework.social.wechat.api.TemplateMessage;
import org.springframework.social.wechat.api.ValueColorPair;
import org.springframework.social.wechat.api.Wechat;

public class BalanceNotificationTask extends Thread
{
    private static final Logger logger = Logger.getLogger(BalanceNotificationTask.class);

    private Wrapper<Wechat> wechatWrapper;
    private AccountService accountService;

    public BalanceNotificationTask(Wrapper<Wechat> wechatWrapper, AccountService accountService)
    {
        super();
        this.wechatWrapper = wechatWrapper;
        this.accountService = accountService;
    }

    @Override
    public void run()
    {
        if (wechatWrapper != null && wechatWrapper.get() != null)
        {
            Calendar today = Calendar.getInstance();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            List<Account> accounts = accountService.findAll();

            for (Account account : accounts)
            {
                String touser = account.getProfile().getWechatId();

                if (touser != null && touser != "")
                {
                    String screenName = account.getProfile().getScreenName();

                    if (account.getPortfolios() != null)
                    {
                        for (Portfolio portfolio : account.getPortfolios())
                        {
                            sendAlert(today, format, touser, screenName, portfolio);

                            logger.debug(String.format("send alert to uid=%d, pid=%d, screenName=%s", account.getId(),
                                    portfolio.getId(), screenName));
                        }
                    }
                }
            }
        }
    }

    public void sendAlert(Calendar today, SimpleDateFormat format, String touser, String screenName, Portfolio portfolio)
    {
        BalanceNotification balanceNotification = new BalanceNotification();

        balanceNotification.setFirst(new ValueColorPair(String.format("Dear %s,", screenName), null));

        balanceNotification.setGreeting(new ValueColorPair(String.format("Portfolio balance for %s",
                portfolio.getName()), null));

        balanceNotification.setDate(new ValueColorPair(format.format(today.getTime()), null));

        balanceNotification.setBalance(formatBalance(portfolio.getValue()));
        balanceNotification.setDaily(formatChang(portfolio.getDailyChange(), portfolio.getDailyChangePercentage()));
        balanceNotification.setTotal(formatChang(portfolio.getTotalChange(), portfolio.getTotalChangePercentage()));

        balanceNotification.setRemark(new ValueColorPair(
                "Any questions or suggestions, please do not hesitate to contact us at email edward.yh.lu@gmail.com",
                null));

        TemplateMessage<BalanceNotification> message = new TemplateMessage<BalanceNotification>();
        message.setTouser(touser);
        message.setTemplateId("pjS8goH2crThA7cRLbFlMLY3W_Q33hYJcJHOgw_4ZlY");
        message.setUrl("http://sina.com");
        message.setData(balanceNotification);

        wechatWrapper.get().messageOperations().send(message);
    }

    public ValueColorPair formatBalance(BigDecimal value)
    {
        DecimalFormat formatter = new DecimalFormat();
        formatter.setGroupingUsed(true);
        formatter.setGroupingSize(3);
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
        formatter.setNegativePrefix("-");
        formatter.setPositivePrefix("+");

        return new ValueColorPair(formatter.format(value), null);
    }

    public ValueColorPair formatChang(BigDecimal change, BigDecimal percentage)
    {

        DecimalFormat formatter = new DecimalFormat();
        formatter.setGroupingUsed(true);
        formatter.setGroupingSize(3);
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
        formatter.setNegativePrefix("-");
        formatter.setPositivePrefix("+");

        String value = formatter.format(change) + ", " + formatter.format(percentage.multiply(BigDecimal.valueOf(100)))
                + "%";

        if (change.compareTo(BigDecimal.ZERO) >= 0)
        {
            return new ValueColorPair(value, "#" + Integer.toHexString(Color.GREEN.getRGB()));
        }
        else
        {
            return new ValueColorPair(value, "#" + Integer.toHexString(Color.RED.getRGB()));
        }
    }
}
