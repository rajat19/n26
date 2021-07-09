package com.n26.model;

import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Getter
public class TransactionStatsAggregator {
    private ReadWriteLock lock;
    private Statistics statistics;
    private long timestamp;

    public TransactionStatsAggregator() {
        statistics = new Statistics();
        lock = new ReentrantReadWriteLock();
    }

    public void create(Transaction transaction) {
        statistics.createFromTransaction(transaction);
        timestamp = transaction.getTimestamp();
    }

    public void merge(Statistics result) {
        try {
            getLock().readLock().lock();
            result.setSum(result.getSum().add(getStatistics().getSum()));
            result.setCount(result.getCount() + getStatistics().getCount());
            BigDecimal average = result.getSum()
                    .divide(BigDecimal.valueOf(result.getCount()), 2, RoundingMode.HALF_UP);
            result.setAvg(average);
            result.setMin(result.getMin().min(getStatistics().getMin()));
            result.setMax(result.getMax().max(getStatistics().getMax()));
        } finally {
            getLock().readLock().unlock();
        }
    }

    public void merge(Transaction transaction) {
        BigDecimal transactionAmount = transaction.getAmount();
        statistics.setSum(statistics.getSum().add(transactionAmount));
        statistics.setCount(statistics.getCount() + 1);
        statistics.setAvg(statistics.getSum().divide(BigDecimal.valueOf(statistics.getCount()), 2, RoundingMode.HALF_UP));
        statistics.setMax(statistics.getMax().max(transactionAmount));
        statistics.setMin(statistics.getMin().min(transactionAmount));
    }

    public boolean isEmpty() {
        return statistics.getCount() == 0;
    }

    public void reset() {
        statistics.reset();
        timestamp = 0;
    }
}
