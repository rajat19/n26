package com.n26.model;

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
        log.info("Request: {}", transactionRequest);

        Instant instant = Instant.parse(transactionRequest.getTimestamp());
        BigDecimal amount = BigDecimal.valueOf(Double.parseDouble(transactionRequest.getAmount()));
        long timestamp = instant.toEpochMilli();
        return new Transaction(amount, timestamp);
    }
}
