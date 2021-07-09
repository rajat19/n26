package com.n26.model;

import com.n26.exception.TransactionOutOfRangeException;

import java.util.List;

public interface ITransactionsContainer {
    void addTransaction(Transaction transaction, long currentTimestamp) throws TransactionOutOfRangeException;
    List<TransactionStatsAggregator> getValidTransactionStatsAggregator(long timestamp);
    void clear();
}
