package com.n26.store;

import com.n26.Application;
import com.n26.exception.TransactionOutOfRangeException;
import com.n26.exception.TransactionTimeInFutureException;
import com.n26.manager.ITransactionsManager;
import com.n26.model.Statistics;
import com.n26.model.Transaction;
import com.n26.service.TransactionService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class StatisticsStoreTest {
    @Autowired
    private TransactionService transactionService;

    @Autowired
    private ITransactionsManager transactionsManager;

    @Test
    public void testCreateFromTransaction() {
        Transaction transaction = new Transaction(BigDecimal.valueOf(12.5), 123L);
        StatisticsStore statisticsStore = new StatisticsStore();
        statisticsStore.create(transaction);
        assertEquals(123L, statisticsStore.getTimestamp());
        assertEquals("12.50", statisticsStore.getStatistics().getSum().toString());
        assertEquals(1, statisticsStore.getStatistics().getCount());
    }

    @Test
    public void testAggregateResult() {
        long time = Instant.now().toEpochMilli();
        final BigDecimal[] sum = {BigDecimal.ZERO};
        IntStream.range(0, 100).forEach(i->{
            Transaction t = new Transaction(BigDecimal.valueOf(i),time - i * 100L);
            sum[0] = sum[0].add(BigDecimal.valueOf(i));
            try {
                transactionService.addTransaction(t);
            } catch (TransactionOutOfRangeException | TransactionTimeInFutureException ignored) {}
        });
        Statistics result = new Statistics();
        List<StatisticsStore> validStatisticsStore = transactionsManager.getValidStatisticsStore(System.currentTimeMillis());
        validStatisticsStore.forEach(store -> store.addToResult(result));

        Statistics expected = new Statistics();
        expected.setMin(BigDecimal.ZERO);
        expected.setMax(BigDecimal.valueOf(99));
        expected.setCount(100);
        expected.setSum(BigDecimal.valueOf(4950));
        expected.setAvg(BigDecimal.valueOf(49.5));
        assertEquals(expected, result);
    }
}
