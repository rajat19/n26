package com.n26.service;

import com.n26.store.interfaces.ITransactionsStore;
import com.n26.model.Statistics;
import com.n26.store.StatisticsStore;
import com.n26.service.interfaces.IStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatisticsService implements IStatisticsService {
    private final ITransactionsStore transactionsStore;

    @Autowired
    public StatisticsService(ITransactionsStore transactionsStore) {
        this.transactionsStore = transactionsStore;
    }

    @Override
    public Statistics getStatistics() {
        long currentTime = System.currentTimeMillis();
        List<StatisticsStore> statisticsStores = transactionsStore.getValidStatisticsStore(currentTime);
        Statistics result = new Statistics();
        if (statisticsStores.isEmpty()) {
            result.resetToZero();
            return result;
        }
        statisticsStores.forEach(tsa -> tsa.mergeToResult(result));
        return result;
    }
}
