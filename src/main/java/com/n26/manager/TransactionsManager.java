package com.n26.manager;

import com.n26.exception.TransactionOutOfRangeException;
import com.n26.exception.TransactionTimeInFutureException;
import com.n26.model.Constant;
import com.n26.model.Transaction;
import com.n26.store.StatisticsStore;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Primary
@Slf4j
public class TransactionsManager implements ITransactionsManager {
    private StatisticsStore[] statisticsStores;

    @PostConstruct
    public void init() {
        statisticsStores = new StatisticsStore[Constant.TOTAL_WINDOW_SIZE_MILLIS / Constant.WINDOW_SIZE_MILLIS];
        initStores();
    }

    @Override
    public void addTransaction(Transaction transaction, long currentTimestamp) throws TransactionTimeInFutureException, TransactionOutOfRangeException {
        if (transaction.isValid(currentTimestamp)) {
            aggregate(transaction, currentTimestamp);
        }
    }

    @Override
    public List<StatisticsStore> getValidStatisticsStore(long currentTimestamp) {
        return Arrays.stream(statisticsStores)
                .filter(ss -> ss.isValid(currentTimestamp))
                .collect(Collectors.toList());
    }

    @Override
    public void clear() {
        initStores();
    }

    private void initStores() {
        for (int i = 0; i< statisticsStores.length; i++) {
            statisticsStores[i] = new StatisticsStore();
        }
    }

    private void aggregate(Transaction transaction, long currentTimestamp) {
        int index = transaction.getIndex(currentTimestamp);
        StatisticsStore statisticsStore = statisticsStores[index];
        try {
            statisticsStore.getLock().writeLock().lock();
            if (statisticsStore.isEmpty()) {
                statisticsStore.create(transaction);
            } else {
                if (statisticsStore.isValid(currentTimestamp)) {
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
}
