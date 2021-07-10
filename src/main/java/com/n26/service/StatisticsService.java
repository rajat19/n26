package com.n26.service;

import com.n26.manager.ITransactionsManager;
import com.n26.model.Statistics;
import com.n26.store.StatisticsStore;
import com.n26.service.interfaces.IStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatisticsService implements IStatisticsService {
    @Autowired
    private ITransactionsManager transactionsManager;

    @Override
    public Statistics getStatistics() {
        long currentTime = System.currentTimeMillis();
        List<StatisticsStore> validStatisticsManagers = transactionsManager.getValidStatisticsStore(currentTime);
        Statistics result = new Statistics();
        if (validStatisticsManagers.isEmpty()) {
            result.resetToZero();
            return result;
        }
        validStatisticsManagers.forEach(tsa -> tsa.mergeToResult(result));
        return result;
    }
}
