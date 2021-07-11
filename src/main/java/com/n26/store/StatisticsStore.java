package com.n26.store;

import com.n26.model.Constant;
import com.n26.model.Statistics;
import com.n26.model.Transaction;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Getter
public class StatisticsStore implements IStatisticsStore {
    private final ReadWriteLock lock;
    private final Statistics statistics;
    private long timestamp;

    public StatisticsStore() {
        statistics = new Statistics();
        lock = new ReentrantReadWriteLock();
    }

    @Override
    public void create(Transaction transaction) {
        statistics.createFromTransaction(transaction);
        timestamp = transaction.getTimestamp();
    }

    @Override
    public void addToResult(Statistics result) {
        try {
            getLock().readLock().lock();
            result.addStatistics(getStatistics());
        } finally {
            getLock().readLock().unlock();
        }
    }

    @Override
    public void merge(Transaction transaction) {
        BigDecimal transactionAmount = transaction.getAmount();
        statistics.setSum(statistics.getSum().add(transactionAmount));
        statistics.setCount(statistics.getCount() + 1);
        statistics.setAvg(statistics.getSum().divide(BigDecimal.valueOf(statistics.getCount()), 2, RoundingMode.HALF_UP));
        statistics.setMax(statistics.getMax().max(transactionAmount));
        statistics.setMin(statistics.getMin().min(transactionAmount));
    }

    @Override
    public boolean isEmpty() {
        return statistics.getCount() == 0;
    }

    @Override
    public void clear() {
        statistics.reset();
        timestamp = 0;
    }

    @Override
    public boolean isValid(long currentTimestamp) {
        if (currentTimestamp < getTimestamp()) {
            return false;
        }
        return currentTimestamp - getTimestamp() < Constant.TOTAL_WINDOW_SIZE_MILLIS;
    }
}
