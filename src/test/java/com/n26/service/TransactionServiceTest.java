package com.n26.service;

import com.n26.Application;
import com.n26.exception.TransactionOutOfRangeException;
import com.n26.exception.TransactionTimeInFutureException;
import com.n26.model.Transaction;
import com.n26.store.StatisticsStore;
import com.n26.manager.TransactionsManager;
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

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class TransactionServiceTest {
    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionsManager transactionsManager;

    @Before
    public void before(){
        transactionsManager.clear();
    }

    @Test(expected= TransactionOutOfRangeException.class )
    public void testGetExceptionWhenInputHasInvalidTimestamp() throws TransactionOutOfRangeException, TransactionTimeInFutureException {
        Transaction txn = new Transaction(BigDecimal.valueOf(12.5), 123);
        transactionService.addTransaction(txn);
    }

    @Test
    public void testEmptyStoreWithInValidTime() {
        long time = System.currentTimeMillis();
        Transaction txn = new Transaction(BigDecimal.valueOf(12.5), 123);
        try {
            transactionService.addTransaction(txn);
        } catch (TransactionOutOfRangeException | TransactionTimeInFutureException ignored) {}

        List<StatisticsStore> list = transactionsManager.getValidStatisticsStore(time);

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
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {}

        List<StatisticsStore> list = transactionsManager.getValidStatisticsStore(time);

        assertNotNull(list);

        int sum = 0;
        for (StatisticsStore agg : list){
            sum += agg.getStatistics().getCount();
        }
        assertEquals(100, sum);
    }

    @Test
    public void testConcurrentTransactionsAndThenDelete(){
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
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {}

        transactionService.deleteAllTransactions();

        List<StatisticsStore> list = transactionsManager.getValidStatisticsStore(time);

        assertNotNull(list);
        assertEquals(0, list.size());
    }
}
