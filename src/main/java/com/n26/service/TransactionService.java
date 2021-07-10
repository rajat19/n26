package com.n26.service;

import com.n26.exception.TransactionOutOfRangeException;
import com.n26.store.interfaces.ITransactionsStore;
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
    private ITransactionsStore transactionsStore;

    @Override
    public void addTransaction(Transaction transaction) throws TransactionOutOfRangeException {
        long currentTimestamp = Instant.now().toEpochMilli();
        transactionsStore.addTransaction(transaction, currentTimestamp);
    }

    @Override
    public void deleteAllTransactions() {
        transactionsStore.clear();
    }
}
