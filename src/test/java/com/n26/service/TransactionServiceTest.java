package com.n26.service;

import com.n26.Application;
import com.n26.exception.TransactionOutOfRangeException;
import com.n26.model.Transaction;
import com.n26.store.StatisticsStore;
import com.n26.store.TransactionsStore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class TransactionServiceTest {
    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionsStore store;

    @Before
    public void before(){
        store.clear();
    }

    @Test(expected= TransactionOutOfRangeException.class )
    public void testGetExceptionWhenInputHasInvalidTimestamp() throws TransactionOutOfRangeException {
        Transaction txn = new Transaction(BigDecimal.valueOf(12.5), 123);
        transactionService.addTransaction(txn);
    }

    @Test
    public void testEmptyGettingAggregatorsWithInValidTime() {
        long time = System.currentTimeMillis();
        Transaction txn = new Transaction(BigDecimal.valueOf(12.5), 123);
        try {
            transactionService.addTransaction(txn);
        } catch (TransactionOutOfRangeException ignored) {}

        List<StatisticsStore> list = store.getValidStatisticsStore(time);

        assertNotNull(list);
        assertEquals(0, list.size());
    }

    @Test
    public void testConcurrentTransactions(){
        final ExecutorService executor = Executors.newFixedThreadPool(10);
        long time = System.currentTimeMillis();
        try{
            IntStream.range(0, 100).forEach(i-> {
                executor.execute(()->{
                    Transaction t = new Transaction(BigDecimal.valueOf(15L * i), time - (i + i * 100L) );
                    try {
                        Thread.sleep(1);
                        transactionService.addTransaction(t);
                    } catch (Exception ignored) {}
                });

            });

        }finally{
            executor.shutdown();
        }

        try {
            Thread.sleep(2000); //making sure all completed
        } catch (InterruptedException ignored) {}

        List<StatisticsStore> list = store.getValidStatisticsStore(time);

        assertNotNull(list);

        int sum = 0;
        for (StatisticsStore agg : list){
            sum += agg.getStatistics().getCount();
        }
        assertEquals(100, sum);
    }
}
