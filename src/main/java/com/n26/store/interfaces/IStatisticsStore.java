package com.n26.store.interfaces;

import com.n26.model.Statistics;
import com.n26.model.Transaction;

public interface IStatisticsStore {
    void create(Transaction transaction);
    void mergeToResult(Statistics result);
    void merge(Transaction transaction);
    boolean isEmpty();
    void clear();
}
