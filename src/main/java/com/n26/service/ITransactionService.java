package com.n26.service;

import com.n26.exception.TransactionOutOfRangeException;
import com.n26.model.Transaction;

public interface ITransactionService {
    void addTransaction(Transaction transaction) throws TransactionOutOfRangeException;
    void deleteAllTransactions();
}
