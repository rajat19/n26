package com.n26.service;

import com.n26.exception.TransactionOutOfRangeException;
import com.n26.model.ITransactionsContainer;
import com.n26.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Slf4j
public class TransactionService implements ITransactionService {
    @Autowired
    private ITransactionsContainer transactionsContainer;

    @Override
    public void addTransaction(Transaction transaction) throws TransactionOutOfRangeException {
        long currentTimestamp = Instant.now().toEpochMilli();
        transactionsContainer.addTransaction(transaction, currentTimestamp);
    }

    @Override
    public void deleteAllTransactions() {
        transactionsContainer.clear();
    }
}
