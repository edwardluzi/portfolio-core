package org.goldenroute.portfolio;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import javax.sql.DataSource;

import org.goldenroute.portfolio.model.Account;
import org.goldenroute.portfolio.model.AssetClass;
import org.goldenroute.portfolio.model.Currency;
import org.goldenroute.portfolio.model.Portfolio;
import org.goldenroute.portfolio.model.Profile;
import org.goldenroute.portfolio.model.Transaction;
import org.goldenroute.portfolio.model.Transaction.Type;
import org.goldenroute.portfolio.repository.AccountRepository;
import org.goldenroute.portfolio.repository.PortfolioRepository;
import org.goldenroute.portfolio.repository.ProfileRepository;
import org.goldenroute.portfolio.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class RepositoryUtils
{
    @Autowired
    private DataSource dataSource;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public DataSource getDataSource()
    {
        return dataSource;
    }

    public AccountRepository getAccountRepository()
    {
        return accountRepository;
    }

    public ProfileRepository getProfileRepository()
    {
        return profileRepository;
    }

    public PortfolioRepository getPortfolioRepository()
    {
        return portfolioRepository;
    }

    public TransactionRepository getTransactionRepository()
    {
        return transactionRepository;
    }

    public RepositoryUtils(DataSource dataSource, AccountRepository accountRepository,
            ProfileRepository profileRepository, PortfolioRepository portfolioRepository,
            TransactionRepository transactionRepository)
    {
        super();

        this.dataSource = dataSource;
        this.accountRepository = accountRepository;
        this.profileRepository = profileRepository;
        this.portfolioRepository = portfolioRepository;
        this.transactionRepository = transactionRepository;
    }

    public Account createAccount(String username)
    {
        Account created = new Account(username);
        Profile profile = new Profile();
        profile.setScreenName(username);

        created.setProfile(profile);
        profile.setOwner(created);

        accountRepository.save(created);

        Account fetched = accountRepository.findOne(created.getId());
        assertEquals(fetched.getProfile().getScreenName(), username);

        return fetched;
    }

    public Account findAccount(String username)
    {
        return accountRepository.findByUsername(username);
    }

    public void deleteAccount(Account account)
    {
        accountRepository.delete(account);
    }

    public void deleteAccount(String username)
    {
        Account account = accountRepository.findByUsername(username);

        if (account != null)
        {
            accountRepository.delete(account);
        }
    }

    public Portfolio createPortfolio(Account account, String name, String benchmark, Currency currency,
            AssetClass primaryAssetClass, String description)
    {
        Portfolio portfolio = new Portfolio();

        portfolio.setName(name);
        portfolio.setBenchmark(benchmark);
        portfolio.setCurrency(currency);
        portfolio.setPrimaryAssetClass(primaryAssetClass);
        portfolio.setDescription(description);

        if (account != null)
        {
            account.addOrUpdate(portfolio);
            portfolio.setOwner(account);
            if (account.getId() != null)
            {
                portfolioRepository.save(portfolio);
            }
        }

        return portfolio;
    }

    public void deletePortfolio(Account account, Portfolio portfolio)
    {
        account.remove(portfolio.getId());
        accountRepository.save(account);
    }

    public Transaction createTransaction(Portfolio portfolio, Long date, String ticker, Type type, BigDecimal price,
            BigDecimal amount, BigDecimal commission, BigDecimal otherCharges)
    {
        Transaction transaction = new Transaction();

        transaction.setTimestamp(date);
        transaction.setTicker(ticker);
        transaction.setType(type);
        transaction.setPrice(price);
        transaction.setAmount(amount);
        transaction.setCommission(commission);
        transaction.setOtherCharges(otherCharges);

        if (portfolio != null)
        {
            portfolio.addOrUpdate(transaction);
            transaction.setOwner(portfolio);

            if (portfolio.getId() != null)
            {
                transactionRepository.save(transaction);
            }
        }

        return transaction;
    }

    public void deleteTransaction(Portfolio portfolio, Transaction transaction)
    {
        portfolio.remove(transaction.getId());
        portfolioRepository.save(portfolio);
    }
}
