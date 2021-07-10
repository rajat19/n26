package com.n26.manager;

import com.n26.exception.TransactionOutOfRangeException;
import com.n26.exception.TransactionTimeInFutureException;
import com.n26.model.Transaction;
import com.n26.store.StatisticsStore;

import java.util.List;

public interface ITransactionsManager {
    void addTransaction(Transaction transaction, long currentTimestamp) throws TransactionTimeInFutureException, TransactionOutOfRangeException;
    List<StatisticsStore> getValidStatisticsStore(long timestamp);
    void clear();
}
