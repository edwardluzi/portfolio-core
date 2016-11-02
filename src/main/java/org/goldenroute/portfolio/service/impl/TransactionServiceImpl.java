package org.goldenroute.portfolio.service.impl;

import org.goldenroute.portfolio.model.Transaction;
import org.goldenroute.portfolio.repository.TransactionRepository;
import org.goldenroute.portfolio.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;

public class TransactionServiceImpl implements TransactionService
{
    @Autowired
    private TransactionRepository transactionRepository;

    @Override
    public void save(Transaction transaction)
    {
        transactionRepository.save(transaction);
    }
}
