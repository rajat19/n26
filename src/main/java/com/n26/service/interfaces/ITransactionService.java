package com.n26.service.interfaces;

import com.n26.exception.TransactionOutOfRangeException;
import com.n26.exception.TransactionTimeInFutureException;
import com.n26.model.Transaction;

public interface ITransactionService {
    void addTransaction(Transaction transaction) throws TransactionOutOfRangeException, TransactionTimeInFutureException;
    void deleteAllTransactions();
}
