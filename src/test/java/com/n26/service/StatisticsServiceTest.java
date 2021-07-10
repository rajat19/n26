package com.n26.service;

import com.n26.Application;
import com.n26.exception.TransactionOutOfRangeException;
import com.n26.exception.TransactionTimeInFutureException;
import com.n26.manager.ITransactionsManager;
import com.n26.model.Statistics;
import com.n26.model.Transaction;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class StatisticsServiceTest {

    @Autowired
    private StatisticsService transactionStatsService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private ITransactionsManager container;

    @Before
    public void before(){
        container.clear();
    }

    @Test
    public void testEmptyStatistics(){
        Statistics stats = transactionStatsService.getStatistics();
        assertEquals(BigDecimal.valueOf(0.00).setScale(2, RoundingMode.HALF_UP), stats.getSum());
        assertEquals(0L, stats.getCount());
    }

    @Test
    public void testNonEmptyStatistics(){
        long time = Instant.now().toEpochMilli();
        final BigDecimal[] sum = {BigDecimal.ZERO};
        IntStream.range(0, 100).forEach(i->{
            Transaction t = new Transaction(BigDecimal.valueOf(i),time - i * 100L);
            sum[0] = sum[0].add(BigDecimal.valueOf(i));
            try {
                transactionService.addTransaction(t);
            } catch (TransactionOutOfRangeException | TransactionTimeInFutureException ignored) {}
        });

        Statistics stats = transactionStatsService.getStatistics();

        assertEquals(sum[0].setScale(2, RoundingMode.HALF_UP), stats.getSum());
        assertEquals(100, stats.getCount());
        assertEquals(BigDecimal.valueOf(0).setScale(2, RoundingMode.HALF_UP), stats.getMin());
        assertEquals(BigDecimal.valueOf(99).setScale(2, RoundingMode.HALF_UP), stats.getMax());
    }


}
