package com.n26.model;

import com.n26.exception.TransactionOutOfRangeException;
import com.n26.exception.TransactionTimeInFutureException;
import com.n26.model.request.TransactionRequest;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Slf4j
public class Transaction {
    @NonNull
    private BigDecimal amount;
    @NonNull
    private long timestamp;

    public static Transaction from(TransactionRequest transactionRequest) {
        Instant instant = Instant.parse(transactionRequest.getTimestamp());
        BigDecimal amount = BigDecimal.valueOf(Double.parseDouble(transactionRequest.getAmount()));
        long timestamp = instant.toEpochMilli();
        return new Transaction(amount, timestamp);
    }

    /**
     *
     * @param currentTimestamp timestamp against which transaction's validity needs to be checked
     * @return boolean
     * @throws TransactionOutOfRangeException when transaction if out of range of current timestamp
     * @throws TransactionTimeInFutureException when transaction is in future
     */
    public boolean isValid(long currentTimestamp) throws TransactionTimeInFutureException, TransactionOutOfRangeException {
        if (currentTimestamp < getTimestamp()) {
            throw new TransactionTimeInFutureException();
        }
        if (currentTimestamp - getTimestamp() >= Constant.TOTAL_WINDOW_SIZE_MILLIS) {
            throw new TransactionOutOfRangeException();
        }
        return true;
    }

    public int getIndex(long currentTimestamp) {
        long transactionTime = getTimestamp();
        return (int)((currentTimestamp - transactionTime) / Constant.WINDOW_SIZE_MILLIS) % (Constant.TOTAL_WINDOW_SIZE_MILLIS / Constant.WINDOW_SIZE_MILLIS);
    }
}
