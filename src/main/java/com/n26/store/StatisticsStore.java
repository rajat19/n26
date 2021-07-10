package com.n26.store;

import com.n26.model.Statistics;
import com.n26.model.Transaction;
import com.n26.store.interfaces.IStatisticsStore;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Getter
@Component
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
    public void mergeToResult(Statistics result) {
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
}
