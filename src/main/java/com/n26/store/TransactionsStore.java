package com.n26.store;

import com.n26.exception.TransactionOutOfRangeException;
import com.n26.exception.TransactionTimeInFutureException;
import com.n26.model.Transaction;
import com.n26.store.interfaces.ITransactionsStore;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Primary
@Slf4j
public class TransactionsStore implements ITransactionsStore {
    private StatisticsStore[] statisticsStores;

    @Value("${max.time.in.millis:60000}")
    private int maxTimeToKeepInMillis;

    @Value("${interval.time.in.millis:1000}")
    private int timeIntervalInMillis;

    @PostConstruct
    public void init() {
        statisticsStores = new StatisticsStore[maxTimeToKeepInMillis / timeIntervalInMillis];
        initAggregator();
    }

    @Override
    @SneakyThrows
    public void addTransaction(Transaction transaction, long currentTimestamp) {
        if (!isTransactionValid(transaction.getTimestamp(), currentTimestamp)) {
            throw new TransactionOutOfRangeException();
        }
        aggregate(transaction, currentTimestamp);
    }

    @Override
    public List<StatisticsStore> getValidStatisticsStore(long currentTimestamp) {
        return Arrays.stream(statisticsStores)
                .filter(ss -> isTransactionValid(ss.getTimestamp(),currentTimestamp))
                .collect(Collectors.toList());
    }

    @Override
    public void clear() {
        initAggregator();
    }

    private void initAggregator() {
        for (int i = 0; i< statisticsStores.length; i++) {
            statisticsStores[i] = new StatisticsStore();
        }
    }

    @SneakyThrows
    private boolean isTransactionValid(long transactionTimestamp, long currentTimestamp) {
        if (currentTimestamp < transactionTimestamp) {
            throw new TransactionTimeInFutureException();
        }
        return currentTimestamp - transactionTimestamp < maxTimeToKeepInMillis;
    }

    private void aggregate(Transaction transaction, long currentTimestamp) {
        int index = getTransactionIndex(transaction, currentTimestamp);
        StatisticsStore statisticsStore = statisticsStores[index];
        try {
            statisticsStore.getLock().writeLock().lock();
            if (statisticsStore.isEmpty()) {
                statisticsStore.create(transaction);
            } else {
                if (isTransactionValid(statisticsStore.getTimestamp(), currentTimestamp)) {
                    statisticsStore.merge(transaction);
                } else {
                    statisticsStore.clear();
                    statisticsStore.create(transaction);
                }
            }
        } finally {
            statisticsStore.getLock().writeLock().unlock();
        }
    }

    private int getTransactionIndex(Transaction transaction, long currentTime) {
        long transactionTime = transaction.getTimestamp();
        return (int)((currentTime - transactionTime) / timeIntervalInMillis) % (maxTimeToKeepInMillis / timeIntervalInMillis);
    }
}
