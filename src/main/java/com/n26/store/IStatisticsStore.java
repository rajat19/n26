package com.n26.store;

import com.n26.model.Statistics;
import com.n26.model.Transaction;

public interface IStatisticsStore {
    void create(Transaction transaction);
    void addToResult(Statistics result);
    void merge(Transaction transaction);
    boolean isEmpty();
    void clear();
    /**
     * @param currentTimestamp check whether statistics store is valid for current time
     * @return boolean
     */
    boolean isValid(long currentTimestamp);
}
