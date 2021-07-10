package com.n26.store.interfaces;

import com.n26.model.Transaction;
import com.n26.store.StatisticsStore;

import java.util.List;

public interface ITransactionsStore {
    void addTransaction(Transaction transaction, long currentTimestamp);
    List<StatisticsStore> getValidStatisticsStore(long timestamp);
    void clear();
}
