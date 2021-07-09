package com.n26.service;

import com.n26.model.ITransactionsContainer;
import com.n26.model.Statistics;
import com.n26.model.TransactionStatsAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class StatisticsService implements IStatisticsService{
    private final ITransactionsContainer transactionsContainer;

    @Autowired
    public StatisticsService(ITransactionsContainer transactionsContainer) {
        this.transactionsContainer = transactionsContainer;
    }

    @Override
    public Statistics getStatistics() {
        long currentTime = System.currentTimeMillis();
        List<TransactionStatsAggregator> transactionStatsAggregators = transactionsContainer.getValidTransactionStatsAggregator(currentTime);
        Statistics result = new Statistics();
        if (transactionStatsAggregators.isEmpty()) {
            result.setMax(BigDecimal.valueOf(0.00));
            result.setMin(BigDecimal.valueOf(0.00));
            return result;
        }
        transactionStatsAggregators.forEach(tsa -> tsa.merge(result));
        return result;
    }
}
