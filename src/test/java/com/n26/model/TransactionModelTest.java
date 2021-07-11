package com.n26.model;

import com.n26.exception.TransactionOutOfRangeException;
import com.n26.exception.TransactionTimeInFutureException;
import com.n26.model.request.TransactionRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SpringBootTest
public class TransactionModelTest {

    @Test
    public void testGenerationFromTransactionRequest() {
        TransactionRequest transactionRequest = new TransactionRequest("40.2", "2021-02-01T14:01:30.312Z");
        Transaction transaction = Transaction.from(transactionRequest);

        assertEquals(BigDecimal.valueOf(40.2), transaction.getAmount());
        assertEquals(1612188090312L, transaction.getTimestamp());
    }

    @Test(expected = TransactionTimeInFutureException.class)
    public void testTransactionTimeInFuture() throws TransactionTimeInFutureException, TransactionOutOfRangeException {
        TransactionRequest transactionRequest = new TransactionRequest("40.2", "2021-02-01T14:01:30.312Z");
        Transaction transaction = Transaction.from(transactionRequest);
        long currentTimestamp = 1612188020312L;
        transaction.isValid(currentTimestamp);
    }

    @Test(expected = TransactionOutOfRangeException.class)
    public void testTransactionOutOFRange() throws TransactionTimeInFutureException, TransactionOutOfRangeException {
        TransactionRequest transactionRequest = new TransactionRequest("40.2", "2021-02-01T14:01:30.312Z");
        Transaction transaction = Transaction.from(transactionRequest);
        long currentTimestamp = 1612188190312L;
        transaction.isValid(currentTimestamp);
    }

    @Test
    public void testValidTransaction() throws TransactionTimeInFutureException, TransactionOutOfRangeException {
        TransactionRequest transactionRequest = new TransactionRequest("40.2", "2021-02-01T14:01:30.312Z");
        Transaction transaction = Transaction.from(transactionRequest);
        long currentTimestamp = 1612188098312L;
        assertTrue(transaction.isValid(currentTimestamp));
    }
}
