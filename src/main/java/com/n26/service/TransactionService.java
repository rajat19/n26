package com.n26.service;

import com.n26.exception.TransactionOutOfRangeException;
import com.n26.exception.TransactionTimeInFutureException;
import com.n26.manager.ITransactionsManager;
import com.n26.model.Transaction;
import com.n26.service.interfaces.ITransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Slf4j
public class TransactionService implements ITransactionService {
    @Autowired
    private ITransactionsManager transactionsStore;

    @Override
    public void addTransaction(Transaction transaction) throws TransactionOutOfRangeException, TransactionTimeInFutureException {
        long currentTimestamp = Instant.now().toEpochMilli();
        transactionsStore.addTransaction(transaction, currentTimestamp);
    }

    @Override
    public void deleteAllTransactions() {
        transactionsStore.clear();
    }
}
