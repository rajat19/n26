package com.n26.model;

import com.n26.exception.TransactionOutOfRangeException;
import com.n26.exception.TransactionTimeInFutureException;
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
public class TransactionsContainer implements ITransactionsContainer{
    private TransactionStatsAggregator[] transactionStatsAggregators;

    @Value("${max.time.in.millis:60000}")
    private int maxTimeToKeepInMillis;

    @Value("${interval.time.in.millis:1000}")
    private int timeIntervalInMillis;

    @PostConstruct
    public void init() {
        transactionStatsAggregators = new TransactionStatsAggregator[maxTimeToKeepInMillis / timeIntervalInMillis];
        initAggregator();
    }

    @Override
    public void addTransaction(Transaction transaction, long currentTimestamp) throws TransactionOutOfRangeException {
        if (!isTransactionValid(transaction.getTimestamp(), currentTimestamp)) {
            throw new TransactionOutOfRangeException();
        }
        aggregate(transaction, currentTimestamp);
    }

    @Override
    public List<TransactionStatsAggregator> getValidTransactionStatsAggregator(long currentTimestamp) {
        return Arrays.stream(transactionStatsAggregators)
                .filter(tsa -> isTransactionValid(tsa.getTimestamp(),currentTimestamp))
                .collect(Collectors.toList());
    }

    @Override
    public void clear() {
        initAggregator();
    }

    private void initAggregator() {
        for (int x=0; x<transactionStatsAggregators.length; x++) {
            transactionStatsAggregators[x] = new TransactionStatsAggregator();
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
        TransactionStatsAggregator transactionStatsAggregator = transactionStatsAggregators[index];
        try {
            transactionStatsAggregator.getLock().writeLock().lock();
            if (transactionStatsAggregator.isEmpty()) {
                transactionStatsAggregator.create(transaction);
            } else {
                if (isTransactionValid(transactionStatsAggregator.getTimestamp(), currentTimestamp)) {
                    transactionStatsAggregator.merge(transaction);
                } else {
                    transactionStatsAggregator.reset();
                    transactionStatsAggregator.create(transaction);
                }
            }
        } finally {
            transactionStatsAggregator.getLock().writeLock().unlock();
        }
    }

    private int getTransactionIndex(Transaction transaction, long currentTime) {
        long transactionTime = transaction.getTimestamp();
        return (int)((currentTime - transactionTime) / timeIntervalInMillis) % (maxTimeToKeepInMillis / timeIntervalInMillis);
    }
}
